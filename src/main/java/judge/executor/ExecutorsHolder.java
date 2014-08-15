package judge.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorsHolder {

	private static Map<ExecutorTaskType, ThreadPoolExecutor> executors = new HashMap<ExecutorTaskType, ThreadPoolExecutor>();

	static public ThreadPoolExecutor getExecutor(ExecutorTaskType taskGroup) {
		if (!executors.containsKey(taskGroup)) {
			synchronized (executors) {
				if (!executors.containsKey(taskGroup)) {
					ThreadPoolExecutor executor = new ThreadPoolExecutor(
							taskGroup.maximumCurrency,
							Integer.MAX_VALUE,
							taskGroup.keepAliveSeconds,
							TimeUnit.SECONDS,
							new LinkedBlockingDeque<Runnable>());
					executor.allowCoreThreadTimeOut(true);
					executors.put(taskGroup, executor);
				}
			}
		}
		return executors.get(taskGroup);
	}

}
