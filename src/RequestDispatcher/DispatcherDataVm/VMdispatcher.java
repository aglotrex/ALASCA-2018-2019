package RequestDispatcher.DispatcherDataVm;

import fr.sorbonne_u.datacenter.software.applicationvm.ApplicationVM;
import fr.sorbonne_u.datacenter.software.interfaces.RequestI;
import fr.sorbonne_u.datacenter.software.ports.RequestNotificationInboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionOutboundPort;

import java.util.HashMap;
import java.util.Random;

public class VMdispatcher implements Comparable {
    private String Uri;
    private RequestSubmissionOutboundPort  vmRsop;
    private RequestNotificationInboundPort vmRnip;
    private HashMap<ApplicationVM.ApplicationVMPortTypes, String> portTypes;
    private Integer charge;


    public VMdispatcher(String avmUri,
                        RequestSubmissionOutboundPort  rsop,
                        RequestNotificationInboundPort rnip,

                        HashMap<ApplicationVM.ApplicationVMPortTypes, String> vmportTypes){
        this.Uri = avmUri;
        this.vmRsop = rsop;
        this.vmRnip = rnip;
        this.portTypes = vmportTypes;
        this.charge =0;
    }
    public void terminate () throws Exception {
        vmRsop.doDisconnection();
    }
    public void acceptRequestSubmission(RequestI r, String controleurURI) throws Exception {
        if (!vmRsop.connected()) {
            throw new Exception(controleurURI + " can't conect to vm.");
        }
        vmRsop.submitRequest(r);
    }
    public void acceptRequestSubmissionAndNotify(RequestI r, String controleurURI) throws Exception {
        if (!vmRsop.connected()) {
            throw new Exception(controleurURI + " can't conect to vm.");
        }
        vmRsop.submitRequestAndNotify(r);
    }
    public void acceptRequestTerminationNotification(RequestI r) throws Exception {

        vmRnip.notifyRequestTermination(r);
    }

    public Integer getCharge() {
        return charge;
    }
    public void calcCharge(){
        //TODO
        Random a = new Random(100);
        charge = (Integer) a.nextInt();
    }

    public String getUri() {
        return Uri;
    }

    @Override
    public int compareTo(Object o) {
        return ((VMdispatcher)o).getCharge().compareTo(this.charge) ;
    }

    public void shutdown() throws Exception {

        this.vmRsop.unpublishPort() ;
        this.vmRnip.unpublishPort() ;

    }
}
