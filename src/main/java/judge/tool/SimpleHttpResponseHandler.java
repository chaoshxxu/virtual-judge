package judge.tool;

public interface SimpleHttpResponseHandler extends Handler<SimpleHttpResponse> {
	
	void handle(SimpleHttpResponse response) throws Exception;

}
