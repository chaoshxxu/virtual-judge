package judge.httpclient;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpStatus;

public class HttpStatusValidator implements SimpleHttpResponseValidator {

	private int httpStatusCode;

	public HttpStatusValidator(int httpStatusCode) {
		super();
		this.httpStatusCode = httpStatusCode;
	}

	@Override
	public void validate(SimpleHttpResponse response) {
		Validate.isTrue(response.getStatusCode() == httpStatusCode);
	}
	

	/////////////////////////////////////////////////////////////////
	
	public static HttpStatusValidator SC_OK = new HttpStatusValidator(HttpStatus.SC_OK);
	public static HttpStatusValidator SC_MOVED_PERMANENTLY = new HttpStatusValidator(HttpStatus.SC_MOVED_PERMANENTLY);
	public static HttpStatusValidator SC_MOVED_TEMPORARILY = new HttpStatusValidator(HttpStatus.SC_MOVED_TEMPORARILY);

}
