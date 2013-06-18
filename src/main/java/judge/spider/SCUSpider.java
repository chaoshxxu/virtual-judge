package judge.spider;

import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class SCUSpider extends Spider {

	@Override
	public void crawl() throws Exception {
		HttpHost host = new HttpHost("cstest.scu.edu.cn");
		HttpClient client = new DefaultHttpClient();
		HttpGet get = null;
		HttpResponse response = null;
		HttpEntity entity = null;
		String html;

		HttpHost proxy = new HttpHost("127.0.0.1", 8087);
		client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

		if (problem.getOriginProb().trim().startsWith("0")) {
			throw new RuntimeException();
		}
		
		try {
			get = new HttpGet("/soj/problem.action?id=" + problem.getOriginProb());
			response = client.execute(host, get);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
			html = HtmlHandleUtil.transformUrlToAbs(html, host.toURI() + get.getURI());
		} finally {
			EntityUtils.consume(entity);
		}
		
		String title = Tools.regFind(html, "<title>\\d+: (.+)</title>");
		if (StringUtils.isBlank(title)) {
			throw new RuntimeException();
		} else {
			problem.setTitle(title);
			problem.setTimeLimit(0);
			problem.setMemoryLimit(0);
			problem.setUrl(host.toURI() + get.getURI());
		}
		
		try {
			get = new HttpGet("/soj/problem/" + problem.getOriginProb() + "/");
			response = client.execute(host, get);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
			html = HtmlHandleUtil.transformUrlToAbs(html, host.toURI() + get.getURI());
		} finally {
			EntityUtils.consume(entity);
		}
		description.setDescription(html);
	}
}
