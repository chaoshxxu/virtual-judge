package judge.httpclient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HttpContext;

public class DedicatedHttpClient {

	protected HttpHost host;
	protected HttpContext context;
	protected String charset;
	
	protected HttpClient delegate;
	protected AnonymousHttpContextRepository contextRepository;

	protected DedicatedHttpClient() {
	}
	
	// /////////////////////////////////////////////////////////

	public <T> T execute(final HttpRequest request, final SimpleHttpResponseMapper<T> mapper) {
		HttpContext _context = context != null ? context : contextRepository.acquire();
		try {
			return delegate.execute(host, request, new ResponseHandler<T>() {
				@Override
				public T handleResponse(HttpResponse response) {
					try {
						SimpleHttpResponse simpleHttpResponse = SimpleHttpResponse.build(response, charset);
						return mapper.map(simpleHttpResponse);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}, _context);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (context == null && _context != null) {
				contextRepository.release(_context);
			}
		}
	}

	public SimpleHttpResponse execute(final HttpRequest request, final SimpleHttpResponseValidator... validators) {
		return execute(request, new SimpleHttpResponseMapper<SimpleHttpResponse>() {
			@Override
			public SimpleHttpResponse map(SimpleHttpResponse response) throws Exception {
				for (SimpleHttpResponseValidator validator : validators) {
					if (validator != null) {
						validator.validate(response);
					}
				}
				return response;
			}
		});
	}

	public SimpleHttpResponse execute(final HttpRequest request) {
		return execute(request, SimpleHttpResponseValidator.DUMMY_VALIDATOR);
	}

	public <T> T execute(final HttpRequest request, final ResponseHandler<T> handler) {
		HttpContext _context = context != null ? context : contextRepository.acquire();
		try {
			return delegate.execute(host, request, handler, _context);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (context == null && _context != null) {
				contextRepository.release(_context);
			}
		}
	}

	// /////////////////////////////////////////////////////////

	public <T> T get(String url, SimpleHttpResponseMapper<T> mapper) {
		return execute(new HttpGet(url), mapper);
	}

	public SimpleHttpResponse get(String url, SimpleHttpResponseValidator... validators) {
		return execute(new HttpGet(url), validators);
	}

	public SimpleHttpResponse get(String url) {
		return get(url, SimpleHttpResponseValidator.DUMMY_VALIDATOR);
	}

	// /////////////////////////////////////////////////////////

	public <T> T post(String url, SimpleHttpResponseMapper<T> mapper) {
		return execute(new HttpPost(url), mapper);
	}

	public SimpleHttpResponse post(String url, SimpleHttpResponseValidator... validators) {
		return execute(new HttpPost(url), validators);
	}

	public SimpleHttpResponse post(String url) {
		return post(url, SimpleHttpResponseValidator.DUMMY_VALIDATOR);
	}

	// /////////////////////////////////////////////////////////

	public <T> T post(String url, HttpEntity entity, SimpleHttpResponseMapper<T> mapper) {
		HttpPost post = new HttpPost(url);
		post.setEntity(entity);
		return execute(post, mapper);
	}

	public SimpleHttpResponse post(String url, HttpEntity entity, SimpleHttpResponseValidator... validators) {
		HttpPost post = new HttpPost(url);
		post.setEntity(entity);
		return execute(post, validators);
	}

	public SimpleHttpResponse post(String url, HttpEntity entity) {
		return post(url, entity, SimpleHttpResponseValidator.DUMMY_VALIDATOR);
	}

	// ////////////////////////////////////////////////////////

	public HttpContext getContext() {
		return context;
	}

}
