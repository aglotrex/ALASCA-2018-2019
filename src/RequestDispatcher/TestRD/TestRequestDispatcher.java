package RequestDispatcher.TestRD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.datacenter.hardware.computers.Computer;
import fr.sorbonne_u.datacenter.hardware.processors.Processor;
import fr.sorbonne_u.datacenter.hardware.tests.ComputerMonitor;
import fr.sorbonne_u.datacenter.software.applicationvm.ApplicationVM;
import RequestDispatcher.RequestDispatcher;
import org.apache.commons.math3.util.Pair;
import RequestGenerator.RequestGenerator;

import static RequestDispatcher.RequestDispatcher.PortTypeRequestDispatcher.REQUEST_NOTIFICATION_IN;

public class				TestRequestDispatcher
		extends		AbstractCVM
{
	public static final String	ComputerServicesInboundPortURI = "cs-ibp" ;
	public static final String	ComputerStaticStateDataInboundPortURI = "css-dip" ;
	public static final String	ComputerDynamicStateDataInboundPortURI = "cds-dip" ;
	public static final String	ApplicationVM0ManagementInboundPortURI = "avm0-ibp" ;
	public static final String	ApplicationVM1ManagementInboundPortURI = "avm1-ibp" ;
	public static final String	RequestGeneratorManagementInboundPortURI = "rgmip" ;
	public static final String	RequestSubmissionInboundPortURIAVM0 = "rsibp0" ;
	public static final String	RequestSubmissionInboundPortURIAVM1 = "rsibp1" ;
	public static final String	RequestNotificationInboundPortURI = "rnibp" ;

	public static final String	RequestSubmissionInboundPortURIdispatcher = "rsibp_disp" ;
	public static final String	RequestNotificationInboundPortURIdispatcher = "rnibp_disp" ;

	/** 	Computer monitor component.										*/
	protected ComputerMonitor						cm ;
	/** 	Application virtual machine component.							*/
	protected ApplicationVM							vm ;
	protected ApplicationVM							vm1 ;
	/** 	RequestP generator component.										*/
	protected RequestGenerator						rg ;

	/**		RequestP dispatcher component.								*/
	protected RequestDispatcher rd ;

	/** Integrator component.											*/
	protected Integrator								integ ;

	// ------------------------------------------------------------------------
	// Component virtual machine constructors
	// ------------------------------------------------------------------------

	public				TestRequestDispatcher()
			throws Exception
	{
		super();
	}

	// ------------------------------------------------------------------------
	// Component virtual machine methods
	// ------------------------------------------------------------------------

	@Override
	public void			deploy() throws Exception
	{
		Processor.DEBUG = true ;

		// --------------------------------------------------------------------
		// Create and deploy a computer component with its 2 processors and
		// each with 2 cores.
		// --------------------------------------------------------------------
		String computerURI = "computer0" ;
		int numberOfProcessors = 2 ;
		int numberOfCores = 2 ;
		Set<Integer> admissibleFrequencies = new HashSet<Integer>() ;
		admissibleFrequencies.add(1500) ;	// Cores can run at 1,5 GHz
		admissibleFrequencies.add(3000) ;	// and at 3 GHz
		Map<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
		processingPower.put(1500, 1500000) ;	// 1,5 GHz executes 1,5 Mips
		processingPower.put(3000, 3000000) ;	// 3 GHz executes 3 Mips
		Computer c = new Computer(
				computerURI,
				admissibleFrequencies,
				processingPower,
				1500,		// Test scenario 1, frequency = 1,5 GHz
				// 3000,	// Test scenario 2, frequency = 3 GHz
				1500,		// max frequency gap within a processor
				numberOfProcessors,
				numberOfCores,
				ComputerServicesInboundPortURI,
				ComputerStaticStateDataInboundPortURI,
				ComputerDynamicStateDataInboundPortURI) ;
		this.addDeployedComponent(c) ;
		c.toggleLogging() ;
		c.toggleTracing() ;
		// --------------------------------------------------------------------



		// --------------------------------------------------------------------
		// Create the computer monitor component and connect its to ports
		// with the computer component.
		// --------------------------------------------------------------------
		this.cm = new ComputerMonitor(computerURI,
				true,
				ComputerStaticStateDataInboundPortURI,
				ComputerDynamicStateDataInboundPortURI) ;
		this.addDeployedComponent(this.cm) ;
		// --------------------------------------------------------------------



		// --------------------------------------------------------------------
		// Create an Application VM component
		// --------------------------------------------------------------------
		this.vm = new ApplicationVM("vm0",	// application vm component URI
				ApplicationVM0ManagementInboundPortURI,
				RequestSubmissionInboundPortURIAVM0,
				RequestNotificationInboundPortURIdispatcher) ;
		this.addDeployedComponent(this.vm) ;
		// Toggle on tracing and logging in the application virtual machine to
		// follow the execution of individual requests.
		this.vm.toggleTracing() ;
		this.vm.toggleLogging() ;
		// --------------------------------------------------------------------
		this.vm1 = new ApplicationVM("vm1",	// application vm component URI
				ApplicationVM1ManagementInboundPortURI,
				RequestSubmissionInboundPortURIAVM1,
				RequestNotificationInboundPortURI) ;
		this.addDeployedComponent(this.vm1) ;
		// Toggle on tracing and logging in the application virtual machine to
		// follow the execution of individual requests.
		this.vm1.toggleTracing() ;
		this.vm1.toggleLogging() ;
		// --------------------------------------------------------------------
		String RequestSubmissionDispatcherLinktoVM1= java.util.UUID.randomUUID().toString();
		String RequestSubmissionDispatcherLinktoVM2= java.util.UUID.randomUUID().toString();
		ArrayList<Pair<String,String>> vm = new ArrayList<>();
		vm.add(new Pair<>(RequestNotificationInboundPortURIdispatcher,RequestSubmissionDispatcherLinktoVM1));
		vm.add(new Pair<>(RequestNotificationInboundPortURI,RequestSubmissionDispatcherLinktoVM2));
		// --------------------------------------------------------------------
		// Creating the request dispatcher component.
		// --------------------------------------------------------------------

		HashMap<RequestDispatcher.PortTypeRequestDispatcher,String> uriRD =
				new HashMap<>();
		uriRD.put(RequestDispatcher.PortTypeRequestDispatcher.DISPATCHER_URI ,
				java.util.UUID.randomUUID().toString());

		uriRD.put(		RequestDispatcher.PortTypeRequestDispatcher.REQUEST_NOTIFICATION_IN,
						RequestNotificationInboundPortURIdispatcher);

		uriRD.put(		RequestDispatcher.PortTypeRequestDispatcher.REQUEST_SBMISSION_IN,
						RequestSubmissionInboundPortURIdispatcher);
		ArrayList<Pair<String,String>> uriGENERATor = new ArrayList<Pair<String,String>>() ;
		uriGENERATor.add(new Pair<>(RequestNotificationInboundPortURI,java.util.UUID.randomUUID().toString()));

		this.rd = new RequestDispatcher(1, 1,
				"rd",     // uri port notification Controleur PAS DE CONTROLLEUR DANS L'exemple
				uriRD, // liste de uris imposé au Dispatcher a la création
				vm, // liste des uris des VM de base + uri des Port de somision a crée
				uriGENERATor //list des Génerateur de base + uri de larequete associé a la reponse du disâtcher
		);
		this.addDeployedComponent(rd);
		this.rd.toggleTracing();
		this.rd.toggleLogging();
		// --------------------------------------------------------------------
		// Creating the request generator component.
		// --------------------------------------------------------------------
		this.rg = new RequestGenerator(
				"rg",			// generator component URI
				500.0,			// mean time between two requests
				6000000000L,	// mean number of instructions in requests
				RequestGeneratorManagementInboundPortURI,
				RequestSubmissionInboundPortURIdispatcher,
				RequestNotificationInboundPortURI) ;
		this.addDeployedComponent(rg) ;
		// Toggle on tracing and logging in the request generator to
		// follow the submission and end of execution notification of
		// individual requests.
		this.rg.toggleTracing() ;
		this.rg.toggleLogging() ;
		// --------------------------------------------------------------------

		// --------------------------------------------------------------------
		// Creating the integrator component.
		// --------------------------------------------------------------------
		this.integ = new Integrator(
				ComputerServicesInboundPortURI,
				ApplicationVM0ManagementInboundPortURI,
				ApplicationVM1ManagementInboundPortURI,
				RequestGeneratorManagementInboundPortURI) ;
		this.addDeployedComponent(this.integ) ;
		// --------------------------------------------------------------------

		// complete the deployment at the component virtual machine level.
		super.deploy();
	}

	// ------------------------------------------------------------------------
	// Test scenarios and main execution.
	// ------------------------------------------------------------------------

	/**
	 * execute the test application.
	 *
	 * @param args	command line arguments, disregarded here.
	 */
	public static void	main(String[] args)
	{
		// Uncomment next line to execute components in debug mode.
		// AbstractCVM.toggleDebugMode() ;
		try {
			final TestRequestDispatcher trd = new TestRequestDispatcher() ;
			trd.startStandardLifeCycle(10000L) ;
			// Augment the time if you want to examine the traces after
			// the execution of the program.
			Thread.sleep(100000L) ;
			// Exit from Java (closes all trace windows...).
			System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
