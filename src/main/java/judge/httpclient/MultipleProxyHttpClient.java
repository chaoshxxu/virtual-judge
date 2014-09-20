package judge.httpclient;

import java.io.IOException;
import java.util.ArrayList;
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
        List<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < delegates.size(); i++) {
            indices.add(i);
        }
        Collections.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return (int) (lastCostTime[a] - lastCostTime[b]);
            }
        });
        Exception finalException = null;
        for (int i : indices) {
            HttpClient client = delegates.get(i);
            long beginTime = System.currentTimeMillis();
            try {
                T result = client.execute(target, request, responseHandler, context);
                lastCostTime[i] = System.currentTimeMillis() - beginTime;
                
//                for (int j = 0; j < delegates.size(); j++) {
//                    if (j == i) {
//                        System.out.print("(" + lastCostTime[j] + ") ");
//                    } else {
//                        System.out.print(lastCostTime[j] + " ");
//                    }
//                }
//                System.out.println();
                
                return result;
            } catch (Exception e) {
                lastCostTime[i] = Math.max(lastCostTime[i] + 1, 60000);
                lastCostTime[i] = Math.max(lastCostTime[i], System.currentTimeMillis() - beginTime);
                finalException = e;
            }
        }
        log.error("All clients of " + identifier + " failed.");
        throw new RuntimeException(finalException);
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
