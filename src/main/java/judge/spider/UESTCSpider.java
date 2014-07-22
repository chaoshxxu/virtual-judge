package judge.spider;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import judge.tool.DedicatedHttpClient;
import judge.tool.HtmlHandleUtil;
import judge.tool.MarkdownParser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;
import org.apache.struts2.json.JSONUtil;


public class UESTCSpider extends Spider {

	@SuppressWarnings("unchecked")
	public void crawl() throws Exception{
		HttpHost host = new HttpHost("acm.uestc.edu.cn");
		DedicatedHttpClient client = new DedicatedHttpClient(host);
		String jsonStr = client.get("/problem/data/" + problem.getOriginProb()).getBody();
		
		Map<String, ?> json = (Map<String, ?>) JSONUtil.deserialize(jsonStr);
		Map<String, ?> problemJson = (Map<String, ?>) json.get("problem");
		Long problemId = (Long) problemJson.get("problemId");
		Validate.isTrue(problem.getOriginProb().equals(problemId.toString()));
		
		String title = (String) problemJson.get("title");
		Validate.isTrue(!StringUtils.isEmpty(title));
		
		String _description = (String) problemJson.get("description");
		String _input = (String) problemJson.get("input");
		String _output = (String) problemJson.get("output");
		String _sampleInput = "<pre>" + problemJson.get("sampleInput") + "</pre>";
		String _sampleOutput = "<pre>" + problemJson.get("sampleOutput") + "</pre>";
		String _hint = (String) problemJson.get("hint");
		
		problem.setTitle(title);
		problem.setTimeLimit(((Long) problemJson.get("timeLimit")).intValue());
		problem.setMemoryLimit(((Long) problemJson.get("memoryLimit")).intValue());
		description.setDescription(
				trans(_description) + 
				"<script type='text/x-mathjax-config'>MathJax.Hub.Config({tex2jax: { inlineMath: [['$','$'],['\\[','\\]']] } }); </script>\n" +
		        "<script type='text/javascript' src='http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML'></script>\n" +
		        "<script type='text/javascript'>setTimeout(function(){MathJax.Hub.Queue(['Typeset', MathJax.Hub, 'left_view']);}, 2000);</script>\n");
		description.setInput(trans(_input));
		description.setOutput(trans(_output));
		description.setSampleInput(trans(_sampleInput));
		description.setSampleOutput(trans(_sampleOutput));
		description.setHint(trans(_hint));
		problem.setSource((String) problemJson.get("source"));
		problem.setUrl("http://acm.uestc.edu.cn/#/problem/show/" + problem.getOriginProb());
	}
	
	private String trans(String str) throws UnsupportedEncodingException {
		String mdParsed = MarkdownParser.parse(str);
		String transformed = HtmlHandleUtil.transformUrlToAbsBody(mdParsed, "http://acm.uestc.edu.cn/problem/data/" + problem.getOriginProb());
		return transformed;
	}
}
