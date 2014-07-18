package judge.tool;

public class SimpleHttpResponse {

	private String body;
	private int statusCode;

	public String getBody() {
		return body;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public SimpleHttpResponse(String body, int statusCode) {
		super();
		this.body = body;
		this.statusCode = statusCode;
	}

}
