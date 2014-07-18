package judge.spider;

import java.util.Map;

import judge.tool.DedicatedHttpClient;
import judge.tool.MultipleProxyHttpClientFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.struts2.json.JSONUtil;


public class UESTCSpider extends Spider {

	@SuppressWarnings("unchecked")
	public void crawl() throws Exception{
		HttpHost host = new HttpHost("acm.uestc.edu.cn");
		HttpClient delegateClient = MultipleProxyHttpClientFactory.getInstance("UESTC");
		DedicatedHttpClient client = new DedicatedHttpClient(host, delegateClient);
		String jsonStr = client.get("/problem/data/" + problem.getOriginProb()).getBody();
//		html = HtmlHandleUtil.transformUrlToAbs(html, host.toURI() + get.getURI());
		
		Map<String, ?> json = (Map<String, ?>) JSONUtil.deserialize(jsonStr);
		Map<String, ?> problemJson = (Map<String, ?>) json.get("problem");
		Long problemId = (Long) problemJson.get("problemId");
		Validate.isTrue(problem.getOriginProb().equals(problemId.toString()));
		
		String title = (String) problemJson.get("title");
		Validate.isTrue(!StringUtils.isEmpty(title));
		
		problem.setTitle(title);
		problem.setTimeLimit(((Long) problemJson.get("timeLimit")).intValue());
		problem.setMemoryLimit(((Long) problemJson.get("memoryLimit")).intValue());
		description.setDescription((String) problemJson.get("description"));
		description.setInput((String) problemJson.get("input"));
		description.setOutput((String) problemJson.get("output"));
		description.setSampleInput((String) problemJson.get("sampleInput"));
		description.setSampleOutput((String) problemJson.get("sampleOutput"));
		description.setHint((String) problemJson.get("hint"));
		problem.setSource((String) problemJson.get("source"));
		problem.setUrl("http://acm.uestc.edu.cn/#/problem/show/" + problem.getOriginProb());
	}
}
