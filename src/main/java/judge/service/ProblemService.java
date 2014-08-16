package judge.service;

import java.util.Date;

import judge.bean.Problem;
import judge.remote.crawler.common.ProblemInfoUpdateTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProblemService {
	
	@Autowired
	private BaseService baseService;
	
	/**
	 * (Re-)crawl problem if necessary
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
			new ProblemInfoUpdateTask(problem, baseService).submit();
		}
	}

}
