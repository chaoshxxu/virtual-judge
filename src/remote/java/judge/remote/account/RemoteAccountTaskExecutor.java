package judge.remote.account;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import judge.remote.RemoteOj;
import judge.remote.account.config.RemoteAccountOJConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Short for remote account task executor
 * @author Isun
 *
 */
public class RemoteAccountTaskExecutor {
    private final static Logger log = LoggerFactory.getLogger(RemoteAccountTaskExecutor.class);
    
    /**
     * remoteOj -> repo dedicating to one OJ
     */
    private Map<RemoteOj, RemoteAccountRepository> repos = 
            new HashMap<RemoteOj, RemoteAccountRepository>();
    
    /**
     * <pre>A RemoteAccountTask must enter this queue twice for: 
     * 1st. Apply for an account and execute;
     * 2nd. Return the account, whatever that task finishes normally.
     * </pre>
     */
    private LinkedBlockingQueue<RemoteAccountTask<?>> running = 
            new LinkedBlockingQueue<RemoteAccountTask<?>>();
    
    ////////////////////////////////////////////////////////////////

    public RemoteAccountTaskExecutor(Map<RemoteOj, RemoteAccountOJConfig> config) {
        for (RemoteOj remoteOj : config.keySet()) {
            RemoteAccountOJConfig ojConfig = config.get(remoteOj);
            repos.put(remoteOj, new RemoteAccountRepository(remoteOj, ojConfig, this));
        }
    }
    
    ////////////////////////////////////////////////////////////////
    
    public void init() {
        Thread executingThead = new Thread() {
            public void run() {
                while (true) {
                    RemoteAccountTask<?> task = null;
                    try {
                        task = running.take();
                        RemoteOj remoteOj = task.getRemoteOj();
                        RemoteAccountRepository repo = repos.get(remoteOj);
                        if (repo != null) {
                            repo.handle(task);
                        } else {
                            throw new RuntimeException("There are no remote accounts configured for remote OJ: " + remoteOj);
                        }
                    } catch (Throwable t) {
                        log.error(t.getMessage(), t);
                        if (task != null) {
                            task.getHandler().onError(t);
                        }
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
