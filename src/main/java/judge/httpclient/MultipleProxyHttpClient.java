package judge.httpclient;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class MultipleProxyHttpClient implements HttpClient {
	private final static Logger log = LoggerFactory.getLogger(MultipleProxyHttpClient.class);

	private String identifier;
	private List<HttpClient> delegates;

	private long[] lastCostTime;
	
	public MultipleProxyHttpClient(String identifier, List<HttpClient> delegates) {
		this.identifier = identifier;
		this.delegates = delegates;
		this.lastCostTime = new long[delegates.size()];
	}
	
	/**
	 * Use the client that has the shortest lastCostTime
	 */
	@Override
	public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) {
		int idx = 0;
		for (int i = 1; i < delegates.size(); i++) {
			if (lastCostTime[i] < lastCostTime[idx]) {
				idx = i;
			}
		}
		
		try {
			long begin = System.currentTimeMillis();
			T result = delegates.get(idx).execute(target, request, responseHandler, context);
			lastCostTime[idx] = System.currentTimeMillis() - begin;
			return result;
		} catch (Throwable t) {
			log.error("Client " + idx + " of " + identifier + " failed.");
			lastCostTime[idx] = 60000L;
			throw new RuntimeException(t);
		}
	}

	@Override
	public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
		throw new RuntimeException("Not supported");
	}
	
	@Override
	public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
		return null;
	}

	@Override
	public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public ClientConnectionManager getConnectionManager() {
		throw new RuntimeException("Not supported");
	}

	@Override
	public HttpParams getParams() {
		throw new RuntimeException("Not supported");
	}

}
