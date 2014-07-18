package judge.tool;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class DedicatedHttpClient {

	private HttpHost host;
	private HttpContext context;
	private HttpClient delegate;
	
	public DedicatedHttpClient(HttpHost host, HttpContext context, HttpClient delegate) {
		super();
		this.host = host;
		this.context = context;
		this.delegate = delegate;
	}
	
	public DedicatedHttpClient(HttpHost host, HttpClient delegate) {
		super();
		this.host = host;
		this.context = getNewContext();
		this.delegate = delegate;
	}
	
	private HttpContext getNewContext() {
		CookieStore cookieStore = new BasicCookieStore();
		HttpContext context = new BasicHttpContext();
		context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		return context;
	}

	/**
	 * Use case: when content is retrieved from the response normally, we take
	 * the operation as successful
	 * 
	 * @param request
	 * @return
	 */
	public SimpleHttpResponse execute(final HttpRequest request) {
		try {
			// It assumes underlying http client will release resource
			// afterwards.
			return delegate.execute(host, request, new ResponseHandler<SimpleHttpResponse>() {
				@Override
				public SimpleHttpResponse handleResponse(HttpResponse response) {
					try {
						return build(response);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}, context);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Use case: when content is retrieved from the response normally, it's
	 * possible that the operation fails due to unexpected content from
	 * response. Hence, we reserve the delegate's chance of retrying. 
	 * 
	 * @param request
	 * @param handler
	 */
	public void execute(final HttpRequest request, final SimpleHttpResponseHandler handler) {
		try {
			// It assumes underlying http client will release resource
			// afterwards.
			delegate.execute(host, request, new ResponseHandler<Object>() {
				@Override
				public Object handleResponse(HttpResponse response) {
					try {
						handler.handle(build(response));
						return null;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}, context);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public SimpleHttpResponse get(String url) {
		return execute(new HttpGet(url));
	}
	
	public void get(String url, SimpleHttpResponseHandler handler) {
		execute(new HttpGet(url), handler);
	}
	
	public SimpleHttpResponse post(String url) {
		return execute(new HttpPost(url));
	}
	
	public void post(String url, SimpleHttpResponseHandler handler) {
		execute(new HttpPost(url), handler);
	}
	
	public SimpleHttpResponse post(String url, String payload) {
		try {
			HttpPost post = new HttpPost(url);
			post.setEntity(new StringEntity(payload));
			return execute(post);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public void post(String url, String payload, SimpleHttpResponseHandler handler) {
		try {
			HttpPost post = new HttpPost(url);
			post.setEntity(new StringEntity(payload));
			execute(post, handler);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public SimpleHttpResponse post(String url, HttpEntity entity) {
		HttpPost post = new HttpPost(url);
		post.setEntity(entity);
		return execute(post);
	}

	public void post(String url, HttpEntity entity, SimpleHttpResponseHandler handler) {
		HttpPost post = new HttpPost(url);
		post.setEntity(entity);
		execute(post, handler);
	}
	
	private SimpleHttpResponse build(HttpResponse response) throws ParseException, IOException {
		String content = EntityUtils.toString(response.getEntity());
		int statusCode = response.getStatusLine().getStatusCode();
		return  new SimpleHttpResponse(content, statusCode);
	}

}
