package judge.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import judge.bean.Contest;
import judge.bean.Submission;
import judge.remote.status.RemoteStatusUpdateEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContestSubmissionMonitor implements RemoteStatusUpdateEvent.Listener {
	
	private Map<Integer, Long> updateTimes = new HashMap<Integer, Long>();

	@Autowired
	private BaseService baseService;

	@Autowired
	private JudgeService judgeService;

	@Autowired
	public ContestSubmissionMonitor(RemoteStatusUpdateEvent remoteStatusUpdateEvent) {
		remoteStatusUpdateEvent.addListener(this);
	}

	@Override
	public void onStatusUpdate(Submission submission) throws Exception {
		Contest contest = submission.getContest();
		if (contest != null) {
			updateTimes.put(contest.getId(), System.currentTimeMillis());
			contest = (Contest) baseService.query(Contest.class, contest.getId());
			judgeService.updateRankData(contest);
		}
	}
	
	/**
	 * -_-!
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public long getLastSubmissionUpdateTime(int contestId) {
		Long lastSubmissionUpdateTime = updateTimes.get(contestId);
		if (lastSubmissionUpdateTime == null) {
			List<Date> statusLastUpdateTimes = baseService.query("select max(s.statusUpdateTime) from Submission s where s.contest.id = " + contestId);
			if (statusLastUpdateTimes.size() > 0) {
				updateTimes.put(contestId, statusLastUpdateTimes.get(0).getTime());
			}
		}
		return lastSubmissionUpdateTime == null ? 0 : lastSubmissionUpdateTime; 
	}

}
