package judge.executor;

public enum ExecutorTaskType {
	GENERAL(50, 600),
	UPDATE_PROBLEM_INFO(40, 60),
	SUBMIT_CODE(40, 60),
	QUERY_SUBMISSION_STATUS(40, 60),
	INIT_UVA_LIVE_PROBLEM_ID_MAP(40, 60),
	INIT_UVA_PROBLEM_ID_MAP(40, 60),

	
	
	
	;
	int maximumConcurrency;
	int keepAliveSeconds;
	ExecutorTaskType(int maximumConcurrency, int keepAliveSeconds) {
		this.maximumConcurrency = maximumConcurrency;
		this.keepAliveSeconds = keepAliveSeconds;
	}
}
