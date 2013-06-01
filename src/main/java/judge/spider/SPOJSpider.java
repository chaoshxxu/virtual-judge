package judge.spider;

import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;


public class SPOJSpider extends Spider {

	public void crawl() throws Exception{

		String html = "";
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod("http://www.spoj.pl/problems/" + problem.getOriginProb());
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

		if (html.contains("Wrong problem code!") || !html.contains("<h2>SPOJ Problem Set (classical)</h2>") && !html.contains("<h2>SPOJ Problem Set (tutorial)</h2>")){
			throw new Exception();
		}


		problem.setTitle(Tools.regFind(html, "<h1>\\d+\\.([\\s\\S]*?)</h1>").trim());
		if (problem.getTitle().isEmpty()){
			throw new Exception();
		}
		Double timeLimit = 1000 * Double.parseDouble(Tools.regFind(html, "Time limit:</td><td>([\\s\\S]*?)s"));
		problem.setTimeLimit(timeLimit.intValue());

		description.setDescription(Tools.regFind(html, "<p align=\"justify\"></p>([\\s\\S]*?)<hr /><table border=\"0\""));
		description.setInput(null);
		description.setOutput(null);
		description.setSampleInput(null);
		description.setSampleOutput(null);
		description.setHint(null);

		problem.setSource(Tools.regFind(html, "Resource:</td><td>([\\s\\S]*?)</td></tr>"));
		problem.setUrl("http://www.spoj.pl/problems/" + problem.getOriginProb());
	}
}
