package judge.spider;

import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import judge.submitter.UVALiveSubmitter;
import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

public class UVALiveSpider extends Spider {

	public static String problemNumberMap[];
	public static Long lastTime = 0L;

	static {
		try {
			Class.forName("judge.submitter.UVALiveSubmitter");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void crawl() throws Exception{
		if (new Date().getTime() - lastTime > 7 * 86400 * 1000L) {
			problemNumberMap = null;
			lastTime = new Date().getTime();
		}
		if (problemNumberMap == null || problemNumberMap[5358] == null) {
			new UVaLiveSpiderInitializer("https://icpcarchive.ecs.baylor.edu/index.php?option=com_onlinejudge&Itemid=8&category=1").start();
		}
		do {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (UVaLiveSpiderInitializer.threadCnt > 0);

		String realProblemNumber = problemNumberMap[Integer.parseInt(problem.getOriginProb())];
		if (realProblemNumber == null) {
			throw new Exception();
		}

		String html = "";
		HttpClient httpClient = new DefaultHttpClient(UVALiveSubmitter.cm, UVALiveSubmitter.params);
//		httpClient.getHostConfiguration().setProxy("127.0.0.1", 8087);
		if (!problem.getOriginProb().matches("\\d+")) {
			throw new Exception();
		}

		//抓标题、时限
		HttpGet getMethod = new HttpGet("https://icpcarchive.ecs.baylor.edu/index.php?option=com_onlinejudge&Itemid=8&page=show_problem&problem=" + realProblemNumber);
		HttpEntity entity = null;
		try {
			HttpResponse response = httpClient.execute(getMethod);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				throw new Exception();
			}
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		problem.setTitle(Tools.regFind(html, "<h3>" + problem.getOriginProb() + " - ([\\s\\S]+?)</h3>").trim());
		problem.setTimeLimit(Integer.parseInt(Tools.regFind(html, "Time limit: ([\\d\\.]+)").replaceAll("\\.", "")));
		problem.setMemoryLimit(0);
		problem.setUrl("https://icpcarchive.ecs.baylor.edu/index.php?option=com_onlinejudge&Itemid=8&page=show_problem&problem=" + realProblemNumber);

		//抓描述
		int category = Integer.parseInt(problem.getOriginProb()) / 100;
		String pdfLink = "<span style='float:right'><a target='_blank' href='https://icpcarchive.ecs.baylor.edu/external/" + category + "/" + problem.getOriginProb() + ".pdf'><img width='100' height='26' border='0' title='Download as PDF' alt='Download as PDF' src='https://icpcarchive.ecs.baylor.edu/components/com_onlinejudge/images/button_pdf.png'></a></span><div style='clear:both'></div>";
		description.setDescription(pdfLink);
		getMethod = new HttpGet("https://icpcarchive.ecs.baylor.edu/external/" + category + "/" + problem.getOriginProb() + ".html");
		try {
			HttpResponse response = httpClient.execute(getMethod);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				throw new Exception();
			}
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
			html = HtmlHandleUtil.transformUrlToAbs(html, getMethod.getURI().toString());

			//some problems' description are fucking long, only get the body.innerHTML
			html = html.replaceAll("(?i)^[\\s\\S]*<body[^>]*>", "").replaceAll("(?i)</body>[\\s\\S]*", "");

			description.setDescription(pdfLink + Tools.regFind(html, "^([\\s\\S]*?)<H2><FONT size=\"4\" COLOR=\"#ff0000\"><A NAME=\"SECTION000100\\d000000000000000\""));
			description.setInput(Tools.regFind(html, "Int?put</A>&nbsp;</FONT></H2>([\\s\\S]*?)<H2><FONT size=\"4\" COLOR=\"#ff0000\"><A NAME=\"SECTION000100\\d000000000000000\""));
			description.setOutput(Tools.regFind(html, "Output</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*?)<H2><FONT size=\"4\" COLOR=\"#ff0000\"><A NAME=\"SECTION000100\\d000000000000000\""));
			description.setSampleInput(Tools.regFind(html, "Sample Int?put</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*?)<H2><FONT size=\"4\" COLOR=\"#ff0000\"><A NAME=\"SECTION000100\\d000000000000000\""));
			description.setSampleOutput(Tools.regFind(html, "Sample Output</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*)"));

			if (description.getSampleInput().isEmpty() || description.getSampleOutput().isEmpty()) {
				description.setDescription(pdfLink + html);
				description.setInput(null);
				description.setOutput(null);
				description.setSampleInput(null);
				description.setSampleOutput(null);
			}
			description.setDescription("<style type=\"text/css\">h1,h2,h3,h4,h5,h6{margin-bottom:0;}div.textBG p{margin: 0 0 0.0001pt;}</style>" + description.getDescription());
		} catch (Exception e) {
		} finally {
			getMethod.releaseConnection();
		}
	}

}
