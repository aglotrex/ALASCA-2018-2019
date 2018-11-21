package RequestGenerator;


import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import Request.RequestIP;
import Request.RequestP;
import org.apache.commons.math3.random.RandomDataGenerator;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.datacenter.TimeManagement;
import fr.sorbonne_u.datacenter.software.connectors.RequestSubmissionConnector;
import fr.sorbonne_u.datacenter.software.interfaces.RequestI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestNotificationI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionI;
import fr.sorbonne_u.datacenter.software.ports.RequestNotificationInboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.sorbonne_u.datacenterclient.requestgenerator.interfaces.RequestGeneratorManagementI;
import fr.sorbonne_u.datacenterclient.requestgenerator.ports.RequestGeneratorManagementInboundPort;
import fr.sorbonne_u.datacenterclient.utils.TimeProcessing;

public class				RequestGenerator
        extends		AbstractComponent
        implements	RequestNotificationHandlerI
{
    public static int	DEBUG_LEVEL = 2 ;

    // -------------------------------------------------------------------------
    // Constants and instance variables
    // -------------------------------------------------------------------------

    /** the URI of the component.											*/
    protected final String					rgURI ;
    /** a random number generator used to generate processing times.		*/
    protected RandomDataGenerator			rng ;
    /** a counter used to generate request URI.								*/
    protected int							counter ;
    /** the mean inter-arrival time of requests in ms.						*/
    protected double							meanInterArrivalTime ;
    /** the mean processing time of requests in ms.							*/
    protected long							meanNumberOfInstructions ;

    /** the inbound port provided to manage the component.					*/
    protected RequestGeneratorManagementInboundPort	rgmip ;
    /** the output port used to send requests to the service provider.		*/
    protected RequestSubmissionOutboundPort	rsop ;
    protected String							requestSubmissionInboundPortURI ;
    /** the inbound port receiving end of execution notifications.			*/
    protected RequestNotificationInboundPort	rnip ;
    /** a future pointing to the next request generation task.				*/
    protected Future<?>						nextRequestTaskFuture ;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * create a request generator component.
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	meanInterArrivalTime &gt; 0.0 and meanNumberOfInstructions &gt; 0
     * pre	requestSubmissionOutboundPortURI != null
     * post	true			// no postcondition.
     * </pre>
     *
     * @param rgURI								URI of the request generator component.
     * @param meanInterArrivalTime				mean inter-arrival time of the requests in ms.
     * @param meanNumberOfInstructions			mean number of instructions of the requests in ms.
     * @param managementInboundPortURI			URI of the management inbound port.
     * @param requestSubmissionInboundPortURI	URI of the inbound port to connect to the request processor.
     * @param requestNotificationInboundPortURI	URI of the inbound port to receive notifications of the request execution progress.
     * @throws Exception							<i>todo.</i>
     */
    public				RequestGenerator(
            String rgURI,
            double meanInterArrivalTime,
            long meanNumberOfInstructions,
            String managementInboundPortURI,
            String requestSubmissionInboundPortURI,
            String requestNotificationInboundPortURI
    ) throws Exception
    {
        super(1, 1) ;

        // preconditions check
        assert	meanInterArrivalTime > 0.0 && meanNumberOfInstructions > 0 ;
        assert	managementInboundPortURI != null ;
        assert	requestSubmissionInboundPortURI != null ;
        assert	requestNotificationInboundPortURI != null ;

        // initialization
        this.rgURI = rgURI ;
        this.counter = 0 ;
        this.meanInterArrivalTime = meanInterArrivalTime ;
        this.meanNumberOfInstructions = meanNumberOfInstructions ;
        this.rng = new RandomDataGenerator() ;
        this.rng.reSeed() ;
        this.nextRequestTaskFuture = null ;
        this.requestSubmissionInboundPortURI =
                requestSubmissionInboundPortURI ;

        this.addOfferedInterface(RequestGeneratorManagementI.class) ;
        this.rgmip = new RequestGeneratorManagementInboundPort(
                managementInboundPortURI, this) ;
        this.addPort(this.rgmip) ;
        this.rgmip.publishPort() ;

        this.addRequiredInterface(RequestSubmissionI.class) ;
        this.rsop = new RequestSubmissionOutboundPort(this) ;
        this.addPort(this.rsop) ;
        this.rsop.publishPort() ;

        this.addOfferedInterface(RequestNotificationI.class) ;
        this.rnip =
                new RequestNotificationInboundPort(
                        requestNotificationInboundPortURI, this) ;
        this.addPort(this.rnip) ;
        this.rnip.publishPort() ;

        // post-conditions check
        assert	this.rng != null && this.counter >= 0 ;
        assert	this.meanInterArrivalTime > 0.0 ;
        assert	this.meanNumberOfInstructions > 0 ;
        assert	this.rsop != null && this.rsop instanceof RequestSubmissionI ;
    }

    // -------------------------------------------------------------------------
    // Component life-cycle
    // -------------------------------------------------------------------------

    /**
     * @see fr.sorbonne_u.components.AbstractComponent#start()
     */
    @Override
    public void			start() throws ComponentStartException
    {
        super.start() ;

        try {
            this.doPortConnection(
                    this.rsop.getPortURI(),
                    requestSubmissionInboundPortURI,
                    RequestSubmissionConnector.class.getCanonicalName()) ;
        } catch (Exception e) {
            throw new ComponentStartException(e) ;
        }
    }

    /**
     * @see fr.sorbonne_u.components.AbstractComponent#finalise()
     */
    @Override
    public void			finalise() throws Exception
    {
        this.doPortDisconnection(this.rsop.getPortURI()) ;
        super.finalise() ;
    }

    /**
     * shut down the component, first canceling any future request generation
     * already scheduled.
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	true				// no more preconditions.
     * post	true				// no more postconditions.
     * </pre>
     *
     * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
     */
    @Override
    public void			shutdown() throws ComponentShutdownException
    {
        if (this.nextRequestTaskFuture != null &&
                !(this.nextRequestTaskFuture.isCancelled() ||
                        this.nextRequestTaskFuture.isDone())) {
            this.nextRequestTaskFuture.cancel(true) ;
        }

        try {
            if (this.rsop.connected()) {
                this.rsop.doDisconnection() ;
            }
        } catch (Exception e) {
            throw new ComponentShutdownException(e) ;
        }

        super.shutdown();
    }

    // -------------------------------------------------------------------------
    // Component internal services
    // -------------------------------------------------------------------------

    /**
     * start the generation and submission of requests.
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	true			// no precondition.
     * post	true			// no postcondition.
     * </pre>
     *
     * @throws Exception		<i>todo.</i>
     */
    public void			startGeneration() throws Exception
    {
        if (RequestGenerator.DEBUG_LEVEL == 1) {
            this.logMessage("Request generator " + this.rgURI + " starting.") ;
        }
        this.generateNextRequest() ;
    }

    /**
     * stop the generation and submission of requests.
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	true			// no precondition.
     * post	true			// no postcondition.
     * </pre>
     *
     * @throws Exception		<i>todo.</i>
     */
    public void			stopGeneration() throws Exception
    {
        if (RequestGenerator.DEBUG_LEVEL == 1) {
            this.logMessage("Request generator " + this.rgURI + " stopping.") ;
        }
        if (this.nextRequestTaskFuture != null &&
                !(this.nextRequestTaskFuture.isCancelled() ||
                        this.nextRequestTaskFuture.isDone())) {
            this.nextRequestTaskFuture.cancel(true) ;
        }
    }

    /**
     * return the current value of the mean inter-arrival time used to generate
     * requests.
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	true			// no precondition.
     * post	true			// no postcondition.
     * </pre>
     *
     * @return	the current value of the mean inter-arrival time.
     */
    public double		getMeanInterArrivalTime()
    {
        return this.meanInterArrivalTime ;
    }

    /**
     * set the value of the mean inter-arrival time used to generate requests.
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	true			// no precondition.
     * post	true			// no postcondition.
     * </pre>
     *
     * @param miat	new value for the mean inter-arrival time.
     */
    public void			setMeanInterArrivalTime(double miat)
    {
        assert	miat > 0.0 ;
        this.meanInterArrivalTime = miat ;
    }

    /**
     * generate a new request with some processing time following an exponential
     * distribution and then schedule the next request generation in a delay
     * also following an exponential distribution.
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	true			// no precondition.
     * post	true			// no postcondition.
     * </pre>
     *
     * @throws Exception		<i>todo.</i>
     */
    public void			generateNextRequest() throws Exception
    {
        // generate a random number of instructions for the request.
        long noi =
                (long) this.rng.nextExponential(this.meanNumberOfInstructions) ;
        this.logMessage("sending request");
        RequestP r = new RequestP(this.rgURI + "-" + this.counter++,noi, RequestIP.RequestType.REQUEST_INSTRUSCTION);
        // generate a random delay until the next request generation.
        long interArrivalDelay =
                (long) this.rng.nextExponential(this.meanInterArrivalTime) ;

        if (RequestGenerator.DEBUG_LEVEL == 2) {
            this.logMessage(
                    "Request generator " + this.rgURI +
                            " submitting request " + r.getRequestURI() + " at " +
                            TimeProcessing.toString(System.currentTimeMillis() +
                                    interArrivalDelay) +
                            " with number of instructions " + noi) ;
        }

        // submit the current request.
        this.rsop.submitRequestAndNotify(r) ;
        // schedule the next request generation.
        this.nextRequestTaskFuture =
                this.scheduleTask(
                        new AbstractComponent.AbstractTask() {
                            @Override
                            public void run() {
                                try {
                                    ((RequestGenerator)this.getOwner()).
                                            generateNextRequest() ;
                                } catch (Exception e) {
                                    throw new RuntimeException(e) ;
                                }
                            }
                        },
                        TimeManagement.acceleratedDelay(interArrivalDelay),
                        TimeUnit.MILLISECONDS) ;
    }

    /**
     * process an end of execution notification for a request r previously
     * submitted.
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	r != null
     * post	true			// no postcondition.
     * </pre>
     *
     * @param r	request that just terminated.
     * @throws Exception		<i>todo.</i>
     */
    @Override
    public void			acceptRequestTerminationNotification(RequestI r)
            throws Exception
    {
        assert	r != null ;

        if (RequestGenerator.DEBUG_LEVEL == 2) {
            this.logMessage("Request generator " + this.rgURI +
                    " is notified that request "+ r.getRequestURI() +
                    " has ended.") ;
        }
    }
}
