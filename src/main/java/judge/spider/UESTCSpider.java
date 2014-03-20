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


public class UESTCSpider extends Spider {

	public void crawl() throws Exception{

		if (!problem.getOriginProb().matches("[1-9]\\d*")) {
			throw new Exception();
		}

		String html = null;
		HttpEntity entity = null;
		try {
			HttpClient client = MultipleProxyHttpClientFactory.getInstance("UESTC");
			HttpHost host = new HttpHost("acm.uestc.edu.cn");
			HttpGet get = new HttpGet("/old/problem.php?pid=" + problem.getOriginProb());
			HttpResponse response = client.execute(host, get, new BasicHttpContext());
			entity = response.getEntity();

			html = EntityUtils.toString(entity, Charset.forName("UTF-8"));
			html = html.replaceAll("<div class=\"bg\">\\s*</div>", "");
			html = HtmlHandleUtil.transformUrlToAbs(html, host.toURI() + get.getURI());
		} finally {
			EntityUtils.consume(entity);
		}

		String title = Tools.regFind(html, problem.getOriginProb() + " - ([\\s\\S]*?) - UESTC Online Judge").trim();
		if (title.isEmpty()){
			throw new Exception();
		}
		
		problem.setTitle(title);
		problem.setTimeLimit(Integer.parseInt(Tools.regFind(html, "Time Limit: <span class=\"h4\">\\s*(\\d+)")));
		problem.setMemoryLimit(Integer.parseInt(Tools.regFind(html, "Memory Limit: <span class=\"h4\">\\s*(\\d+)")));
		description.setDescription(Tools.regFind(html, "<h2>Description</h2>([\\s\\S]*?)<h2>"));
		description.setInput(Tools.regFind(html, "<h2>Input</h2>([\\s\\S]*?)<h2>"));
		description.setOutput(Tools.regFind(html, "<h2>Output</h2>([\\s\\S]*?)<h2>"));
		description.setSampleInput(Tools.regFind(html, "<h2>Sample Input</h2>([\\s\\S]*?)<h2>"));
		description.setSampleOutput(Tools.regFind(html, "<h2>Sample Output</h2>([\\s\\S]*?)<h2>"));
		description.setHint(Tools.regFind(html, "<h2>Hint</h2>([\\s\\S]*?)<h2>"));
		problem.setSource(Tools.regFind(html, "<h2>Source</h2>\\s*<p>([\\s\\S]*?)</p>\\s*</div>\\s*<div class=\"pmenu_all").trim());
		problem.setUrl("http://acm.uestc.edu.cn/old/problem.php?pid=" + problem.getOriginProb());
	}
}
