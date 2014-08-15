package judge.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Embed an ExecutorService upon initialization.
 * @author isun
 *
 * @param <V>
 */
public abstract class Task<V> implements Callable<V>{

	protected ExecutorTaskType taskType;
	protected ThreadPoolExecutor executor;
	protected Future<V> finishSignal;
	
	
	public Task(ExecutorTaskType taskType) {
		super();
		this.taskType = taskType;
		this.executor = ExecutorsHolder.getExecutor(taskType);
	}

	/**
	 * Submit if it hasn't
	 */
	public void submit() {
		if (!submitted()) {
			finishSignal = executor.submit(this);
		}
	}
	
	public boolean submitted() {
		return finishSignal != null;
	}
	
	/**
	 * Submit if it hasn't, then wait to get the executing result
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public V get() throws InterruptedException, ExecutionException {
		submit();
		return finishSignal.get();
	}

}
