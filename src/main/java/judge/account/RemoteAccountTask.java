package judge.account;

import java.util.concurrent.ArrayBlockingQueue;

import judge.executor.ExecutorTaskType;
import judge.tool.SpringBean;


public abstract class RemoteAccountTask<V>{
	
	private final ExecutorTaskType executorTaskType;
	private final String remoteOj;
	private final String accountId;
	private final String exclusiveCode;
	private RemoteAccount account;
	private ArrayBlockingQueue<Object> resultQueue = new ArrayBlockingQueue<Object>(1);
	private boolean _done = false;
	private boolean _submitted = false;
	
	public RemoteAccountTask(ExecutorTaskType executorTaskType, String remoteOj, String accountId, String exclusiveCode) {
		super();
		this.executorTaskType = executorTaskType;
		this.remoteOj = remoteOj;
		this.accountId = accountId;
		this.exclusiveCode = exclusiveCode;
	}
	
	public void done() {
		_done = true;
	}
	
	public boolean isDone() {
		return _done;
	}
	
	public String getRemoteOj() {
		return remoteOj;
	}
	
	public String getAccountId() {
		return accountId;
	}

	public String getExclusiveCode() {
		return exclusiveCode;
	}
	
	public RemoteAccount getAccount() {
		return account;
	}

	public void setAccount(RemoteAccount account) {
		this.account = account;
	}
	
	public ExecutorTaskType getExecutorTaskType() {
		return executorTaskType;
	}

	/**
	 * Don't call it !
	 * @param result
	 */
	public void offerResult(Object result) {
		resultQueue.offer(result);
	}

	/////////////////////////////////////////////////////////////////

	public void submit() {
		if (!_submitted) {
			_submitted = true;
			SpringBean.getBean(RemoteAccountTaskExecutor.class).submit(this);
		}
	}
	
	@SuppressWarnings("unchecked")
	public V get() throws InterruptedException {
		submit();
		Object result = resultQueue.take();
		if (result instanceof Throwable) {
			throw new RuntimeException((Throwable)result);
		} else {
			return (V) result;
		}
	}
	
	/////////////////////////////////////////////////////////////////
	
	protected abstract V call(RemoteAccount remoteAccount);
	
}
