package judge.remote.crawler.common;

import java.util.Date;

import judge.bean.Description;
import judge.bean.Problem;
import judge.executor.ExecutorTaskType;
import judge.executor.Task;
import judge.remote.RemoteOj;
import judge.service.BaseService;
import judge.service.IBaseService;
import judge.tool.Handler;
import judge.tool.SpringBean;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProblemInfoUpdateTask extends Task<Void>{
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
                if ("0".equals(desc.getAuthor())){
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