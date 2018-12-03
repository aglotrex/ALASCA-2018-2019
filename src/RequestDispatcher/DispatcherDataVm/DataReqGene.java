package RequestDispatcher.DispatcherDataVm;

import Request.RequestIP;
import fr.sorbonne_u.datacenter.software.interfaces.RequestI;
import fr.sorbonne_u.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionOutboundPort;

public class DataReqGene {
    private String Uri ;
    private RequestNotificationOutboundPort vmRsop;


        public DataReqGene(String DispatcherUri,
                           RequestNotificationOutboundPort  rnop) throws Exception {
            this.Uri = DispatcherUri;
            this.vmRsop = rnop;
        }
        public void terminate () throws Exception {
            vmRsop.doDisconnection();
        }

        public void shutdown() throws Exception {

            this.vmRsop.unpublishPort() ;
        }
        public void notification(RequestIP r) throws Exception {
            vmRsop.notifyRequestTermination(r);
        }
        public String getUri() {
            return Uri;
        }

}
