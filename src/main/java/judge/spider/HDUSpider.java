package judge.spider;

import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class HDUSpider extends Spider {

	public void crawl() throws Exception{
		String html = "";
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod("http://acm.hdu.edu.cn/showproblem.php?pid=" + problem.getOriginProb());
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if(statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + getMethod.getStatusLine());
			}
			html = Tools.getHtml(getMethod, null);
			html = HtmlHandleUtil.transformUrlToAbs(html, getMethod.getURI().toString());
		} catch(Exception e) {
			getMethod.releaseConnection();
			throw new Exception();
		}

		problem.setTitle(Tools.regFind(html, "color:#1A5CC8\">([\\s\\S]*?)</h1>").trim());
		if (problem.getTitle().isEmpty()){
			throw new Exception();
		}

		problem.setTimeLimit(Integer.parseInt(Tools.regFind(html, "(\\d*) MS")));
		problem.setMemoryLimit(Integer.parseInt(Tools.regFind(html, "/(\\d*) K")));
		description.setDescription(Tools.regFind(html, "> Problem Description </div>([\\s\\S]*?)<br /><[^<>]*?panel_title[^<>]*?>"));
		description.setInput(Tools.regFind(html, "> Input </div>([\\s\\S]*?)<br /><[^<>]*?panel_title[^<>]*?>"));
		description.setOutput(Tools.regFind(html, "> Output </div>([\\s\\S]*?)<br /><[^<>]*?panel_title[^<>]*?>"));
		description.setSampleInput(Tools.regFind(html, "> Sample Input </div>([\\s\\S]*?)<br /><[^<>]*?panel_title[^<>]*?>"));
		description.setSampleOutput(Tools.regFind(html, "> Sample Output </div>([\\s\\S]*?)(<br /><[^<>]*?panel_title[^<>]*?>|<[^<>]*?><[^<>]*?><i>Hint)") + "</div></div>");
		description.setHint(Tools.regFind(html, "<i>Hint</i></div>([\\s\\S]*?)<br /><[^<>]*?panel_title[^<>]*?>"));
		if (description.getHint().length() > 0){
			description.setHint("<pre>" + description.getHint() + "</pre>");
		}

		problem.setSource(Tools.regFind(html, "Source </div><div class=\"panel_content\">([\\s\\S]*?)<[^<>]*?panel_[^<>]*?>").replaceAll("<[\\s\\S]*?>", ""));
		problem.setUrl("http://acm.hdu.edu.cn/showproblem.php?pid=" + problem.getOriginProb());
	}
}
