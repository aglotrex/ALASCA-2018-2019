package Request;

public class RequestP
implements RequestIP
{
	private String uriRQ;
	private String uri_value;
	private long value;
	private RequestType type;

    public RequestP(String uriRQ, String uri_value, long value, RequestType type) {
        this.uriRQ = uriRQ;
        this.uri_value = uri_value;
        this.value = value;
        this.type = type;
    }
    public RequestP(String uri_value, long value, RequestType type) {
        this.uriRQ = java.util.UUID.randomUUID().toString();
        this.uri_value = uri_value;
        this.value = value;
        this.type = type;
    }

    @Override
	public String getRequestURI() {
		return uriRQ;
	}

	@Override
	public long getPredictedNumberOfInstructions() {
		return value;
	}

	@Override
	public String getURI() {
		return uri_value;
	}

	@Override
	public long getValue() {
		return getPredictedNumberOfInstructions();
	}

	@Override
	public RequestType getType() {
		return type;
	}
}