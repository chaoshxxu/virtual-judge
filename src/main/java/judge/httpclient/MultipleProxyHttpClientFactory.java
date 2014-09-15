package judge.httpclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class MultipleProxyHttpClientFactory {
//	private final static Logger log = LoggerFactory.getLogger(MultipleProxyHttpClientFactory.class);
	
	private List<HttpClient> delegates;
	private Map<String, MultipleProxyHttpClient> instances = new HashMap<String, MultipleProxyHttpClient>();
	private File jsonConfig;
	
	public MultipleProxyHttpClientFactory(String jsonConfigPath) {
		this.jsonConfig = new File(jsonConfigPath);
	}

	private HttpClientBuilder getBaseBuilder(int socketTimeout, int connectionTimeout, int maxConnTotal, int maxConnPerRoute, String userAgent) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		SSLContextBuilder contextBuilder = SSLContexts.custom();
		contextBuilder.loadTrustMaterial(null, new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		});
		SSLContext sslContext = contextBuilder.build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {
			@Override
			public void verify(String host, SSLSocket ssl) throws IOException {
			}

			@Override
			public void verify(String host, X509Certificate cert) throws SSLException {
			}

			@Override
			public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
			}

			@Override
			public boolean verify(String s, SSLSession sslSession) {
				return true;
			}
		});

		Registry<ConnectionSocketFactory> socketFactoryRegistry = 
				RegistryBuilder.<ConnectionSocketFactory> create()
				.register("https", sslsf)
				.register("http", PlainConnectionSocketFactory.getSocketFactory())
				.build();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		
		RequestConfig config = RequestConfig.custom()
			    .setSocketTimeout(socketTimeout)
			    .setConnectTimeout(connectionTimeout)
			    .build();
		
		return HttpClients.custom().setConnectionManager(cm)
			.setMaxConnTotal(maxConnTotal)
			.setMaxConnPerRoute(maxConnPerRoute)
			.setUserAgent(userAgent)
			.setDefaultRequestConfig(config);
	}
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() throws JsonIOException, JsonSyntaxException, FileNotFoundException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		Map<String, Object> proto = new HashMap<String, Object>();
		Map<String, Object> httpClientConfigs = new Gson().fromJson(new FileReader(jsonConfig), proto.getClass());
		
		int socketTimeout = ((Double)httpClientConfigs.get("socket_timeout")).intValue();
		int connectionTimeout = ((Double)httpClientConfigs.get("connection_timeout")).intValue();
		int maxConnTotal = ((Double)httpClientConfigs.get("max_conn_total")).intValue();
		int maxConnPerRoute = ((Double)httpClientConfigs.get("max_conn_per_route")).intValue();
		String userAgent = (String) httpClientConfigs.get("user_agent");
		ArrayList<ArrayList<Object>> proxyConfigs = (ArrayList<ArrayList<Object>>) httpClientConfigs.get("proxies");
		
		delegates = new ArrayList<HttpClient>();
		for (ArrayList<Object> proxyConfig : proxyConfigs) {
			HttpClientBuilder baseBuilder = getBaseBuilder(
					socketTimeout,
					connectionTimeout,
					maxConnTotal,
					maxConnPerRoute,
					userAgent
			);
			if (proxyConfig.size() < 2) {
				delegates.add(baseBuilder.build());
				continue;
			}
			
			String address = (String) proxyConfig.get(0);
			int port = ((Double)proxyConfig.get(1)).intValue();
			HttpHost proxyHost = new HttpHost(address, port);
			
			if (proxyConfig.size() >= 4) {
				String username = (String) proxyConfig.get(2);
				String password = (String) proxyConfig.get(3);
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(
						new AuthScope(address, port),
						new UsernamePasswordCredentials(username, password));
				baseBuilder.setDefaultCredentialsProvider(credsProvider);
			}
			
	        CloseableHttpClient httpclient = baseBuilder.setProxy(proxyHost).build();
	        delegates.add(httpclient);
		}
	}

	public MultipleProxyHttpClient getInstance(String identifier) {
		if (!instances.containsKey(identifier)) {
			synchronized (instances) {
				if (!instances.containsKey(identifier)) {
					instances.put(identifier, new MultipleProxyHttpClient(identifier, delegates));
				}
			}
		}
		return instances.get(identifier);
	}

}
