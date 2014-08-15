package judge.httpclient;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;

/**
 * Note: Be careful that this factory may not be generic! 
 * @author Isun
 *
 */
@SuppressWarnings("deprecation")
public class MultipleProxyHttpClientFactory {

	private static List<HttpClient> delegates = new ArrayList<HttpClient>();
	private static Map<String, MultipleProxyHttpClient> instances = new HashMap<String, MultipleProxyHttpClient>();

	static {
		Scheme sslScheme = null;
		try {
			X509TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] xcs, String string) {
				}

				public void checkServerTrusted(X509Certificate[] xcs, String string) {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			SSLContext sslcontext;
			sslcontext = SSLContext.getInstance("TLS");

			sslcontext.init(null, new TrustManager[] { tm }, null);

			SSLSocketFactory socketFactory = new SSLSocketFactory(sslcontext, SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
			// 不校验域名
			socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			sslScheme = new Scheme("https", 443, socketFactory);

			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
			schemeRegistry.register(sslScheme);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}

		Scheme paramSocksScheme = new Scheme("http", 80, new SocksSchemeSocketFactory());

		// ////////////////////////////////////////////////////////////////

		PoolingClientConnectionManager plainCM = new PoolingClientConnectionManager();
		plainCM.setMaxTotal(200);
		plainCM.setDefaultMaxPerRoute(40);
		plainCM.getSchemeRegistry().register(sslScheme);

		PoolingClientConnectionManager paramSocksCM = new PoolingClientConnectionManager();
		paramSocksCM.setMaxTotal(200);
		paramSocksCM.setDefaultMaxPerRoute(40);
		paramSocksCM.getSchemeRegistry().register(sslScheme);
		paramSocksCM.getSchemeRegistry().register(paramSocksScheme);

		// ////////////////////////////////////////////////////////////////

		// 0
		final HttpClient lrqHttpProxyClient = new DefaultHttpClient(plainCM);
		HttpHost lrqHttpProxyHost = new HttpHost("106.186.23.182", 25);
		lrqHttpProxyClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, lrqHttpProxyHost);

		// 1
		final HttpClient lrqSocksProxyClient = new DefaultHttpClient(paramSocksCM);
		lrqSocksProxyClient.getParams().setParameter("socks.host", "106.186.23.182");
		lrqSocksProxyClient.getParams().setParameter("socks.port", 21);

		// 2
		final HttpClient localssProxyClient = new DefaultHttpClient(paramSocksCM);
		localssProxyClient.getParams().setParameter("socks.host", "127.0.0.1");
		localssProxyClient.getParams().setParameter("socks.port", 1080);

		// 3
		final HttpClient plainClient = new DefaultHttpClient(plainCM);

		delegates.add(lrqHttpProxyClient);
		delegates.add(lrqSocksProxyClient);
		delegates.add(localssProxyClient);
		delegates.add(plainClient);

		for (HttpClient client : delegates) {
			((DefaultHttpClient) client).getParams().setParameter(CoreProtocolPNames.USER_AGENT,
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36");
			((DefaultHttpClient) client).getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			((DefaultHttpClient) client).getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
		}
	}

	static public MultipleProxyHttpClient getInstance(String identifier) {
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
