package judge.spider;

import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;


public class NBUTSpider extends Spider {

	public void crawl() throws Exception{

		String html = "";
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT, "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8");

		GetMethod getMethod = new GetMethod("http://cdn.ac.nbutoj.com/Problem/view.xhtml?id=" + problem.getOriginProb());
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if(statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + getMethod.getStatusLine());
			}
			html = Tools.getHtml(getMethod, null);
			html = HtmlHandleUtil.transformUrlToAbs(html, getMethod.getURI().toString());
			html = html.replaceAll("cdn\\.ac\\.nbutoj\\.com/+", "cdn.ac.nbutoj.com/");
			html = html.replace("<pre>无</pre>", "");
		} finally {
			getMethod.releaseConnection();
		}

		problem.setTitle(Tools.regFind(html, "\\["+ problem.getOriginProb() + "\\]([\\s\\S]*?)</title>").trim());
		if (problem.getTitle().isEmpty()){
			throw new Exception();
		}

		problem.setTimeLimit(Integer.parseInt(Tools.regFind(html, "时间限制: (\\d+)")));
		problem.setMemoryLimit(Integer.parseInt(Tools.regFind(html, "内存限制: (\\d+)")));
		description.setDescription(Tools.regFind(html, "<li class=\"contents\" id=\"description\">([\\s\\S]*?)</li>\\s*<li class=\"titles\" id=\"input-title\">"));
		description.setInput(Tools.regFind(html, "<li class=\"contents\" id=\"input\">([\\s\\S]*?)</li>\\s*<li class=\"titles\" id=\"output-title\">"));
		description.setOutput(Tools.regFind(html, "<li class=\"contents\" id=\"output\">([\\s\\S]*?)</li>\\s*<li class=\"titles\" id=\"sampleinput-title\">"));
		description.setSampleInput(Tools.regFind(html, "<li class=\"contents\" id=\"sampleinput\">([\\s\\S]*?)</li>\\s*<li class=\"titles\" id=\"sampleoutput-title\">"));
		description.setSampleOutput(Tools.regFind(html, "<li class=\"contents\" id=\"sampleoutput\">([\\s\\S]*?)</li>\\s*<li class=\"titles\" id=\"hint-title\">"));
		description.setHint(Tools.regFind(html, "<li class=\"contents\" id=\"hint\">([\\s\\S]*?)</li>\\s*<li class=\"titles\" id=\"source-title\">"));
		problem.setSource(Tools.regFind(html, "<li class=\"contents\" id=\"source\">([\\s\\S]*?)</li>\\s*<li class=\"titles\" id=\"operation-title\">").replaceAll("<pre>([^<>]*)</pre>", "$1"));
		problem.setUrl("http://cdn.ac.nbutoj.com/Problem/view.xhtml?id=" + problem.getOriginProb());
	}
}
