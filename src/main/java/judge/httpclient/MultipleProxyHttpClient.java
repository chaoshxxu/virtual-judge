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
    
//    private void showDelegatesStatus() {
//        for (int j = 0; j < delegates.size(); j++) {
//            System.out.print(lastCostTime[j] + " ");
//        }
//        System.out.println();   
//    }

    /**
     * Use the client that has the shortest lastCostTime
     */
    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) {
        boolean[] usedDelegate = new boolean[delegates.size()];
        int tryCount = 0;
        
        while (true) {
            int chosenIndex = -1;
            for (int i = 0; i < delegates.size(); i++) {
                if (!usedDelegate[i] && (chosenIndex < 0 || lastCostTime[i] < lastCostTime[chosenIndex])) {
                    chosenIndex = i;
                }
            }
            usedDelegate[chosenIndex] = true;
            
            HttpClient client = delegates.get(chosenIndex);
            long beginTime = System.currentTimeMillis();
            try {
                T result = client.execute(target, request, responseHandler, context);
                lastCostTime[chosenIndex] = System.currentTimeMillis() - beginTime;
                return result;
            } catch (Throwable t) {
                lastCostTime[chosenIndex] = Math.max(lastCostTime[chosenIndex] + 1, 60000);
                lastCostTime[chosenIndex] = Math.max(lastCostTime[chosenIndex], System.currentTimeMillis() - beginTime);
                if (++tryCount >= delegates.size()) {
                    log.error("All clients of " + identifier + " failed.");
                    throw new RuntimeException(t);
                }
            }
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
