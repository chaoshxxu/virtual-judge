package judge.executor;

public enum ExecutorTaskType {
	GENERAL(50, 600),
	UPDATE_PROBLEM_INFO(40, 60),
	INIT_UVA_LIVE_PROBLEM_ID_MAP(40, 60),
	INIT_UVA_PROBLEM_ID_MAP(40, 60),

	
	
	
	;
	int maximumCurrency;
	int keepAliveSeconds;
	ExecutorTaskType(int maximumCurrency, int keepAliveSeconds) {
		this.maximumCurrency = maximumCurrency;
		this.keepAliveSeconds = keepAliveSeconds;
	}
}

