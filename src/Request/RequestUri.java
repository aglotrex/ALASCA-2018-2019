package Request;

public class RequestUri
implements RequestUriI
{
	// ------------------------------------------------------------------------
	// Constants and instance variables
	// ------------------------------------------------------------------------

	private static final long serialVersionUID = 1L ;
	protected final String	requestURI ;
	protected final String	SubmissionURI ;
	protected final String	NotificationURI ;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public RequestUri(String	SubmissionURI,String	NotificationURI )
	{
		super() ;
		this.SubmissionURI = SubmissionURI ;
		this.NotificationURI = NotificationURI;
		this.requestURI = java.util.UUID.randomUUID().toString() ;
	}

	public RequestUri(
		String uri,
		String	SubmissionURI,
		String	NotificationURI
		)
		{
		super() ;

		this.SubmissionURI = SubmissionURI ;
		this.NotificationURI = NotificationURI;
		this.requestURI = uri ;
	}


	@Override
	public String getRequestURI() {
		return this.requestURI;
	}

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestI#getPredictedNumberOfInstructions()
	 */
	@Override

	public long		getPredictedNumberOfInstructions()
	{
		return 0;
	}

	@Override
	public String getSubmissionURI() {
		return this.SubmissionURI;
	}

	@Override
	public String getNotificationURI() {
		return this.NotificationURI;
	}
}