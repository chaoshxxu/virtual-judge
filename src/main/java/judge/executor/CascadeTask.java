package judge.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * It accepts binding child tasks and has a short cut to wait until all the
 * children tasks complete.
 * 
 * @author Isun
 * 
 */
public abstract class CascadeTask<V> extends Task<V> {

	private List<Task<?>> childrenTasks = new ArrayList<Task<?>>();

	public CascadeTask(ExecutorTaskType taskType) {
		super(taskType);
	}

	/**
	 * Try submitting the given task if hasn't. Then bind it as a child to the
	 * current task. It will be useful when calling Task.get()
	 * 
	 * @param childTask
	 */
	protected void addChildTask(Task<?> childTask) {
		childTask.submit();
		childrenTasks.add(childTask);
	}

	/**
	 * Submit if it hasn't, then wait until it finishes && all children tasks
	 * finish, then get the result.
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Override
	public V get() throws InterruptedException, ExecutionException {
		V taskResult = super.get();
		for (Task<?> childTask : childrenTasks) {
			childTask.submit();
		}
		for (Task<?> childTask : childrenTasks) {
			childTask.get();
		}
		return taskResult;
	}

}
