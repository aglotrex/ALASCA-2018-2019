package RequestDispatcher.DispatcherDataVm;

import Request.RequestIP;
import fr.sorbonne_u.datacenter.software.applicationvm.ApplicationVM;
import fr.sorbonne_u.datacenter.software.interfaces.RequestI;
import fr.sorbonne_u.datacenter.software.ports.RequestNotificationInboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionOutboundPort;

import java.util.HashMap;
import java.util.Random;

public class VMdispatcher implements Comparable {
    private String Uri;
    private RequestSubmissionOutboundPort  vmRsop;
    private long charge;


    public VMdispatcher(String avmUri,
                        RequestSubmissionOutboundPort  rsop) throws Exception {
        this.Uri = avmUri;
        this.vmRsop = rsop;
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
    public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
        vmRsop.submitRequestAndNotify(r);
    }

    public void setCharge(long charge) {
        this.charge = charge;
    }

    public long getCharge() {
        return charge;
    }

    public String getUri() {
        return Uri;
    }

    @Override
    public int compareTo(Object o) {
        return (int) ( ((VMdispatcher) o).getCharge() - this.charge);
    }

    public void shutdown() throws Exception {

        this.vmRsop.unpublishPort() ;
    }

}
