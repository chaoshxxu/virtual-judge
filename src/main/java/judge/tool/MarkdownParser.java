package judge.tool;

import java.io.UnsupportedEncodingException;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.SimpleHttpResponse;
import judge.httpclient.SimpleHttpResponseMapper;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

public class MarkdownParser {
	
	public static String parse(String markdown) throws UnsupportedEncodingException {
		DedicatedHttpClient client = new DedicatedHttpClient(new HttpHost("api.github.com", 443, "https"));
		return client.post("/markdown/raw?access_token=5363ede91b01d1513781a31ffa3976085c73ea91", new StringEntity(markdown, "UTF-8"), new SimpleHttpResponseMapper<String>() {
			@Override
			public String map(SimpleHttpResponse response) throws Exception {
				Validate.isTrue(response.getStatusCode() == HttpStatus.SC_OK);
				return response.getBody();
			}
		});
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		String md = "Two integer $a$,$b$ ($0<a,b<10$)";
		System.out.println(parse(md));
	}

}
