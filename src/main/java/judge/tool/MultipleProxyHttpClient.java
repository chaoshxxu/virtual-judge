package judge.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
	
	/**
	 * How long the last request costs. If an exception is throw, take the value as the longest cost time in the past hour.
	 */
	private Integer[] lastCostTime;
	
	private int lastHour;
	private int longestCostTimeInLastHour;
	static private Calendar calendar = Calendar.getInstance();
	
	public MultipleProxyHttpClient(String identifier, List<HttpClient> delegates) {
		this.identifier = identifier;
		this.delegates = delegates;

		lastCostTime = new Integer[delegates.size()];
		Arrays.fill(lastCostTime, 0);
	}

	/**
	 * Use the client that has the shortest lastCostTime
	 */
	@Override
	public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
		if (calendar.get(Calendar.HOUR) != lastHour) {
			lastHour = calendar.get(Calendar.HOUR);
			longestCostTimeInLastHour = 0;
		}

		List<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < delegates.size(); i++) {
			indices.add(i);
		}
		Collections.sort(indices, new Comparator<Integer>() {
			@Override
			public int compare(Integer a, Integer b) {
				return lastCostTime[a].compareTo(lastCostTime[b]);
			}
		});
		Exception finalException = null;
		for (int i : indices) {
			HttpClient client = delegates.get(i);
			try {
				long beginTime = System.currentTimeMillis();
				HttpResponse response = client.execute(target, request, context);
				lastCostTime[i] = (int) (System.currentTimeMillis() - beginTime);
				longestCostTimeInLastHour = Math.max(longestCostTimeInLastHour, lastCostTime[i]);
				return response;
			} catch (Exception e) {
				lastCostTime[i] = longestCostTimeInLastHour + 1;
				finalException = e;
			}
		}
		System.err.println("All clients of " + identifier + " failed.");
		throw new RuntimeException(finalException);
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

	/**
	 * Use the client that has the shortest lastCostTime
	 */
	@Override
	public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) {
		if (calendar.get(Calendar.HOUR) != lastHour) {
			lastHour = calendar.get(Calendar.HOUR);
			longestCostTimeInLastHour = 0;
		}
		
		List<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < delegates.size(); i++) {
			indices.add(i);
		}
		Collections.sort(indices, new Comparator<Integer>() {
			@Override
			public int compare(Integer a, Integer b) {
				return lastCostTime[a].compareTo(lastCostTime[b]);
			}
		});
		Exception finalException = null;
		for (int i : indices) {
			HttpClient client = delegates.get(i);
			try {
				long beginTime = System.currentTimeMillis();
				T result = client.execute(target, request, responseHandler, context);
				lastCostTime[i] = (int) (System.currentTimeMillis() - beginTime);
				longestCostTimeInLastHour = Math.max(longestCostTimeInLastHour, lastCostTime[i]);
				return result;
			} catch (Exception e) {
				lastCostTime[i] = longestCostTimeInLastHour + 1;
				finalException = e;
			}
		}
		System.err.println("All clients of " + identifier + " failed.");
		throw new RuntimeException(finalException);
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
