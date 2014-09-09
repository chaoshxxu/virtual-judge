package judge.httpclient;


public interface SimpleHttpResponseValidator {

	void validate(SimpleHttpResponse response) throws Exception;
	
	
	///////////////////////////////////////////////////////////////
	
	
	final SimpleHttpResponseValidator DUMMY_VALIDATOR = new SimpleHttpResponseValidator() {
		@Override
		public void validate(SimpleHttpResponse response) {
			// Validate nothing. Pass all the time.
		}
	};
	
}
