package judge.remote.provider.uvalive;

import com.google.gson.reflect.TypeToken;
import judge.executor.CascadeTask;
import judge.executor.ExecutorTaskType;
import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.DedicatedHttpClientFactory;
import judge.httpclient.HttpStatusValidator;
import judge.service.JedisService;
import judge.tool.SpringBean;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
@Component
public class UVALiveProblemIdMapHelper {
    private final static Logger log = LoggerFactory.getLogger(UVALiveProblemIdMapHelper.class);
    private static final String problemIdMap_REDIS_KEY = "vjudge:UVALiveProblemIdMapHelper:problemIdMap";
    private static final String lastUpdateTime_REDIS_KEY = "vjudge:UVALiveProblemIdMapHelper:lastUpdateTime";

    @Autowired
    private JedisService jedisService;

    private Map<String, String> problemIdMap = new ConcurrentHashMap<>();
    private long lastUpdateTime;

    @PostConstruct
    public void init() {
        try {
            problemIdMap = jedisService.get(problemIdMap_REDIS_KEY, new TypeToken<ConcurrentHashMap<String, String>>() {}.getType());
            lastUpdateTime = jedisService.get(lastUpdateTime_REDIS_KEY, Long.class);
        } catch (Throwable t) {
            problemIdMap = new ConcurrentHashMap<>();
            lastUpdateTime = 0L;
        }
        log.info("problemIdMap.size = {}", problemIdMap.size());
        log.info("lastUpdateTime = {}", lastUpdateTime);
    }

    @PreDestroy
    public void destroy() {
        jedisService.set(problemIdMap_REDIS_KEY, problemIdMap);
        jedisService.set(lastUpdateTime_REDIS_KEY, lastUpdateTime);
        log.info("UVALiveProblemIdMapHelper is persisted successfully!");
    }

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