package judge.spider;

import java.util.List;

import judge.submitter.LightOJSubmitter;
import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;


public class LightOJSpider extends Spider {

	private static HttpClient httpClient = new HttpClient();
	private static Object lock = new boolean[0];


	public void crawl() throws Exception{
		synchronized (lock) {
			gao();
		}
	}

	private void gao() throws Exception {
		httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT, "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8");
		if (!isLoggedIn()) {
			login();
		}

		String html = "";
		GetMethod getMethod = new GetMethod("http://lightoj.com/volume_showproblem.php?problem=" + problem.getOriginProb());
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if(statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: "+getMethod.getStatusLine());
			}
			html = Tools.getHtml(getMethod, null);
			html = HtmlHandleUtil.transformUrlToAbs(html, getMethod.getURI().toString());
		} finally {
			getMethod.releaseConnection();
		}

		problem.setTitle(Tools.regFind(html, "Problem \\d+ - ([\\s\\S]*?)</title>"));
		if (problem.getTitle().isEmpty()){
			throw new Exception();
		}
		problem.setTimeLimit((int)(1000 * Double.parseDouble(Tools.regFind(html, "([\\d\\.]*?) second\\(s\\)</span>"))));
		problem.setMemoryLimit(1024 * Integer.parseInt(Tools.regFind(html, "([\\d\\.]*?) MB</span>")));

		List<String> styleSheets = HtmlHandleUtil.getStyleSheet(html);
		String targetStyleSheet = null;
		for (String styleSheet : styleSheets) {
			if (styleSheet.contains("Font Definitions")) {
				targetStyleSheet = styleSheet;
			}
		}

		description.setDescription(targetStyleSheet + Tools.regFind(html, "<div class=\"Section1\">([\\s\\S]*?)<h1>Input</h1>"));
		description.setInput(Tools.regFind(html, "<h1>Input</h1>([\\s\\S]*?)<h1>Output</h1>"));
		description.setOutput(Tools.regFind(html, "<h1>Output</h1>([\\s\\S]*?)<table class=\"Mso\\w+"));
		description.setSampleInput(Tools.regFind(html, "<h1>Output</h1>[\\s\\S]*<table class=\"Mso\\w+[\\s\\S]*?<td[\\s\\S]*?<td[\\s\\S]*?<td[^>]*?>([\\s\\S]*?)</td>"));
		description.setSampleOutput(Tools.regFind(html, "<h1>Output</h1>[\\s\\S]*<table class=\"Mso\\w+[\\s\\S]*?<td[\\s\\S]*?<td[\\s\\S]*?<td[\\s\\S]*?<td[^>]*?>([\\s\\S]*?)</td>"));
		description.setHint(Tools.regFind(html, "Note</h1>([\\s\\S]*?)</div>\\s+</body>"));

		problem.setSource(Tools.regFind(html, "(<div id=\"problem_setter\">[\\s\\S]*?)</div>\\s*</div>\\s*<span id=\"showNavigation\""));
		problem.setUrl("http://lightoj.com/volume_showproblem.php?problem=" + problem.getOriginProb());
	}


	private boolean isLoggedIn() throws Exception {
		String html;

		GetMethod getMethod = new GetMethod("http://lightoj.com");
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if(statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: "+getMethod.getStatusLine());
			}
			html = Tools.getHtml(getMethod, null);
		} catch(Exception e) {
			throw e;
		} finally {
			getMethod.releaseConnection();
		}
		
		return !html.contains("<script>location.href=");
	}

	private void login() throws Exception{
        PostMethod postMethod = new PostMethod("http://lightoj.com/login_check.php");

        postMethod.addParameter("mypassword", LightOJSubmitter.passwordList[0]);
        postMethod.addParameter("myrem", "1");
        postMethod.addParameter("myuserid", LightOJSubmitter.usernameList[0]);
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

        try {
        	httpClient.executeMethod(postMethod);
		} finally {
			postMethod.releaseConnection();
		}
	}
}
