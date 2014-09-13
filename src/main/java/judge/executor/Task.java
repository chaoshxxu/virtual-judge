package judge.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import judge.tool.SpringBean;

public abstract class Task<V> implements Callable<V>{

	protected ExecutorTaskType taskType;
	protected int delaySeconds;
	
	/**
	 * For immediate executing task
	 */
	private Future<V> finishSignal;
	
	/**
	 * For delayed executing task
	 */
	private ScheduledFuture<Future<V>> scheduledFinishSignal;

	
	//////////////////////////////////////////////////////////////////////////////
	
	public Task(ExecutorTaskType taskType) {
		this(taskType, 0);
	}

	public Task(ExecutorTaskType taskType, int delaySeconds) {
		super();
		this.taskType = taskType;
		this.delaySeconds = delaySeconds;
	}

	//////////////////////////////////////////////////////////////////////////////

	/**
	 * Submit if it hasn't
	 */
	public void submit() {
		if (!submitted()) {
			TaskExecutor executor = SpringBean.getBean(TaskExecutor.class);
			if (delaySeconds > 0) {
				scheduledFinishSignal = executor.submitDelay(this);
			} else {
				finishSignal = executor.submitNoDelay(this);
			}
		}
	}
	
	public boolean submitted() {
		return finishSignal != null || scheduledFinishSignal != null;
	}
	
	/**
	 * Submit if it hasn't, then wait to get the executing result
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public V get() throws InterruptedException, ExecutionException {
		submit();
		if (finishSignal == null) {
			finishSignal = scheduledFinishSignal.get();
		}
		return finishSignal.get();
	}

}
