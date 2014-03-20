package judge.spider;

import java.nio.charset.Charset;

import judge.tool.HtmlHandleUtil;
import judge.tool.MultipleProxyHttpClientFactory;
import judge.tool.Tools;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;


public class HUSTSpider extends Spider {

	public void crawl() throws Exception{
		
		if (!problem.getOriginProb().matches("[1-9]\\d*")) {
			throw new Exception();
		}

		String html = null;
		HttpEntity entity = null;
		try {
			HttpClient client = MultipleProxyHttpClientFactory.getInstance("HUST");
			HttpHost host = new HttpHost("acm.hust.edu.cn");
			HttpGet get = new HttpGet("/problem/show/" + problem.getOriginProb());
			HttpResponse response = client.execute(host, get, new BasicHttpContext());
			entity = response.getEntity();

			html = EntityUtils.toString(entity, Charset.forName("UTF-8"));
			html = HtmlHandleUtil.transformUrlToAbs(html, host.toURI() + get.getURI());
		} finally {
			EntityUtils.consume(entity);
		}

		String title = Tools.regFind(html, "<title>([\\s\\S]*?)</title>").trim();
		if (title.isEmpty()){
			throw new Exception();
		}
		
		problem.setTitle(title);
		problem.setTimeLimit((int) (1000 * Double.parseDouble(Tools.regFind(html, "Time Limit: <span class=\"label label-warning\">(.+)s</span>"))));
		problem.setMemoryLimit((int) (1024 * Double.parseDouble(Tools.regFind(html, "Memory Limit: <span class=\"label label-danger\">(.+)MB</span>"))));
		description.setDescription(Tools.regFind(html, "<dd id=\"problem-desc\">([\\s\\S]*?)</dd>"));
		description.setInput(Tools.regFind(html, "<dt> Input </dt>\\s*<dd>([\\s\\S]*?)</dd>"));
		description.setOutput(Tools.regFind(html, "<dt> Output </dt>\\s*<dd>([\\s\\S]*?)</dd>"));
		description.setSampleInput(Tools.regFind(html, "<dt> Sample Input </dt>\\s*<dd>([\\s\\S]*?)</dd>"));
		description.setSampleOutput(Tools.regFind(html, "<dt> Sample Output </dt>\\s*<dd>([\\s\\S]*?)</dd>"));
		description.setHint(Tools.regFind(html, "<dt> Hint </dt>\\s*<dd>([\\s\\S]*?)</dd>"));
		problem.setSource(Tools.regFind(html, "<dt> Source </dt>\\s*<dd>([\\s\\S]*?)</dd>"));
		problem.setUrl("http://acm.hust.edu.cn/problem/show/" + problem.getOriginProb());
	}
}
