package judge.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

public class MultipleProxyHttpClient implements HttpClient {
	
	private String identifier;
	private List<HttpClient> delegates;
	
	private Integer[] successCount;
	private Integer[] failCount;
	private Integer lastSuccessIndex;
	
	public MultipleProxyHttpClient(String identifier, List<HttpClient> delegates) {
		this.identifier = identifier;
		this.delegates = delegates;

		successCount = new Integer[delegates.size()];
		Arrays.fill(successCount, 0);

		failCount = new Integer[delegates.size()];
		Arrays.fill(failCount, 0);
	}

	/**
	 * First use the last success client;
	 * If fail, use the client that has failed the least times.
	 */
	@Override
	public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
		List<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < delegates.size(); i++) {
			indices.add(i);
		}
		Collections.sort(indices, new Comparator<Integer>() {
			@Override
			public int compare(Integer a, Integer b) {
				if (a.equals(lastSuccessIndex)) {
					return -1;
				} else if (b.equals(lastSuccessIndex)) {
					return 1;
				} else if (!failCount[a].equals(failCount[b])) {
					return failCount[a].compareTo(failCount[b]);
				} else {
					return successCount[b].compareTo(successCount[a]);
				}
			}
		});
		IOException finalException = null;
		for (int i : indices) {
			HttpClient client = delegates.get(i);
			try {
				HttpResponse response = client.execute(target, request, context);
				successCount[i]++;
				lastSuccessIndex = i;
				System.out.println("Client " + identifier + " #" + i + " succeeded -> "  + successCount[i] + "/" + failCount[i]);
				return response;
			} catch (IOException e) {
				failCount[i]++;
				System.err.println("Client " + identifier + " #" + i + " failed -> "  + successCount[i] + "/" + failCount[i]);
				finalException = e;
			}
		}
		throw finalException;
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
	public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException,
			ClientProtocolException {
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
