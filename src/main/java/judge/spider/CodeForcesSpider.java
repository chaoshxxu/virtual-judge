package judge.spider;

import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class CodeForcesSpider extends Spider {

	public void crawl() throws Exception{

		String html = "";
		HttpClient httpClient = new HttpClient();
//		httpClient.getHostConfiguration().setProxy("127.0.0.1", 8087);
		int splitIndex = 0;
		while (problem.getOriginProb().charAt(splitIndex) <= '9') {
			++splitIndex;
		}
		String contestNum = problem.getOriginProb().substring(0, splitIndex);
		String problemNum = problem.getOriginProb().substring(splitIndex);
		GetMethod getMethod = new GetMethod("http://codeforces.com/problemset/problem/" + contestNum + "/" + problemNum);
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

		problem.setTitle(Tools.regFind(html, "<div class=\"title\">\\s*" + problemNum + "\\. ([\\s\\S]*?)</div>").trim());
		if (problem.getTitle().isEmpty()){
			throw new Exception();
		}
		Double timeLimit = 1000 * Double.parseDouble(Tools.regFind(html, "</div>([\\d\\.]+) seconds?\\s*</div>"));
		problem.setTimeLimit(timeLimit.intValue());
		problem.setMemoryLimit(1024 * Integer.parseInt(Tools.regFind(html, "</div>(\\d+) megabytes\\s*</div>")));
		description.setDescription(Tools.regFind(html, "standard output\\s*</div></div><div>([\\s\\S]*?)</div><div class=\"input-specification"));
		if (description.getDescription().isEmpty()) {
			description.setDescription("<div>" + Tools.regFind(html, "(<div class=\"input-file\">[\\s\\S]*?)</div><div class=\"input-specification"));
		}
		description.setInput(Tools.regFind(html, "<div class=\"section-title\">\\s*Input\\s*</div>([\\s\\S]*?)</div><div class=\"output-specification\">"));
		description.setOutput(Tools.regFind(html, "<div class=\"section-title\">\\s*Output\\s*</div>([\\s\\S]*?)</div><div class=\"sample-tests\">"));
		description.setSampleInput("<style type=\"text/css\">.input, .output {border: 1px solid #888888;} .output {margin-bottom:1em;position:relative;top:-1px;} .output pre,.input pre {background-color:#EFEFEF;line-height:1.25em;margin:0;padding:0.25em;} .title {background-color:#FFFFFF;border-bottom: 1px solid #888888;font-family:arial;font-weight:bold;padding:0.25em;}</style>" + Tools.regFind(html, "<div class=\"sample-test\">([\\s\\S]*?)</div>\\s*</div>\\s*</div>"));
		description.setHint(Tools.regFind(html, "<div class=\"section-title\">\\s*Note\\s*</div>([\\s\\S]*?)</div></div></div></div>"));
		problem.setUrl("http://codeforces.com/problemset/problem/" + contestNum + "/" + problemNum);
	}
}
