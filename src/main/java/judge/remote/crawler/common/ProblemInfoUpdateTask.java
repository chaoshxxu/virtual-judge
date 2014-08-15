package judge.remote.crawler.common;

import java.util.Date;

import judge.bean.Description;
import judge.bean.Problem;
import judge.executor.ExecutorTaskType;
import judge.executor.Task;
import judge.service.IBaseService;

public class ProblemInfoUpdateTask extends Task<Void>{
	
	private Problem problem;
	private IBaseService baseService;
	
	public ProblemInfoUpdateTask(Problem problem, IBaseService baseService) {
		super(ExecutorTaskType.UPDATE_PROBLEM_INFO);
		this.problem = problem;
		this.baseService = baseService;
	}

	@Override
	public Void call() {
		try {
			Crawler crawler = CrawlersHolder.getCrawler(problem.getOriginOJ());
			RawProblemInfo info = crawler.crawl(problem.getOriginProb());

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
		} catch (Exception e) {
			e.printStackTrace();
			if (problem.getDescriptions() == null || problem.getDescriptions().isEmpty()) {
				// Never crawled successfully
				baseService.delete(problem);
			} else {
				problem.setTimeLimit(2);
				baseService.addOrModify(problem);
			}
		}
		return null;
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