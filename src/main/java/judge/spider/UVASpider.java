package judge.spider;

import java.io.IOException;
import java.util.Map;

import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

public class UVASpider extends Spider {

	@Override
	public void crawl() throws Exception {
		HttpHost host = new HttpHost("uva.onlinejudge.org");
		HttpClient client = new DefaultHttpClient();
		HttpGet get = null;
		HttpResponse response = null;
		HttpEntity entity = null;
		String html;

		if (!problem.getOriginProb().matches("[1-9]\\d*")) {
			throw new RuntimeException();
		}

		Map problemInfo = getProblemInfo(problem.getOriginProb(), client);

		if (problemInfo.isEmpty()) {
			throw new RuntimeException();
		} else {
			problem.setTitle((String) problemInfo.get("title"));
			problem.setTimeLimit(((Long) problemInfo.get("rtl")).intValue());
			problem.setMemoryLimit(0);
			problem.setUrl(host.toURI() + "/index.php?option=com_onlinejudge&Itemid=8&page=show_problem&problem=" + problemInfo.get("pid"));
		}

		int category = Integer.parseInt(problem.getOriginProb()) / 100;
		String pdfLink = "<span style='float:right'><a target='_blank' href='http://uva.onlinejudge.org/external/" + category + "/" + problem.getOriginProb() + ".pdf'><img width='100' height='26' border='0' title='Download as PDF' alt='Download as PDF' src='http://uva.onlinejudge.org/components/com_onlinejudge/images/button_pdf.png'></a></span><div style='clear:both'></div>";
		description.setDescription(pdfLink);
		try {
			get = new HttpGet("/external/" + category + "/" + problem.getOriginProb() + ".html");
			response = client.execute(host, get);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
			html = HtmlHandleUtil.transformUrlToAbs(html, host.toURI() + get.getURI());
		} finally {
			EntityUtils.consume(entity);
		}
		description.setDescription(pdfLink + Tools.regFind(html, "^([\\s\\S]*?)<H2><FONT size=4 COLOR=#ff0000><A NAME=\"SECTION000100\\d000000000000000\">"));
		description.setInput(Tools.regFind(html, "Int?put</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*?)<H2><FONT size=4 COLOR=#ff0000><A NAME=\"SECTION000100\\d000000000000000\">"));
		description.setOutput(Tools.regFind(html, "Output</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*?)<H2><FONT size=4 COLOR=#ff0000><A NAME=\"SECTION000100\\d000000000000000\">"));
		description.setSampleInput(Tools.regFind(html, "Sample Int?put</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*?)<H2><FONT size=4 COLOR=#ff0000><A NAME=\"SECTION000100\\d000000000000000\">"));
		description.setSampleOutput(Tools.regFind(html, "Sample Output</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*)"));

		if (description.getSampleInput().isEmpty() || description.getSampleOutput().isEmpty()) {
			description.setDescription(pdfLink + html);
			description.setInput(null);
			description.setOutput(null);
			description.setSampleInput(null);
			description.setSampleOutput(null);
		}
		description.setDescription("<style type=\"text/css\">h1,h2,h3,h4,h5,h6{margin-bottom:0;}div.textBG p{margin: 0 0 0.0001pt;}</style>" + description.getDescription());
	}

	private Map getProblemInfo(String problemNum, HttpClient client) throws ClientProtocolException, IOException, JSONException {
		HttpEntity entity = null;
		try {
			HttpGet get = new HttpGet("http://uhunt.felix-halim.net/api/p/num/" + problemNum);
			HttpResponse response = client.execute(get);
			entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			return (Map) JSONUtil.deserialize(json);
		} finally {
			EntityUtils.consume(entity);
		}
	}
}
