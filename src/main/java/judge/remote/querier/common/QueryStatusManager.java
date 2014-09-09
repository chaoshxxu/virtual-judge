package judge.remote.querier.common;

import judge.bean.Submission;
import judge.executor.ExecutorTaskType;
import judge.executor.Task;
import judge.remote.status.RunningSubmissions;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.submitter.common.SubmissionInfo;
import judge.service.IBaseService;
import judge.tool.Handler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueryStatusManager {
	private final static Logger log = LoggerFactory.getLogger(QueryStatusManager.class);

	/**
	 * If a submission keeps remote status unchanged for more than this
	 * length(in milliseconds), and is still with intermediate status, stop
	 * querying it.
	 */
	private final static long MAX_PATIENCE_LENGTH = 60000L;

	@Autowired
	private IBaseService baseService;

	@Autowired
	protected RunningSubmissions runningSubmissions;

	public void createQuery(final Submission submission) {
		if (submission.getId() == 0) {
			log.error(String.format("Please persist first: %s", runningSubmissions.getLogKey(submission)));
			return;
		}
		if (StringUtils.isBlank(submission.getRealRunId())) {
			log.error(String.format("Please submit first: %s", runningSubmissions.getLogKey(submission)));
			return;
		}
		if (!runningSubmissions.contains(submission.getId())) {
			log.info(String.format("Create query: %s", runningSubmissions.getLogKey(submission)));
			runningSubmissions.add(submission);
			new QueryStatusTask(submission, 0).submit();
		}
	}

	class QueryStatusTask extends Task<Void> {
		private Submission submission;

		public QueryStatusTask(Submission submission, int delaySeconds) {
			super(ExecutorTaskType.QUERY_SUBMISSION_STATUS, delaySeconds);
			this.submission = submission;
		}

		@Override
		public Void call() throws Exception {
			try {
				query(submission);
			} catch (Throwable t) {
				log.error(t.getMessage(), t);
				stopQuery(submission);
			}
			return null;
		}

		private void query(final Submission submission) {
			if (!runningSubmissions.contains(submission.getId())) {
				return;
			}

			final SubmissionInfo info = SubmissionConverter.toInfo(submission);
			Querier querier = QueriersHolder.getQuerier(info.remoteOj);
			querier.query(info, new Handler<SubmissionRemoteStatus>() {
				@Override
				public void handle(SubmissionRemoteStatus remoteStatus) {
					try {
						_handle(remoteStatus);
					} catch (Throwable t) {
						log.error(t.getMessage(), t);
						stopQuery(submission);
					}
				}

				private void _handle(SubmissionRemoteStatus remoteStatus) {
					String originalRawStatus = submission.getStatus();
					submission.setStatusCanonical(remoteStatus.statusType.name());
					submission.setStatus(remoteStatus.rawStatus);
					submission.setTime(remoteStatus.executionTime);
					submission.setMemory(remoteStatus.executionMemory);
					submission.setAdditionalInfo(StringUtils.left(remoteStatus.compilationErrorInfo, 10000));
					submission.setFailCase(remoteStatus.failCase);
					submission.setQueryCount(submission.getQueryCount() + 1);
					if (!StringUtils.equals(originalRawStatus, remoteStatus.rawStatus)) {
						submission.setStatusUpdateTime(remoteStatus.queryTime);
					}
					log.info(String.format("Query(%d): %s", submission.getQueryCount(), runningSubmissions.getLogKey(submission)));

					if ( //
							remoteStatus.statusType.finalized || //
							submission.getStatusUpdateTime() != null && //
							remoteStatus.queryTime.getTime() - submission.getStatusUpdateTime().getTime() > MAX_PATIENCE_LENGTH) {
						stopQuery(submission);
						return;
					}

					int nextQueryDelaySeconds = submission.getQueryCount() + 2;
					new QueryStatusTask(submission, nextQueryDelaySeconds).submit();
				}

				@Override
				public void onError(Throwable t) {
					log.error(t.getMessage(), t);
					if (submission.getSubTime() == null || System.currentTimeMillis() - submission.getSubTime().getTime() > 86400000L) {
						submission.reset();
					}
					stopQuery(submission);
				}
			});
		}

		private void stopQuery(Submission submission) {
			log.info(String.format("Stop query: %s", runningSubmissions.getLogKey(submission)));
			if (runningSubmissions.remove(submission.getId()) != null) {
				baseService.addOrModify(submission);
			}
		}

	}

}
