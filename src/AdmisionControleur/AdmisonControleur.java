package AdmisionControleur;

import Request.RequestP;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;
import fr.sorbonne_u.datacenter.hardware.computers.Computer;
import fr.sorbonne_u.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.sorbonne_u.datacenter.software.connectors.RequestSubmissionConnector;
import fr.sorbonne_u.datacenter.software.interfaces.RequestI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionOutboundPort;

import java.util.*;

public class AdmisonControleur extends AbstractComponent  implements OfferedI , RequiredI, RequestSubmissionHandlerI {



    /** Inbound port offrant les services de l'admission contrôlleur */


    private String uriControleur;

    private ArrayList<RequestSubmissionInboundPort> UriRequestDispatcher;

    //resevoir le demande
    protected RequestSubmissionInboundPort rsip;
    // evoyer l'uri de notification
    protected RequestSubmissionOutboundPort rsop ;


    private ArrayList<ApplicationVMManagementOutboundPort> portxVMmanage;

    private Set<Integer> admissibleFrequencies;
    private Map<Integer,Integer> processingPower;
    private ArrayList<Computer> ordis;

    public AdmisonControleur(int nbThreads, int nbSchedulableThreads) {
        super(nbThreads, nbSchedulableThreads);
    }

    @Override
    public void acceptRequestSubmission(RequestI r) throws Exception {
       /** String sumisionUriRequestGenerator = ((RequestUriI) r).getSubmissionURI();
        String notificationURIRequestGenerator = ((RequestUriI) r).getSubmissionURI();

        String notificqationUriRequestDispatcher = this.UriRequestDispatcher.get(0).getPortURI();



        this.doPortConnection(rsop.getPortURI()
                ,notificqationUriRequestDispatcher,
                RequestSubmissionConnector.class.getCanonicalName());
        rsop.submitRequest(new RequestP("",notificationURIRequestGenerator,));
        rsop.doDisconnection();
        this.doPortConnection(rsop.getPortURI()
                ,sumisionUriRequestGenerator,
                RequestSubmissionConnector.class.getCanonicalName());

        rsop.submitRequest(new RequestP(notificqationUriRequestDispatcher,""));
        rsop.doDisconnection();
        **/

    }

    @Override
    public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {

    }


    // public  AdmisonControleur(int nbThreads, int nbSchedulableThreads, int numberOfProcessor, int numberOfCore);
    /**
        super(nbThreads, nbSchedulableThreads);

        admissibleFrequencies = new HashSet<Integer>() ;
        admissibleFrequencies.add(1500) ;	// Cores can run at 1,5 GHz
        admissibleFrequencies.add(3000) ;	// and at 3 GHz
        processingPower = new HashMap<Integer,Integer>() ;
        processingPower.put(1500, 1500000) ;	// 1,5 GHz executes 1,5 Mips
        processingPower.put(3000, 3000000) ;	// 3 GHz executes 3 Mips
        for ()
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
                ApplicationVMManagementInboundPortURI,
                RequestSubmissionInboundPortURI,
                RequestNotificationInboundPortURI) ;
        this.addDeployedComponent(this.vm) ;
        // Toggle on tracing and logging in the application virtual machine to
        // follow the execution of individual requests.
        this.vm.toggleTracing() ;
        this.vm.toggleLogging() ;
        // --------------------------------------------------------------------
        public void addRequestSource(
                Integer howManyAVMsOnStartup,
                HashMap<RequestDispatcher.PortTypeRequestDispatcher, String> RD_uris,
                A<RGPortTypes, String> RG_uris,
                String rg_monitor_in) throws Exception;
    }

    String computerURI = "computer0" ;
     ;


    /**
     * Supprime le RequestDispatcher_old associé à l'URI du port donné en paramètre.
     * @param 	RD_RequestSubmissionInboundPortUri 	Uri du port du RequestDispatcher_old à supprimer.
     * @throws 	Exception
     */

    //public void removeRequestSource(String requestGeneratorURI) throws Exception;

    /**
     * Retourne les outbound ports de management des machines virtuelles d'application.
     * @throws Exception
     * @retunr la liste des outbound ports de management des machines virtuelles d'application.
     */
    /**
    public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts()
            throws Exception;
**/
    /**
     * Crée un composant RequestDispatcher_old avec les URIs données en paramètre.
     * @param 	num_rd		Numéro du RequestDispatcher_old.
     * @param	rg_uris		URIs du RequestDispatcher_old.
     * @param 	ac_uris		Uris du Composant AdmissionControler.
     * @throws 	Exception
     * @retunr 	la liste des outbound ports de management des machines virtuelles d'application.
     */
    /**
    public void createNewRequestDispatcher(
            Integer num_rd,
            HashMap<RGPortTypes, String> rg_uris,
            HashMap<ACPortTypes, String> ac_uris) throws Exception; **/
}
