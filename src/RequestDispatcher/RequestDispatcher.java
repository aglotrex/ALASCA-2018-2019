package RequestDispatcher;

import Handler.RequestNotificationHandlerIP;
import Handler.RequestSubmissionHandlerIP;
import Request.RequestIP;
import Request.RequestP;
import RequestDispatcher.DispatcherDataVm.DataReqGene;
import RequestDispatcher.DispatcherDataVm.DataRequest;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.datacenter.software.connectors.RequestSubmissionConnector;
import fr.sorbonne_u.datacenter.software.interfaces.*;
import fr.sorbonne_u.datacenter.software.ports.RequestNotificationInboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionOutboundPort;
import RequestDispatcher.DispatcherDataVm.DataVM;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class RequestDispatcher extends AbstractComponent implements RequestSubmissionHandlerIP, RequestNotificationHandlerIP {
    protected String Uri;

    protected String UriAdmissionControleur;

    //rececption of
    protected RequestNotificationInboundPort reqNotifIN_Port;
    protected RequestSubmissionInboundPort   reqSubmiIN_Port;

    protected ArrayList<DataVM> VMlist;
    protected ArrayList<DataReqGene> ReqGenList;

    protected ArrayList<String> initialUriVM;
    protected ArrayList<String> initialUriReqGen;


    protected RequestNotificationOutboundPort rNoutP_AC;

    protected HashMap<String, DataRequest> requestSubmision;


    protected ArrayList<Pair<Long,Long>> tempsReponseRequete;

    protected Thread sortThread;

    public static enum	PortTypeRequestDispatcher{
        DISPATCHER_URI,

        REQUEST_NOTIFICATION_IN,
        REQUEST_SBMISSION_IN,

        REQUEST_SUBMISSION_OUT_VM,
        REQUEST_NOTIFICATION_OUT_GN,
        REQUEST_NOTIFICATION_OUT_AC;
    }

    @Override
    public void acceptRequestSubmission(RequestIP r) throws Exception {

        switch (r.getType()) {
            case ADD_VM:
                this.registerVM(r.getURI());
                break;
            case ADD_GENERATOR:
                this.registerGenerator(r.getURI());
                break;
            case REQUEST_INSTRUSCTION:
                this.logMessage(this.Uri + " dispatcher receve instruction ");
                DataReqGene reqG = null;
                for (DataReqGene reqGtmp :ReqGenList ) {
                    if( reqGtmp.getUri().equals(r.getURI())) {
                        reqG = reqGtmp;
                        break;
                    }
                }
                if (reqG == null)
                    return;

                DataVM vm = VMlist.remove(0);

                DataRequest dataREQ = new DataRequest(reqG,vm,true,r.getValue());
                this.requestSubmision.put(r.getRequestURI(),dataREQ);
                vm.acceptRequestSubmission(r, Uri);
                VMlist.add(vm);
                break;


            case REMOVE_VM:
                DataVM supp=null;
                for(DataVM vmD : VMlist) {
                    if (vmD.getUri().equals(r.getURI())) {
                        supp = vmD;
                        break;
                    }
                }
                VMlist.remove(supp);
                supp.terminate();
                break;
            case REMOVE_GENERATOR:
                DataReqGene suppGE = null;
                for(DataReqGene geD : this.ReqGenList) {
                    if (geD.getUri().equals(r.getURI())) {
                        suppGE = geD;
                        break;
                    }
                }
                ReqGenList.remove(suppGE);
                suppGE.terminate();
                break;
        }

    }



    @Override
    public void acceptRequestSubmissionAndNotify(RequestIP r) throws Exception {
        acceptRequestSubmission(r);

        switch (r.getType()) {
            case ADD_VM:

                rNoutP_AC.notifyRequestTermination(new RequestP(r.getRequestURI(),
                                                                this.reqNotifIN_Port.getPortURI(),
                                                                0,
                                                                RequestIP.RequestType.ADD_VM));
                break;
            case ADD_GENERATOR:
            case REQUEST_INSTRUSCTION:
                break;

            case REMOVE_VM:
                rNoutP_AC.notifyRequestTermination(new RequestP(r.getRequestURI(),this.Uri,0,
                                                                RequestIP.RequestType.REMOVE_VM));

                break;
            case REMOVE_GENERATOR :
                rNoutP_AC.notifyRequestTermination(new RequestP(r.getRequestURI(),this.Uri,0,
                                                                RequestIP.RequestType.REMOVE_GENERATOR));
                break;
        }


    }

    public String uriGenerator(String uri){
        if (uri == null)
            return java.util.UUID.randomUUID().toString();
        return uri;
    }

    public RequestDispatcher(int nbThreads, int nbSchedulableThreads,
                             String uriCA,      // uri port notification Controleur
                             HashMap<PortTypeRequestDispatcher,String> uriDispatcher, // liste de uris imposé au Dispatcher a la création
                             ArrayList<String> uriVMs, // liste des uris des VM de base + uri des Port de somision a crée
                             ArrayList<String> urireqG //list des Génerateur de base + uri de larequete associé a la reponse du disâtcher
                            ) throws Exception {
        super(nbThreads, nbSchedulableThreads);
        this.UriAdmissionControleur = uriCA;

        this.Uri = uriGenerator(uriDispatcher.get(PortTypeRequestDispatcher.DISPATCHER_URI));

        this.addRequiredInterface(RequestSubmissionI.class) ;
        this.addRequiredInterface(RequestNotificationI.class) ;

        this.addOfferedInterface(RequestSubmissionInboundPort.class);
        this.addOfferedInterface(RequestNotificationInboundPort.class);

        this.initialUriVM     = uriVMs;
        this.initialUriReqGen = urireqG;
        this.VMlist = new ArrayList<>();
        this.ReqGenList = new ArrayList<>();

        String reqNotifIN_Port_URI = uriGenerator( uriDispatcher.get(PortTypeRequestDispatcher.REQUEST_NOTIFICATION_IN));
        this.reqNotifIN_Port = new RequestNotificationInboundPort(reqNotifIN_Port_URI,this);
        this.addPort(reqNotifIN_Port);
        this.reqNotifIN_Port.publishPort() ;

        String reqSubmiIN_Port_URI = uriGenerator( uriDispatcher.get(PortTypeRequestDispatcher.REQUEST_SBMISSION_IN));
        this.reqSubmiIN_Port = new RequestSubmissionInboundPort(reqSubmiIN_Port_URI,this);
        this.addPort(reqSubmiIN_Port);
        this.reqSubmiIN_Port.publishPort() ;

        String reqNotifOUT_AC_Port_URI = uriGenerator( uriDispatcher.get(PortTypeRequestDispatcher.REQUEST_NOTIFICATION_OUT_AC));
        this.rNoutP_AC = new RequestNotificationOutboundPort(reqNotifOUT_AC_Port_URI,this) ;
        this.addPort(this.rNoutP_AC) ;
        this.rNoutP_AC.publishPort() ;


        sortThread = new Thread(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int indice = 0;
                    while (true) {
                        for(DataVM vm : VMlist){
                            String requestUri = java.util.UUID.randomUUID().toString();

                            //vm.acceptRequestSubmissionAndNotify(new RequestP(requestUri,null,0,
                            //                RequestIP.RequestType.REQUEST_CHARGE));
                            //TODO addapté la VM

                        }
                        Collections.sort(VMlist);

                        wait(5000);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }
    public void registerVM(String avmUri) throws Exception {


        for ( DataVM vmD : VMlist ) {
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



        DataVM vm = new DataVM(avmUri,rsopVM);
        VMlist.add(vm);

        this.logMessage( rsopVM.getPortURI() + " has been added.");


    }
    private void registerGenerator(String reqGuri) throws Exception {

        for ( DataReqGene reqG : ReqGenList) {
            if (reqG.getUri().equals(reqGuri)){
                this.logMessage("Register AVM : alredy in list.");
                return;
            }
        }


        RequestNotificationOutboundPort rnopReqG = new RequestNotificationOutboundPort(this);
        this.addPort(rnopReqG);
        rnopReqG.publishPort();
        this.doPortConnection(rnopReqG.getPortURI(),reqGuri,
                RequestSubmissionConnector.class.getCanonicalName() );



        DataReqGene reqG = new DataReqGene(reqGuri,rnopReqG);
        ReqGenList.add(reqG);

        this.logMessage( rnopReqG.getPortURI() + " has been added.");

       //TODO remetre enleve pour Test
        //  rNoutP_AC.notifyRequestTermination(new RequestP(uriRequest,null,0,RequestIP.RequestType.REPONSE_DISPATCHER));

    }
    @Override
    public void start() throws ComponentStartException {
        super.start();
        try {
            //TODO a déparentéser utilise pour l'exemple
            // this.doPortConnection(this.rNoutP_AC.getPortURI(),this.UriAdmissionControleur,
            //       RequestSubmissionConnector.class.getCanonicalName());

            for(String vmUri : this.initialUriVM) {
                this.registerVM(vmUri);
                this.logMessage("add VM : "+vmUri);
            }
            for(String reqGenUri : this.initialUriReqGen) {
                this.registerGenerator(reqGenUri);
                this.logMessage("add request generator with botification port URI = " + reqGenUri);
            }


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
       // this.doPortDisconnection(this.Uri);
        //this.reqNotifIN_Port.doDisconnection();
        //this.reqSubmiIN_Port.doDisconnection();
        //this.rNoutP_AC.doDisconnection();
        for (DataVM vm : VMlist)
            vm.terminate();
        for (DataReqGene reqG : this.ReqGenList)
            reqG.terminate();

        super.finalise();
    }

    @Override
    public void shutdown() throws ComponentShutdownException {

        try {
            //this.doPortDisconnection(this.Uri);
            this.reqNotifIN_Port.unpublishPort();
            this.reqSubmiIN_Port.unpublishPort();
            //this.rNoutP_AC.unpublishPort() ;
            for (DataVM vm : VMlist)
                vm.shutdown();
            for (DataReqGene reqG : this.ReqGenList)
                reqG.shutdown();

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
    public void acceptRequestTerminationNotification(RequestIP r) throws Exception {
        switch (r.getType()) {

            case REPONSE_INTSTRUCTION:
                DataRequest reqD = this.requestSubmision.get(r.getRequestURI());

                if (reqD == null)
                    return;
                this.requestSubmision.remove(r.getRequestURI());

                this.tempsReponseRequete.add(reqD.reception(r));


                this.logMessage("Reponce from VM receve reponce for  URI = "+r.getRequestURI());
                break;

        }

    }
    public long tempsMoyen(){
        return this.tempsMoyen((long) 0);
    }
    public long tempsMoyen(long temps) {

        long acc = 0;
        long nb = 0;
        for (Pair<Long, Long> temp : tempsReponseRequete)
            if (temp.getFirst() > temps) {
                acc += temp.getSecond();
                nb++;
            }
        if (nb<=0)
            return acc;
        return (acc /  nb);

    }

}
