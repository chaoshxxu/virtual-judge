package judge.spider;


import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;


public class SGUSpider extends Spider {


	public void crawl() throws Exception{

		String html = "";
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod("http://acm.sgu.ru/problem.php?contest=0&problem=" + problem.getOriginProb());
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

		if (html.contains("<h4>no such problem</h4>")){
			throw new Exception();
		}

		html = HtmlHandleUtil.transformUrlToAbs(html, getMethod.getURI().toString());

		String tl = Tools.regFind(html, "ime limit per test: ([\\d\\.]*)");
		if (tl.length() > 0){
			problem.setTimeLimit((int)(1000 * Double.parseDouble(tl)));
		}

		String ml = Tools.regFind(html, "emory limit per test: ([\\d]*)");
		if (ml.length() > 0){
			problem.setMemoryLimit(Integer.parseInt(ml));
		} else {
			ml = Tools.regFind(html, "emory limit: ([\\d]*)");
			if (ml.length() > 0){
				problem.setMemoryLimit(Integer.parseInt(ml));
			}
		}

		problem.setTitle(Tools.regFind(html, problem.getOriginProb() + "\\.([\\s\\S]*?)</[th]", 1).trim());

		description.setInput(null);
		description.setOutput(null);
		description.setSampleInput(null);
		description.setSampleOutput(null);
		description.setHint(null);

		if (html.contains("<title> SSU Online Contester")) {
			description.setDescription(Tools.regFind(html, "output: standard </div><br />([\\s\\S]*?)<br /><br /><div align=\"left\" style=\"margin-top:1em;\"><b>Input</b>"));
			description.setInput(Tools.regFind(html, "<b>Input</b></div>([\\s\\S]*?)<br /><br /><div align=\"left\" style=\"margin-top:1em;\"><b>Output</b>"));
			description.setOutput(Tools.regFind(html, "<b>Output</b></div>([\\s\\S]*?)<br /><br /><div align=\"left\" style=\"margin-top:1em;\"><b>Example\\(s\\)</b>"));
			description.setSampleInput(Tools.regFind(html, "<b>Example\\(s\\)</b></div>([\\s\\S]*?)<br /><br />(<div align=\"left\" style=\"margin-top:1em;\"><b>Note</b>|</div><hr />)"));
			description.setHint(Tools.regFind(html, "<b>Note</b></div>([\\s\\S]*?)<br /><br /></div><hr />"));
		} else if (html.contains("<title>Saratov State University")) {
			description.setDescription(Tools.regFind(html, "output:\\s*standard[\\s\\S]*?</div><br><br><br>([\\s\\S]*?)<div align = left><br><b>Input</b>"));
			description.setInput(Tools.regFind(html, "<b>Input</b></div>([\\s\\S]*?)<div align = left><br><b>Output</b>"));
			description.setOutput(Tools.regFind(html, "<b>Output</b></div>([\\s\\S]*?)<div align = left><br><b>Sample test"));
			description.setSampleInput(Tools.regFind(html, "<b>Sample test\\(s\\)</b></div>([\\s\\S]*?)(<div align = left><br><b>Note</b>|<div align = right>)"));
			description.setHint(Tools.regFind(html, "<b>Note</b></div>([\\s\\S]*?)<div align = right>"));
		}
		if (description.getOutput() == null || description.getOutput().isEmpty()) {
			description.setDescription(html.replaceAll("(?i)[\\s\\S]*\\d{3,}\\s*KB\\s*</P>", "").replaceAll("(?i)</?(body|html)>", ""));
		}
		problem.setSource(Tools.regFind(html, "Resource:</td><td>([\\s\\S]*?)\n</td>"));
		problem.setUrl("http://acm.sgu.ru/problem.php?contest=0&problem=" + problem.getOriginProb());
	}
}
