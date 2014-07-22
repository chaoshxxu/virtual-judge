package judge.tool;

import java.io.IOException;

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
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class DedicatedHttpClient {

	private HttpHost host;
	private HttpContext context;
	private HttpClient delegate;
	private String charset = "UTF-8";
	
	public DedicatedHttpClient(HttpHost host, HttpContext context) {
		super();
		this.host = host;
		this.context = context;
		this.delegate = MultipleProxyHttpClientFactory.getInstance(host.getHostName());
	}
	
	public DedicatedHttpClient(HttpHost host) {
		super();
		this.host = host;
		this.context = getNewContext();
		this.delegate = MultipleProxyHttpClientFactory.getInstance(host.getHostName());
	}
	
	public DedicatedHttpClient(HttpHost host, HttpContext context, String charset) {
		this(host, context);
		this.charset = charset;
	}
	
	public DedicatedHttpClient(HttpHost host, String charset) {
		this(host);
		this.charset = charset;
	}
	
	///////////////////////////////////////////////////////////

	public SimpleHttpResponse execute(final HttpRequest request) {
		try {
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
	
	public <T> T execute(final HttpRequest request, final SimpleHttpResponseMapper<T> mapper) {
		try {
			return delegate.execute(host, request, new ResponseHandler<T>() {
				@Override
				public T handleResponse(HttpResponse response) {
					try {
						SimpleHttpResponse simpleHttpResponse = build(response);
						return mapper.map(simpleHttpResponse);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}, context);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void execute(final HttpRequest request, final SimpleHttpResponseHandler handler) {
		try {
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
	
	///////////////////////////////////////////////////////////
	
	public SimpleHttpResponse get(String url) {
		return execute(new HttpGet(url));
	}
	
	public <T> T get(String url, SimpleHttpResponseMapper<T> mapper) {
		return execute(new HttpGet(url), mapper);
	}
	
	public void get(String url, SimpleHttpResponseHandler handler) {
		execute(new HttpGet(url), handler);
	}
	
	///////////////////////////////////////////////////////////
	
	public SimpleHttpResponse post(String url) {
		return execute(new HttpPost(url));
	}
	
	public <T> T post(String url, SimpleHttpResponseMapper<T> mapper) {
		return execute(new HttpPost(url), mapper);
	}
	
	public void post(String url, SimpleHttpResponseHandler handler) {
		execute(new HttpPost(url), handler);
	}
	
	///////////////////////////////////////////////////////////
	
	public SimpleHttpResponse post(String url, HttpEntity entity) {
		HttpPost post = new HttpPost(url);
		post.setEntity(entity);
		return execute(post);
	}

	public <T> T post(String url, HttpEntity entity, SimpleHttpResponseMapper<T> mapper) {
		HttpPost post = new HttpPost(url);
		post.setEntity(entity);
		return execute(post, mapper);
	}

	public void post(String url, HttpEntity entity, SimpleHttpResponseHandler handler) {
		HttpPost post = new HttpPost(url);
		post.setEntity(entity);
		execute(post, handler);
	}
	
	///////////////////////////////////////////////////////////
	
	private SimpleHttpResponse build(HttpResponse response) throws ParseException, IOException {
		String content = EntityUtils.toString(response.getEntity(), charset);
		int statusCode = response.getStatusLine().getStatusCode();
		return  new SimpleHttpResponse(content, statusCode);
	}
	
	private HttpContext getNewContext() {
		CookieStore cookieStore = new BasicCookieStore();
		HttpContext context = new BasicHttpContext();
		context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
		return context;
	}


}
