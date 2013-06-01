package judge.spider;

import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;


public class HUSTSpider extends Spider {

	public void crawl() throws Exception{

		String html = "";
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod("http://acm.hust.edu.cn/problem.php?id=" + problem.getOriginProb());
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if(statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: "+getMethod.getStatusLine());
			}
			html = Tools.getHtml(getMethod, null);
			html = HtmlHandleUtil.transformUrlToAbs(html, getMethod.getURI().toString());
		} catch(Exception e) {
			getMethod.releaseConnection();
			throw new Exception();
		}

		problem.setTitle(Tools.regFind(html, "<title>[\\s\\S]*?-- ([\\s\\S]*?)</title>").trim());
		if (problem.getTitle().isEmpty()){
			throw new Exception();
		}

		problem.setTimeLimit(1000 * Integer.parseInt(Tools.regFind(html, "Time Limit: </b>([\\d\\.]*?) Sec")));
		problem.setMemoryLimit(1024 * Integer.parseInt(Tools.regFind(html, "Memory Limit: </b>([\\d\\.]*?) MB")));
		description.setDescription(Tools.regFind(html, "<h2>Description</h2>([\\s\\S]*?)<h2>"));
		description.setInput(Tools.regFind(html, "<h2>Input</h2>([\\s\\S]*?)<h2>"));
		description.setOutput(Tools.regFind(html, "<h2>Output</h2>([\\s\\S]*?)<h2>"));
		description.setSampleInput(Tools.regFind(html, "<h2>Sample Input</h2>([\\s\\S]*?)<h2>"));
		description.setSampleOutput(Tools.regFind(html, "<h2>Sample Output</h2>([\\s\\S]*?)<h2>"));
		description.setHint(Tools.regFind(html, "<h2>HINT</h2>([\\s\\S]*?)<h2>"));
		problem.setSource(Tools.regFind(html, "<h2>Source</h2>([\\s\\S]*?)<center>").replaceAll("<[\\s\\S]*?>", ""));
		problem.setUrl("http://acm.hust.edu.cn/problem.php?id=" + problem.getOriginProb());
	}
}
