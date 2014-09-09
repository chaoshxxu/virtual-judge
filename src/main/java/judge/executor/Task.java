package judge.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Embed an ExecutorService upon initialization.
 * @author Isun
 *
 * @param <V>
 */
public abstract class Task<V> implements Callable<V>{

	protected ExecutorTaskType taskType;
	protected Future<V> finishSignal;
	protected int delaySeconds;
	
	public Task(ExecutorTaskType taskType) {
		this(taskType, 0);
	}

	public Task(ExecutorTaskType taskType, int delaySeconds) {
		super();
		this.taskType = taskType;
		this.delaySeconds = delaySeconds;
	}

	/**
	 * Submit if it hasn't
	 */
	public void submit() {
		if (!submitted()) {
			if (delaySeconds > 0) {
				finishSignal = ExecutorsHolder.getScheduledExecutor().schedule(this, delaySeconds, TimeUnit.SECONDS);
			} else {
				finishSignal = ExecutorsHolder.getExecutor(taskType).submit(this);
			}
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
