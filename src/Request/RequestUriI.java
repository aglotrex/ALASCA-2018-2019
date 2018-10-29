package Request;

import fr.sorbonne_u.datacenter.software.interfaces.RequestI;

import java.io.Serializable;

/**
     * The interface <code>RequestI</code> must be implemented by requests
     * submitted to a <code>TaskDispatcher</code> for execution as a job for
     * an application running on the data center.
     *
     * <p><strong>Description</strong></p>
     *
     * Requests are simulated by a fixed predicted number of instructions that will
     * have to be executed to complete the request.
     *
     * <p>Created on : April 9, 2015</p>
     *
     * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
     */
    public interface RequestUriI
            extends Serializable , RequestI
    {


        public String getSubmissionURI() ;
        public String getNotificationURI() ;
    }

