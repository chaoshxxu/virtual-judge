package judge.spider;

import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;


public class FZUSpider extends Spider {

	public void crawl() throws Exception{
		if (!problem.getOriginProb().matches("[1-9]\\d+")) {
			throw new Exception();
		}

		String html = "";
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod("http://acm.fzu.edu.cn/problem.php?pid=" + problem.getOriginProb());
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		try {
			httpClient.executeMethod(getMethod);
			html = Tools.getHtml(getMethod, null);
			html = html.replaceAll("<div class=\"data\">([\\s\\S]*?)</div>", "<pre>$1</pre>");
			html = HtmlHandleUtil.transformUrlToAbs(html, getMethod.getURI().toString());
		} finally {
			getMethod.releaseConnection();
		}

		if (html.contains("No Such Problem!")){
			throw new Exception();
		}

		problem.setTitle(Tools.regFind(html, "<b> Problem \\d+ (.+?)</b>").trim());
		if (problem.getTitle().isEmpty()){
			throw new Exception();
		}

		problem.setTimeLimit(Integer.parseInt(Tools.regFind(html, "Time Limit: (\\d+) mSec")));
		problem.setMemoryLimit(Integer.parseInt(Tools.regFind(html, "Memory Limit : (\\d+) KB")));
		description.setDescription(Tools.regFind(html, "Problem Description</h2><div class=\"pro_desc\">([\\s\\S]+?)</div>\\s*<h2><img"));
		description.setInput(Tools.regFind(html, "Input</h2><div class=\"pro_desc\">([\\s\\S]+?)</div>\\s*<h2><img"));
		description.setOutput(Tools.regFind(html, "Output</h2><div class=\"pro_desc\">([\\s\\S]+?)</div>\\s*<h2><img"));
		description.setSampleInput(Tools.regFind(html, "Sample Input</h2>([\\s\\S]*?)<h2><img") + "</pre>");
		description.setSampleOutput(Tools.regFind(html, "Sample Output</h2>([\\s\\S]*?)(<h2><img|</div>\\s*<br />)") + "</pre>");
		description.setHint(Tools.regFind(html, "Hint</h2>([\\s\\S]+?)(<h2><img|</div>\\s*<br />)"));
		problem.setSource(Tools.regFind(html, "Source</h2>([\\s\\S]+?)(<h2><img|</div>\\s*<br />)"));
		if (problem.getSource().trim().isEmpty()) {
			problem.setSource(null);
		}
		problem.setUrl("http://acm.fzu.edu.cn/problem.php?pid=" + problem.getOriginProb());
	}
}
