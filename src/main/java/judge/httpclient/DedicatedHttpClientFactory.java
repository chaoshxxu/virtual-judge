package judge.httpclient;

import org.apache.http.HttpHost;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DedicatedHttpClientFactory {
	
	@Autowired
	private AnonymousHttpContextRepository contextRepository;

	@Autowired
	private MultipleProxyHttpClientFactory multipleProxyHttpClientFactory;

	public DedicatedHttpClient build(HttpHost host, HttpContext context, String charset) {
		DedicatedHttpClient client = new DedicatedHttpClient();
		client.host = host;
		client.context = context;
		client.charset = charset;
		client.delegate = multipleProxyHttpClientFactory.getInstance(host.getHostName());
		client.contextRepository = contextRepository;
		return client;
	}
	
	public DedicatedHttpClient build(HttpHost host, String charset) {
		return build(host, null, charset);
	}
	
	public DedicatedHttpClient build(HttpHost host, HttpContext context) {
		return build(host, context, "UTF-8");
	}

	public DedicatedHttpClient build(HttpHost host) {
		return build(host, null, "UTF-8");
	}

}
