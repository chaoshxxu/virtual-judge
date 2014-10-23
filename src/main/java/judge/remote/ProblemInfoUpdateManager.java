package judge.remote;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import judge.bean.Description;
import judge.bean.Problem;
import judge.executor.ExecutorTaskType;
import judge.executor.Task;
import judge.remote.crawler.Crawler;
import judge.remote.crawler.CrawlersHolder;
import judge.remote.crawler.RawProblemInfo;
import judge.service.BaseService;
import judge.service.IBaseService;
import judge.service.JudgeService;
import judge.tool.Handler;
import judge.tool.SpringBean;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProblemInfoUpdateManager {
    private final static Logger log = LoggerFactory.getLogger(ProblemInfoUpdateManager.class);

    @Autowired
    private BaseService baseService;

    @Autowired
    private JudgeService judgeService;

    /**
     * OJ+PID -> lastTriggerTime
     */
    private ConcurrentHashMap<String, Date> triggerCache = new ConcurrentHashMap<String, Date>();

    /**
     * (Re-)crawl problem if any of the following is satisfied:
     * 1. Haven't tried crawling for more than 7 days;
     * 2. Haven't tried crawling for more than 10 minutes, since the last crawling, which failed,
     * 3. Enforce == true, and ((not crawling) or (crawling for more than 1 hour))
     *
     * @param problem
     * @param enforce
     */
    public void updateProblem(Problem problem, boolean enforce) {
        long sinceTriggerTime = Long.MAX_VALUE;
        if (problem.getTriggerTime() != null) {
            sinceTriggerTime = System.currentTimeMillis() - problem.getTriggerTime().getTime();
        }
        boolean condition1 = sinceTriggerTime > 7L * 86400L * 1000L;
        boolean condition2 = problem.getTimeLimit() == 2 && sinceTriggerTime > 600L * 1000L;
        boolean condition3 = enforce && (problem.getTimeLimit() != 1 || sinceTriggerTime > 3600L * 1000L);
        if (condition1 || condition2 || condition3) {
            problem.setTimeLimit(1);
            problem.setTriggerTime(new Date());
            baseService.addOrModify(problem);
            new ProblemInfoUpdateTask(problem).submit();

            triggerCache.put(problem.getOriginOJ() + problem.getOriginProb(), new Date());
            if (triggerCache.size() > 1000) {
                triggerCache.clear();
            }
        }
    }

    public void updateProblem(String remoteOj, String remoteProblemId, boolean enforce) {
        if (remoteOj == null || remoteProblemId == null || remoteProblemId.length() > 36) {
            log.error("Illegal remoteOJ or remoteProblemId");
            return;
        }

        remoteOj = remoteOj.trim();
        remoteProblemId = remoteProblemId.trim();

        Problem problem = judgeService.findProblem(remoteOj, remoteProblemId);
        if (problem == null) {
            problem = new Problem();
            problem.setOriginOJ(remoteOj);
            problem.setOriginProb(remoteProblemId);
            problem.setTriggerTime(triggerCache.get(remoteOj + remoteProblemId));
            problem.setTitle("N/A");
        }
        updateProblem(problem, enforce);
    }

}

class ProblemInfoUpdateTask extends Task<Void> {
    private final static Logger log = LoggerFactory.getLogger(ProblemInfoUpdateTask.class);

    private Problem problem;
    private IBaseService baseService = SpringBean.getBean(BaseService.class);

    public ProblemInfoUpdateTask(Problem problem) {
        super(ExecutorTaskType.UPDATE_PROBLEM_INFO);
        this.problem = problem;
    }

    @Override
    public Void call() throws Exception {
        Crawler crawler = null;
        try {
            crawler = CrawlersHolder.getCrawler(RemoteOj.valueOf(problem.getOriginOJ()));
            Validate.notNull(crawler);
        } catch (Throwable t) {
            _onError(t);
            return null;
        }
        crawler.crawl(problem.getOriginProb(), new Handler<RawProblemInfo>() {

            @Override
            public void handle(RawProblemInfo info) {
                problem.setTitle(info.title);
                problem.setTimeLimit(info.timeLimit);
                problem.setMemoryLimit(info.memoryLimit);
                problem.setSource(info.source);
                problem.setUrl(info.url);

                Description description = getSystemDescription();
                description.setDescription(info.description);
                description.setInput(info.input);
                description.setOutput(info.output);
                description.setSampleInput(info.sampleInput);
                description.setSampleOutput(info.sampleOutput);
                description.setHint(info.hint);
                description.setUpdateTime(new Date());

                baseService.addOrModify(problem);
                baseService.addOrModify(description);
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage(), t);
                _onError(t);
            }
        });

        return null;
    }

    private void _onError(Throwable t) {
        t.printStackTrace();
        if (problem.getDescriptions() == null || problem.getDescriptions().isEmpty()) {
            // Never crawled successfully
            baseService.delete(problem);
        } else {
            problem.setTimeLimit(2);
            baseService.addOrModify(problem);
        }
    }

    private Description getSystemDescription() {
        if (problem.getDescriptions() != null) {
            for (Description desc : problem.getDescriptions()) {
                if ("0".equals(desc.getAuthor())) {
                    return desc;
                }
            }
        }
        Description description = new Description();
        description.setAuthor("0");
        description.setRemarks("Initialization.");
        description.setVote(0);
        description.setProblem(problem);

        return description;
    }

}
