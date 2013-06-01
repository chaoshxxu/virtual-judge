package judge.submitter;

import javax.servlet.ServletContext;

import org.apache.commons.httpclient.HttpClient;

import judge.bean.Submission;
import judge.service.IBaseService;
import judge.service.JudgeService;
import judge.tool.ApplicationContainer;
import judge.tool.SpringBean;

public abstract class Submitter extends Thread {
	static public ServletContext sc = ApplicationContainer.sc;
	static public IBaseService baseService = (IBaseService) SpringBean.getBean("baseService", sc);
	static public JudgeService judgeService = (JudgeService) SpringBean.getBean("judgeService", sc);

	public Submission submission;

	protected HttpClient httpClient;
	protected int maxRunId = 0;
	protected int idx;

	public abstract void work();
	public abstract void waitForUnfreeze();

	public void run() {
		work();
		updateStanding();
		waitForUnfreeze();
	}

	private void updateStanding() {
		if (submission.getContest() != null){
			try {
				judgeService.updateRankData(submission.getContest().getId(), true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Submission getSubmission() {
		return submission;
	}
	public void setSubmission(Submission submission) {
		this.submission = submission;
	}

}
