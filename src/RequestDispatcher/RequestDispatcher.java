package RequestDispatcher;

import Handler.RequestNotificationHandlerIP;
import Handler.RequestSubmissionHandlerIP;
import Request.RequestIP;
import Request.RequestP;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.datacenter.software.applicationvm.ApplicationVM;
import fr.sorbonne_u.datacenter.software.connectors.RequestSubmissionConnector;
import fr.sorbonne_u.datacenter.software.interfaces.*;
import fr.sorbonne_u.datacenter.software.ports.RequestNotificationInboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionOutboundPort;
import RequestDispatcher.DispatcherDataVm.VMdispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RequestDispatcher extends AbstractComponent implements RequestSubmissionHandlerIP, RequestNotificationHandlerIP {
    protected String UriVM;
    protected String UriCA;

    protected ArrayList<VMdispatcher> VMlist;
    // intefaces vm
    protected RequestSubmissionOutboundPort rSoutP_VM;
    protected RequestNotificationInboundPort rNinP_VM;


    // interfaces Generator
    protected RequestSubmissionInboundPort rSinP_GN;
    protected RequestNotificationOutboundPort rNoutP_GN;

    protected RequestNotificationOutboundPort ControleurDadmision;
    protected Thread sortThread;

    protected Lock verou = new ReentrantLock();

    public static enum	PortTypeRequestDispatcher{
        REQUEST_SUBMISSION_OUT_VM,
        REQUEST_NOTIFICATION_IN_VM,

        REQUEST_SUBMISSION_IN_AC,
        REQUEST_NOTIFICATION_OUT_AC,

        REQUEST_DISPACTCHER_GENERAOTOR_URI,
        REQUEST_VIRTUAL_MACHINE_URI;
    }

    @Override
    public void acceptRequestSubmission(RequestIP r) throws Exception {
        switch (r.getType()) {
            case ADD_VM:
                this.registerVM(r.getURI());
                break;
            case ADD_GENERATOR:
                String UriPort = this.registerGenerator(r.getURI());
                ControleurDadmision.notifyRequestTermination(new RequestP(UriPort,0,RequestIP.RequestType.REPONSE_DISPATCHER));
                break;
            case REQUEST_INSTRUSCTION:

                VMdispatcher vm = VMlist.remove(0);
                this.logMessage(this.UriVM + " is using " + vm.getUri());
                vm.acceptRequestSubmission(r, UriVM);
                VMlist.add(vm);

                break;
            case REPONSE_CHARGE:
                for(VMdispatcher vm1 : this.VMlist) {
                    if (vm1.getUri().equals(r.getURI())) {
                        vm1.setCharge(r.getValue());
                        break;
                    }
                }
                break;
            case REPONSE_INTSTRUCTION:
                //notification terminaison
                break;
        }

    }

    @Override
    public void acceptRequestSubmissionAndNotify(RequestIP r) throws Exception {
        // ca veut dire que la request a le même URI

        if(r.getType().equals(RequestIP.RequestType.REQUEST_INSTRUSCTION)) {
            System.out.println("reception soumision requete");
            if (VMlist.isEmpty()) {
                logMessage(" pas de VM");
                return;
            }
            VMdispatcher vm = VMlist.remove(0);

            this.logMessage(this.UriVM + " is using " + vm.getUri());

            vm.acceptRequestSubmissionAndNotify(r, UriVM);

            VMlist.add(vm);
        }
        else{
            this.acceptRequestSubmission(r);
        }


    }



    public RequestDispatcher(int nbThreads, int nbSchedulableThreads,
                             String uri, ArrayList<VMdispatcher> vmUri,
                             String rSinP, String rNinP) throws Exception {
        super(nbThreads, nbSchedulableThreads);
        UriVM = uri;
        VMlist = vmUri;

        this.addRequiredInterface(RequestSubmissionI.class) ;
        this.rSoutP_VM = new RequestSubmissionOutboundPort(this) ;
        this.addPort(this.rSoutP_VM) ;
        this.rSoutP_VM.publishPort() ;

        this.rSinP_GN = new RequestSubmissionInboundPort(rSinP, this);
        this.addPort(this.rSinP_GN);
        this.rSinP_GN.publishPort();
        this.addOfferedInterface(RequestSubmissionInboundPort.class);

        this.addRequiredInterface(RequestNotificationI.class) ;
        this.rNoutP_GN = new RequestNotificationOutboundPort(this) ;
        this.addPort(this.rNoutP_GN) ;
        this.rNoutP_GN.publishPort() ;

        this.rNinP_VM = new RequestNotificationInboundPort(rNinP, this);
        this.addPort(this.rNinP_VM);
        this.rNinP_VM.publishPort();
        this.addOfferedInterface(RequestNotificationInboundPort.class);

        sortThread = new Thread(new Thread(new Runnable() {
            @Override
            public void run() {
                int indice = 0;
                while (true) {
                    while (indice <= VMlist.size()) {
                        if (indice <= VMlist.size())
                            indice = 0;
                    }
                    //verou.lock();
                    Collections.sort(VMlist);
                    //verou.unlock();

                    try {
                        wait(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }));
    }
    public void registerVM(String avmUri) throws Exception {


        HashMap<ApplicationVM.ApplicationVMPortTypes, String> avmURIs = new HashMap<>();
        for ( VMdispatcher vmD : VMlist ) {
            if (vmD.getUri().equals(avmUri)){
                this.logMessage("Register AVM : alredy in list.");
                return;
            }
        }


        RequestSubmissionOutboundPort rsopVM = new RequestSubmissionOutboundPort(this);
        this.addPort(rsopVM);
        rsopVM.publishPort();
        this.doPortConnection(rsopVM.getPortURI(),avmUri,
                RequestSubmissionConnector.class.getCanonicalName() );




        RequestNotificationInboundPort rnipVM = new RequestNotificationInboundPort(this);
        this.addPort(rnipVM);
        rnipVM.publishPort();

        this.doPortConnection(rnipVM.getPortURI(),avmUri,
                RequestNotificationHandlerI.class.getCanonicalName() );


        VMdispatcher vm = new VMdispatcher(avmUri,rsopVM,rnipVM,avmURIs);
        VMlist.add(vm);

        this.logMessage(this.UriVM + " : " + avmURIs + " has been added.");

    }
    public void registerVM(HashMap<ApplicationVM.ApplicationVMPortTypes, String> avmURIs,
                           Class<?> vmInterface) throws Exception {



        this.logMessage("Register avm : " + avmURIs);

        String avmUri = avmURIs.get(ApplicationVM.ApplicationVMPortTypes.INTROSPECTION);


        for ( VMdispatcher vmD : VMlist ) {
            if (vmD.getUri().equals(avmUri)){
                this.logMessage("Register AVM : alredy in list.");
                return;
            }
        }


        RequestSubmissionOutboundPort rsopVM = new RequestSubmissionOutboundPort(this);
        this.addPort(rsopVM);
        rsopVM.publishPort();
        this.doPortConnection(rsopVM.getPortURI(),avmUri,
                RequestSubmissionConnector.class.getCanonicalName() );


        RequestNotificationInboundPort rnipVM = new RequestNotificationInboundPort(this);
        this.addPort(rnipVM);
        rnipVM.publishPort();

        this.doPortConnection(rnipVM.getPortURI(),avmUri,
                RequestNotificationHandlerI.class.getCanonicalName() );


        VMdispatcher vm = new VMdispatcher(avmUri,rsopVM,rnipVM,avmURIs);
        VMlist.add(vm);

        this.logMessage(this.UriVM + " : " + avmURIs + " has been added.");

    }
    @Override
    public void start() throws ComponentStartException {
        super.start();
        try {

            this.doPortConnection(this.rSoutP_VM.getPortURI(),this.UriVM,
                    RequestSubmissionConnector.class.getCanonicalName());
            //this.doPortConnection(this.rNoutP_GN,this.UriCA, RequestSubmissionConnector.class.getCanonicalName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute() throws Exception {
        super.execute();
    }

    @Override
    public void finalise() throws Exception {
        this.doPortDisconnection(this.UriVM);
        this.rNoutP_GN.doDisconnection();
        this.rSinP_GN.doDisconnection();
        this.rSoutP_VM.doDisconnection();
        this.rNinP_VM.doDisconnection();
        for (VMdispatcher vm : VMlist) {
            vm.terminate();
        }
        super.finalise();
    }

    @Override
    public void shutdown() throws ComponentShutdownException {

        try {
            this.rSoutP_VM.unpublishPort() ;
            this.rNinP_VM.unpublishPort() ;
            this.rSinP_GN.unpublishPort();
            this.rNoutP_GN.unpublishPort();
            for (VMdispatcher vm : VMlist) {
                vm.shutdown();
            }
        } catch (Exception e) {
            throw new ComponentShutdownException(e) ;
        }
        super.shutdown();
    }

    @Override
    public void shutdownNow() throws ComponentShutdownException {

        super.shutdownNow();
    }

    @Override
    public boolean isInitialised() {
        return super.isInitialised();
    }


    @Override
    public void acceptRequestTerminationNotification(RequestI r) throws Exception {
        String uriVM = ((RequestUriI) r).getSubmissionURI();

        for ( VMdispatcher vmD : this.VMlist ) {
            if( vmD.getUri().equals(uriVM)){
                vmD.acceptRequestTerminationNotification(r);
                return;
            }

        }
    }

}
