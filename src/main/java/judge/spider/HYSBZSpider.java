package judge.spider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;



public class HYSBZSpider extends Spider {

	public void crawl() throws Exception{

		String html = "";
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod("http://www.lydsy.com/JudgeOnline/problem.php?id=" + problem.getOriginProb());
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

		problem.setTitle(Tools.regFind(html, "<center><h2>([\\s\\S]*?)</h2>").replaceAll(problem.getOriginProb() + ": ", "").trim());
		if (problem.getTitle().isEmpty()){
			throw new Exception();
		}

		problem.setSource(Tools.regFind(html, "<h2>Source</h2>[\\s\\S]*?<div class=content><p>([\\s\\S]*?)</p></div><center>"));
		Matcher matcher = Pattern.compile("\\[(.*)\\](.*)").matcher(problem.getTitle());
		if (matcher.find()) {
			problem.setTitle(matcher.group(2));
			problem.setSource(matcher.group(1));
		}

		problem.setTimeLimit(1000 * Integer.parseInt(Tools.regFind(html, "Time Limit: </span>(\\d+) Sec")));
		problem.setMemoryLimit(1024 * Integer.parseInt(Tools.regFind(html, "Memory Limit: </span>(\\d+) MB")));
		description.setDescription(Tools.regFind(html, "<h2>Description</h2>([\\s\\S]*?)<h2>Input</h2>"));
		description.setInput(Tools.regFind(html, "<h2>Input</h2>([\\s\\S]*?)<h2>Output</h2>"));
		description.setOutput(Tools.regFind(html, "<h2>Output</h2>([\\s\\S]*?)<h2>Sample Input</h2>"));
		description.setSampleInput(Tools.regFind(html, "<h2>Sample Input</h2>([\\s\\S]*?)<h2>Sample Output</h2>").replaceAll("<span", "<pre").replaceAll("</span>", "</pre>").replace("<br /> ", "<br />"));
		description.setSampleOutput(Tools.regFind(html, "<h2>Sample Output</h2>([\\s\\S]*?)<h2>HINT</h2>").replaceAll("<span", "<pre").replaceAll("</span>", "</pre>").replace("<br /> ", "<br />"));
		description.setHint(Tools.regFind(html, "<h2>HINT</h2>([\\s\\S]*?)<h2>Source</h2>"));
		problem.setUrl("http://www.lydsy.com/JudgeOnline/problem.php?id=" + problem.getOriginProb());
	}
}
