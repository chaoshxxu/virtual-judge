package judge.account;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import judge.account.config.RemoteAccountOJConfig;

/**
 * Short for remote account task executor
 * @author isun
 *
 */
public class RemoteAccountTaskExecutor {
	
	/**
	 * remoteOj -> repo dedicating to one OJ
	 */
	private Map<String, RemoteAccountRepository> repos = 
			new HashMap<String, RemoteAccountRepository>();
	
	/**
	 * <pre>A RemoteAccountTask must enter this queue twice for: 
	 * 1st. Apply for an account and execute;
	 * 2nd. Return the account, whatever that task finishes normally.
	 * </pre>
	 */
	private LinkedBlockingQueue<RemoteAccountTask<?>> running = 
			new LinkedBlockingQueue<RemoteAccountTask<?>>();
	
	////////////////////////////////////////////////////////////////

	public RemoteAccountTaskExecutor(Map<String, RemoteAccountOJConfig> config) {
		for (String remoteOj : config.keySet()) {
			RemoteAccountOJConfig ojConfig = config.get(remoteOj);
			repos.put(remoteOj, new RemoteAccountRepository(remoteOj, ojConfig, this));
		}
	}
	
	////////////////////////////////////////////////////////////////
	
	public void init() {
		Thread executingThead = new Thread() {
			public void run() {
				while (true) {
					try {
						RemoteAccountTask<?> task = running.take();
						String remoteOj = task.getRemoteOj();
						RemoteAccountRepository repo = repos.get(remoteOj);
						if (repo != null) {
							repo.handle(task);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		executingThead.setDaemon(true);
		executingThead.start();
	}
	
	public void submit(RemoteAccountTask<?> task) {
		running.offer(task);
	}
	
}
