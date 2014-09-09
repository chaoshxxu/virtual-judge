package judge.remote.account;

import java.util.concurrent.ArrayBlockingQueue;

import judge.executor.ExecutorTaskType;
import judge.remote.RemoteOj;
import judge.tool.Handler;
import judge.tool.SpringBean;


public abstract class RemoteAccountTask<V>{
	
	private final ExecutorTaskType executorTaskType;
	private final RemoteOj remoteOj;
	private final String accountId;
	private final String exclusiveCode;
	private Handler<V> handler;
	private RemoteAccount account;
	private ArrayBlockingQueue<Object> resultQueue = new ArrayBlockingQueue<Object>(1);
	private boolean _done = false;
	private boolean _submitted = false;
	
	public RemoteAccountTask(ExecutorTaskType executorTaskType, RemoteOj remoteOj, String accountId, String exclusiveCode, Handler<V> handler) {
		super();
		this.executorTaskType = executorTaskType;
		this.remoteOj = remoteOj;
		this.accountId = accountId;
		this.exclusiveCode = exclusiveCode;
		this.handler = handler;
	}
	
	public RemoteAccountTask(ExecutorTaskType executorTaskType, RemoteOj remoteOj, String accountId, String exclusiveCode) {
		this(executorTaskType, remoteOj, accountId, exclusiveCode, null);
	}
	
	public void done() {
		_done = true;
	}
	
	public boolean isDone() {
		return _done;
	}
	
	public RemoteOj getRemoteOj() {
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
	
	public Handler<V> getHandler() {
		return handler;
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
	
	protected abstract V call(RemoteAccount remoteAccount) throws Exception;
	
}
