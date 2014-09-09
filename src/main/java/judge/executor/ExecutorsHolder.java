package judge.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorsHolder {

	private static Map<ExecutorTaskType, ThreadPoolExecutor> executors = new HashMap<ExecutorTaskType, ThreadPoolExecutor>();
	private static ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(40);

	static public ThreadPoolExecutor getExecutor(ExecutorTaskType taskGroup) {
		if (!executors.containsKey(taskGroup)) {
			synchronized (executors) {
				if (!executors.containsKey(taskGroup)) {
					ThreadPoolExecutor executor = new ThreadPoolExecutor(
							taskGroup.maximumConcurrency,
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
	
	static public ScheduledExecutorService getScheduledExecutor() {
		return scheduledExecutor;
	}

}
