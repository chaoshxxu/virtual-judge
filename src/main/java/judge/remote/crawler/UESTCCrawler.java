package judge.remote.crawler;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import judge.executor.ExecutorTaskType;
import judge.executor.Task;
import judge.remote.RemoteOj;
import judge.remote.crawler.common.RawProblemInfo;
import judge.remote.crawler.common.SimpleCrawler;
import judge.tool.HtmlHandleUtil;
import judge.tool.MarkdownParser;

import org.apache.commons.lang3.Validate;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.springframework.stereotype.Component;

@Component
public class UESTCCrawler extends SimpleCrawler {
	
	@Override
	public RemoteOj getOj() {
		return RemoteOj.UESTC;
	}

	@Override
	protected String getProblemUrl(String problemId) {
		return getHost().toURI() + "/problem/data/" + problemId;
	}
	
	@Override
	protected void preValidate(String problemId) {
		Validate.isTrue(problemId.matches("[1-9]\\d*"));
	}
	
	@Override
	protected boolean autoTransformAbsoluteHref() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void populateProblemInfo(RawProblemInfo info, String problemId, String jsonStr) throws InterruptedException, ExecutionException, JSONException {
		Map<String, ?> json = (Map<String, ?>) JSONUtil.deserialize(jsonStr);
		Map<String, ?> problemJson = (Map<String, ?>) json.get("problem");
		
		info.title = (String) problemJson.get("title");
		
		String _description = (String) problemJson.get("description");
		String _input = (String) problemJson.get("input");
		String _output = (String) problemJson.get("output");
		String _sampleInput = "<pre>" + problemJson.get("sampleInput") + "</pre>";
		String _sampleOutput = "<pre>" + problemJson.get("sampleOutput") + "</pre>";
		String _hint = (String) problemJson.get("hint");
		
		info.timeLimit = (((Long) problemJson.get("timeLimit")).intValue());
		info.memoryLimit = (((Long) problemJson.get("memoryLimit")).intValue());
		
		ParseTask parseDescriptionTask = new ParseTask(_description, problemId);
		ParseTask parseInputTask = new ParseTask(_input, problemId);
		ParseTask parseOutputTask = new ParseTask(_output, problemId);
		ParseTask parseSampleInputTask = new ParseTask(_sampleInput, problemId);
		ParseTask parseSampleOutputTask = new ParseTask(_sampleOutput, problemId);
		ParseTask parseHintTask = new ParseTask(_hint, problemId);
		
		parseDescriptionTask.submit();
		parseInputTask.submit();
		parseOutputTask.submit();
		parseSampleInputTask.submit();
		parseSampleOutputTask.submit();
		parseHintTask.submit();
		
		info.description = (
				parseDescriptionTask.get() + 
				"<script type='text/x-mathjax-config'>MathJax.Hub.Config({tex2jax: { inlineMath: [['$','$'],['\\[','\\]']] } }); </script>\n" +
		        "<script type='text/javascript' src='http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML'></script>\n" +
		        "<script type='text/javascript'>setTimeout(function(){MathJax.Hub.Queue(['Typeset', MathJax.Hub, 'left_view']);}, 2000);</script>\n");
		info.input = parseInputTask.get();
		info.output = parseOutputTask.get();
		info.sampleInput = parseSampleInputTask.get();
		info.sampleOutput = parseSampleOutputTask.get();
		info.hint = parseHintTask.get();
		info.source = ((String) problemJson.get("source"));
		info.url = (getHost().toURI() + "/#/problem/show/" + problemId);	
	}
	
	class ParseTask extends Task<String> {
		
		private String str;
		private String problemId;
		
		public ParseTask(String str, String problemId) {
			super(ExecutorTaskType.GENERAL);
			this.str = str;
			this.problemId = problemId;
		}

		@Override
		public String call() throws Exception {
			String mdParsed = MarkdownParser.parse(str);
			String transformed = HtmlHandleUtil.transformUrlToAbsBody(mdParsed, getHost().toURI() + "/#/problem/show/" + problemId);
			return transformed;
		}
		
	}


}
