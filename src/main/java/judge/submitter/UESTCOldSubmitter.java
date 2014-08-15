package judge.submitter;


public class UESTCOldSubmitter extends Submitter {

	public void work() {
		submission.setStatus("Judging Error 1");
		baseService.addOrModify(submission);
	}

	@Override
	public void waitForUnfreeze() {
	}

}
