package judge.httpclient;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

public class SimpleHttpResponse {

    private String body;
    private int statusCode;
    private HttpResponse rawResponse;

    public String getBody() {
        return body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Note, when SimpleHttpResponse instance is ready, rawResponse has been
     * disposed. Hence don't read the body of rawResponse.
     * 
     * @return
     */
    public HttpResponse getRawResponse() {
        return rawResponse;
    }

    public SimpleHttpResponse(String body, int statusCode, HttpResponse rawResponse) {
        this.body = body;
        this.statusCode = statusCode;
        this.rawResponse = rawResponse;
    }
    
    public static SimpleHttpResponse build(HttpResponse response, String charset) throws ParseException, IOException {
        String content = EntityUtils.toString(response.getEntity(), charset);
        int statusCode = response.getStatusLine().getStatusCode();
        return new SimpleHttpResponse(content, statusCode, response);
    }

}
