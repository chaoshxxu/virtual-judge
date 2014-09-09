package judge.httpclient;

import org.apache.commons.lang3.Validate;

public class HttpBodyValidator implements SimpleHttpResponseValidator {

	private String subString;
	private boolean negate;
	
	public HttpBodyValidator(String subString) {
		this(subString, false);
	}

	public HttpBodyValidator(String subString, boolean negate) {
		this.subString = subString;
		this.negate = negate;
	}

	@Override
	public void validate(SimpleHttpResponse response) throws Exception {
		try {
			Validate.isTrue(response.getBody().contains(subString) ^ negate);
		} catch (Exception e) {
			System.err.println(response.getBody());
			throw new RuntimeException(e);
		}
	}
	
}
