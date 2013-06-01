package judge.spider;

import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;


public class UESTCSpider extends Spider {

	public void crawl() throws Exception{

		if (!problem.getOriginProb().matches("[1-9]\\d*")) {
			throw new Exception();
		}

		String html = "";
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod("http://222.197.181.5/problem.php?pid=" + problem.getOriginProb());
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if(statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + getMethod.getStatusLine());
			}
			html = Tools.getHtml(getMethod, null);
			html = html.replaceAll("<div class=\"bg\">\\s*</div>", "");
			html = HtmlHandleUtil.transformUrlToAbs(html, getMethod.getURI().toString());
		} finally {
			getMethod.releaseConnection();
		}

		problem.setTitle(Tools.regFind(html, problem.getOriginProb() + " - ([\\s\\S]*?) - UESTC Online Judge").trim());
		if (problem.getTitle().isEmpty()){
			throw new Exception();
		}

		problem.setTimeLimit(Integer.parseInt(Tools.regFind(html, "Time Limit: <span class=\"h4\">\\s*(\\d+)")));
		problem.setMemoryLimit(Integer.parseInt(Tools.regFind(html, "Memory Limit: <span class=\"h4\">\\s*(\\d+)")));
		description.setDescription(Tools.regFind(html, "<h2>Description</h2>([\\s\\S]*?)<h2>"));
		description.setInput(Tools.regFind(html, "<h2>Input</h2>([\\s\\S]*?)<h2>"));
		description.setOutput(Tools.regFind(html, "<h2>Output</h2>([\\s\\S]*?)<h2>"));
		description.setSampleInput(Tools.regFind(html, "<h2>Sample Input</h2>([\\s\\S]*?)<h2>"));
		description.setSampleOutput(Tools.regFind(html, "<h2>Sample Output</h2>([\\s\\S]*?)<h2>"));
		description.setHint(Tools.regFind(html, "<h2>Hint</h2>([\\s\\S]*?)<h2>"));
		problem.setSource(Tools.regFind(html, "<h2>Source</h2>\\s*<p>([\\s\\S]*?)</p>\\s*</div>\\s*<div class=\"pmenu_all").trim());
		problem.setUrl("http://222.197.181.5/problem.php?pid=" + problem.getOriginProb());
	}
}
