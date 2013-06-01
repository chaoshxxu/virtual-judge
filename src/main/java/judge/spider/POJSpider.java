package judge.spider;

import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;


public class POJSpider extends Spider {

	public void crawl() throws Exception{

		if (!problem.getOriginProb().matches("[1-9]\\d*")) {
			throw new Exception();
		}

		String html = "";
		HttpClient httpClient = new HttpClient();
//		httpClient.getHostConfiguration().setProxy("127.0.0.1", 8087);
		GetMethod getMethod = new GetMethod("http://poj.org/problem?id=" + problem.getOriginProb());
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if(statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: "+getMethod.getStatusLine());
			}
			html = Tools.getHtml(getMethod, null);
			html = HtmlHandleUtil.transformUrlToAbs(html, getMethod.getURI().toString());
		} catch(Exception e) {
			e.printStackTrace();
			throw new Exception();
		} finally {
			getMethod.releaseConnection();
		}

		problem.setTitle(Tools.regFind(html, "<title>\\d{3,} -- ([\\s\\S]*?)</title>").trim());
		if (problem.getTitle().isEmpty()){
			throw new Exception();
		}

		problem.setTimeLimit(Integer.parseInt(Tools.regFind(html, "<b>Time Limit:</b> (\\d{3,})MS</td>")));
		problem.setMemoryLimit(Integer.parseInt(Tools.regFind(html, "<b>Memory Limit:</b> (\\d{2,})K</td>")));
		description.setDescription(Tools.regFind(html, "<p class=\"pst\">Description</p>([\\s\\S]*?)<p class=\"pst\">"));
		description.setInput(Tools.regFind(html, "<p class=\"pst\">Input</p>([\\s\\S]*?)<p class=\"pst\">"));
		description.setOutput(Tools.regFind(html, "<p class=\"pst\">Output</p>([\\s\\S]*?)<p class=\"pst\">"));
		description.setSampleInput(Tools.regFind(html, "<p class=\"pst\">Sample Input</p>([\\s\\S]*?)<p class=\"pst\">"));
		description.setSampleOutput(Tools.regFind(html, "<p class=\"pst\">Sample Output</p>([\\s\\S]*?)<p class=\"pst\">"));
		problem.setSource(Tools.regFind(html, "<p class=\"pst\">Source</p>([\\s\\S]*?)</td></tr></tbody></table>"));
		description.setHint(Tools.regFind(html, "<p class=\"pst\">Hint</p>([\\s\\S]*?)<p class=\"pst\">"));
		problem.setUrl("http://poj.org/problem?id=" + problem.getOriginProb());
	}
}
