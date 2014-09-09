package judge.remote.crawler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.executor.CascadeTask;
import judge.executor.ExecutorTaskType;
import judge.executor.Task;
import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.DedicatedHttpClientFactory;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleHttpResponse;
import judge.remote.RemoteOj;
import judge.remote.crawler.common.RawProblemInfo;
import judge.remote.crawler.common.SyncCrawler;
import judge.tool.HtmlHandleUtil;
import judge.tool.SpringBean;
import judge.tool.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UVALiveCrawler extends SyncCrawler {
	
	private static UVALiveProblemIdMapHelper helper = new UVALiveProblemIdMapHelper();

	@Override
	public RemoteOj getOj() {
		return RemoteOj.UVALive;
	}

	@Override
	public RawProblemInfo crawl(final String problemId1) throws Exception {
		final String problemId2 = helper.getProblemId2(problemId1);
		Validate.isTrue(!StringUtils.isEmpty(problemId2));
		
		final HttpHost host = new HttpHost("icpcarchive.ecs.baylor.edu", 443, "https");
		final DedicatedHttpClient client = dedicatedHttpClientFactory.build(host);

		final String outerUrl = host.toURI() + "/index.php?option=com_onlinejudge&Itemid=8&page=show_problem&problem=" + problemId2;
		Task<String> taskOuter = new Task<String>(ExecutorTaskType.GENERAL) {
			@Override
			public String call() throws Exception {
				String html = client.get(outerUrl, HttpStatusValidator.SC_OK).getBody();
				return HtmlHandleUtil.transformUrlToAbs(html, outerUrl);
			}
		};
		
		Task<String> taskInner = new Task<String>(ExecutorTaskType.GENERAL) {
			@Override
			public String call() throws Exception {
				String url = host.toURI() + "/external/" + Integer.parseInt(problemId1) / 100 + "/" + problemId1 + ".html";
				SimpleHttpResponse response = client.get(url);
				if (response.getStatusCode() == HttpStatus.SC_OK) {
					String html = client.get(url, HttpStatusValidator.SC_OK).getBody();
					html = HtmlHandleUtil.transformUrlToAbs(html, url);
					//some problems' description are fucking long, only get the body.innerHTML
					return html.replaceAll("(?i)^[\\s\\S]*<body[^>]*>", "").replaceAll("(?i)</body>[\\s\\S]*", "");
				} else {
					return "";
				}
			}
		};
		
		taskOuter.submit();
		taskInner.submit();
		String htmlOuter = taskOuter.get();
		String htmlInner = taskInner.get();

		RawProblemInfo info = new RawProblemInfo();
		info.title = Tools.regFind(htmlOuter, "<h3>" + problemId1 + " - (.+?)</h3>").trim();
		info.timeLimit = Integer.parseInt(Tools.regFind(htmlOuter, "Time limit: ([\\d\\.]+)").replaceAll("\\.", ""));
		info.memoryLimit = 0;
		info.source = Tools.regFind(htmlOuter, problemId1 + " - [^<>]*</h3>([\\s\\S]*?)<br />").trim();
		info.url = outerUrl;
		
		String descriptionPrefix = 
				"<style type=\"text/css\">h1,h2,h3,h4,h5,h6{margin-bottom:0;}div.textBG p{margin: 0 0 0.0001pt;}</style>" +
				"<span style='float:right'><a target='_blank' href='https://icpcarchive.ecs.baylor.edu/external/" + Integer.parseInt(problemId1) / 100 + "/" + problemId1 + ".pdf'><img width='100' height='26' border='0' title='Download as PDF' alt='Download as PDF' src='https://icpcarchive.ecs.baylor.edu/components/com_onlinejudge/images/button_pdf.png'></a></span><div style='clear:both'></div>";
		String sampleInput = Tools.regFind(htmlInner, "Sample Int?put</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*?)<H2><FONT size=\"4\" COLOR=\"#ff0000\"><A NAME=\"SECTION000100\\d000000000000000\"");
		String sampleOutput = Tools.regFind(htmlInner, "Sample Output</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*)");
		if (StringUtils.isEmpty(sampleInput) || StringUtils.isEmpty(sampleOutput)) {
			info.description = descriptionPrefix + htmlInner;
		} else {
			info.description = descriptionPrefix + Tools.regFind(htmlInner, "^([\\s\\S]*?)<H2><FONT size=\"4\" COLOR=\"#ff0000\"><A NAME=\"SECTION000100\\d000000000000000\"");
			info.input = Tools.regFind(htmlInner, "Int?put</A>&nbsp;</FONT></H2>([\\s\\S]*?)<H2><FONT size=\"4\" COLOR=\"#ff0000\"><A NAME=\"SECTION000100\\d000000000000000\"");
			info.output = Tools.regFind(htmlInner, "Output</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*?)<H2><FONT size=\"4\" COLOR=\"#ff0000\"><A NAME=\"SECTION000100\\d000000000000000\"");
			info.sampleInput = sampleInput;
			info.sampleOutput = sampleOutput;
		}
		
		return info;
	}
}

/**
 * <pre>
 * UVA live has two standards of "problem id".
 * For example, to access the problem "2000 - Wrap it up", you need to know another magic number other than "2000", it's "1".
 * 
 * The URL pointing to the page containing problem title and time limit is:
 * https://icpcarchive.ecs.baylor.edu/index.php?option=com_onlinejudge&Itemid=8&page=show_problem&problem=1 
 * 
 * The URL pointing to the page containing description is:
 * https://icpcarchive.ecs.baylor.edu/external/20/2000.html
 * 
 * This class helps to get "1" given "2000".
 * To distinguish them, I'd like to call "2000" problem_id_1, and call "1" problem_id_2.
 * </pre>
 * 
 * @author Isun
 * 
 */
class UVALiveProblemIdMapHelper {
	private final static Logger log = LoggerFactory.getLogger(UVALiveProblemIdMapHelper.class);

	private Map<String, String> problemIdMap = new ConcurrentHashMap<String, String>();
	private long lastUpdateTime;
	
	public synchronized String getProblemId2(String problemId1) throws InterruptedException, ExecutionException {
		if (!problemIdMap.containsKey(problemId1) && System.currentTimeMillis() - lastUpdateTime > 3600000L) {
			lastUpdateTime = System.currentTimeMillis();
			
			UVALiveProblemIdCrawlTask task = new UVALiveProblemIdCrawlTask(0, problemIdMap, new ConcurrentHashMap<Integer, Boolean>());
			
			long begin = System.currentTimeMillis();
			task.get();
			log.info("UVA live problem id map init cost " + (System.currentTimeMillis() - begin) + "ms");
		}
		return problemIdMap.get(problemId1);
	}
}

class UVALiveProblemIdCrawlTask extends CascadeTask<Boolean> {
	private final static Logger log = LoggerFactory.getLogger(UVALiveProblemIdCrawlTask.class);

	private int category;
	private Map<String, String> problemIdMap;
	private Map<Integer, Boolean> visitedCategories;

	public UVALiveProblemIdCrawlTask(
			int category, 
			Map<String, String> problemIdMap, 
			Map<Integer, Boolean> visitedCategories) {
		super(ExecutorTaskType.INIT_UVA_LIVE_PROBLEM_ID_MAP);
		this.category = category;
		this.problemIdMap = problemIdMap;
		this.visitedCategories = visitedCategories;
	}

	@Override
	public Boolean call() {
		if (visitedCategories.containsKey(category)) {
			return null;
		}
		visitedCategories.put(category, true);
		
		log.info("> UVA live problem id mapping, category = " + category);
		
		HttpHost host = new HttpHost("icpcarchive.ecs.baylor.edu", 443, "https");
		DedicatedHttpClient client = SpringBean.getBean(DedicatedHttpClientFactory.class).build(host);
		String listPageUrl = "/index.php?option=com_onlinejudge&Itemid=8&limit=1000&limitstart=0&category=" + category;

		String html = client.get(listPageUrl, HttpStatusValidator.SC_OK).getBody();
		
		// 1. find problem
		Matcher matcher = Pattern.compile("problem=(\\d+)\">(\\d+)").matcher(html);
		while (matcher.find()) {
			String problemId1 = matcher.group(2);
			String problemId2 = matcher.group(1);
			problemIdMap.put(problemId1, problemId2);
		}
		
		// 2. find sub-category
		matcher = Pattern.compile("category=(\\d+)").matcher(html);
		while (matcher.find()) {
			int newCategory = Integer.parseInt(matcher.group(1));
			UVALiveProblemIdCrawlTask childTask = new UVALiveProblemIdCrawlTask(newCategory, problemIdMap, visitedCategories);
			addChildTask(childTask);
		}
		log.info("< UVA live problem id mapping, category = " + category);
		return null;
	}

}