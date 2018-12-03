package RequestDispatcher.DispatcherDataVm;

import fr.sorbonne_u.datacenter.software.interfaces.RequestI;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionOutboundPort;

public class DataVM implements Comparable {
    private String Uri;
    private RequestSubmissionOutboundPort  vmRsop;
    private long charge;
    private long timeSinceModification;


    public DataVM(String avmUri,
                  RequestSubmissionOutboundPort  rsop) throws Exception {
        this.Uri = avmUri;
        this.vmRsop = rsop;
        this.charge =0;
        this.timeSinceModification=1;
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
    public void addCharge(long modif,long currentTime){
        this.charge += charge;

        if (charge < 0) //
            timeSinceModification= currentTime;
    }

    public long getCharge() {
        return charge/timeSinceModification;
    }

    public String getUri() {
        return Uri;
    }

    @Override
    public int compareTo(Object o) {
        return (int) ( ((DataVM) o).getCharge() - this.charge);
    }

    public void shutdown() throws Exception {

        this.vmRsop.unpublishPort() ;
    }

}
