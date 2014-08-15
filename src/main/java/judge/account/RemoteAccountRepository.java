package judge.account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import judge.account.config.RemoteAccountConfig;
import judge.account.config.RemoteAccountOJConfig;
import judge.executor.Task;

import org.apache.commons.lang3.Validate;
import org.apache.http.protocol.HttpContext;

/**
 * Containing remote accounts dedicating to one remote OJ
 * @author isun
 *
 */
public class RemoteAccountRepository {
	
	private final String remoteOj;

	/**
	 * accountId -> acountStatus
	 */
	private final Map<String, RemoteAccountStatus> publicRepo = new HashMap<String, RemoteAccountStatus>();
	
	/**
	 * accountId -> acountStatus
	 */
	private final Map<String, RemoteAccountStatus> privateRepo = new HashMap<String, RemoteAccountStatus>();
	
	/**
	 * accountId == null, meaning any account is OK.
	 */
	private final LinkedList<RemoteAccountTask<?>> normalBacklog = new LinkedList<RemoteAccountTask<?>>();
	
	/**
	 * accountId != null, meaning specifying an account.
	 * accountId -> tasks
	 */
	private final Map<String, LinkedList<RemoteAccountTask<?>>> pickyBacklog = new HashMap<String, LinkedList<RemoteAccountTask<?>>>();
	
	private final RemoteAccountTaskExecutor remoteAuthTaskExecutor;
	
	/////////////////////////////////////////////////////////////
	
	public RemoteAccountRepository(String remoteOj, RemoteAccountOJConfig ojConfig, RemoteAccountTaskExecutor remoteAuthTaskExecutor) {
		this.remoteOj = remoteOj;
		this.remoteAuthTaskExecutor = remoteAuthTaskExecutor;
		
		for (RemoteAccountConfig accountConfig : ojConfig.accounts) {
			RemoteAccountStatus status = new RemoteAccountStatus(remoteOj, accountConfig.id, accountConfig.password, accountConfig.isPublic, ojConfig.contextNumber);
			(accountConfig.isPublic ? publicRepo : privateRepo).put(accountConfig.id, status);
		}
	}
	
	/////////////////////////////////////////////////////////////

	public void handle(RemoteAccountTask<?> task) {
		if (task.isDone()) {
			releaseAccount(task.getAccount());
		} else {
			tryExecute(task);
		}
	}
	
	public void releaseAccount(RemoteAccount account) {
		Validate.isTrue(account.getRemoteOj().equals(remoteOj));
		String accountId = account.getAccountId();
		
		RemoteAccountStatus accountStatus = null;
		if (publicRepo.containsKey(accountId)) {
			accountStatus = publicRepo.get(accountId);
		} else if (privateRepo.containsKey(accountId)) {
			accountStatus = privateRepo.get(accountId);
		} else {
			return;
		}
		
		accountStatus.removeExclusiveCode(account.getExclusiveCode());
		accountStatus.returnContext(account.getContext());
		
		LinkedList<RemoteAccountTask<?>> backlogs = pickyBacklog.get(accountId);
		if (backlogs != null) {
			if (tryBacklog(backlogs)) {
				if (backlogs.isEmpty()) {
					pickyBacklog.remove(accountId);
				}
				return;
			}
		}
		if (accountStatus.isPublic()) {
			tryBacklog(normalBacklog);
		}
	}

	private boolean tryBacklog(LinkedList<RemoteAccountTask<?>> backlog) {
		for (Iterator<RemoteAccountTask<?>> iterator = backlog.iterator(); iterator.hasNext();) {
			RemoteAccountTask<?> task = (RemoteAccountTask<?>) iterator.next();
			RemoteAccount account = findAccount(task.getAccountId(), task.getExclusiveCode());
			if (account != null) {
				iterator.remove();
				execute(task, account);
				return true;
			}
		}
		return false;
	}

	private void tryExecute(RemoteAccountTask<?> task) {
		RemoteAccount account = findAccount(task.getAccountId(), task.getExclusiveCode());
		if (account != null) {
			execute(task, account);
			return;
		}
		String accountId = task.getAccountId();
		if (accountId == null) {
			normalBacklog.add(task);
			return;
		}
		LinkedList<RemoteAccountTask<?>> picky = pickyBacklog.get(accountId);
		if (picky == null) {
			picky = new LinkedList<RemoteAccountTask<?>>();
			pickyBacklog.put(accountId, picky);
		}
		picky.add(task);
	}
	
	private RemoteAccount findAccount(String accountId, String exclusiveCode) {
		List<RemoteAccountStatus> candidates = new ArrayList<RemoteAccountStatus>();
		if (accountId == null) {
			candidates.addAll(publicRepo.values());
		} else if (accountId != null && publicRepo.containsKey(accountId)) {
			candidates.add(publicRepo.get(accountId));
		} else if (accountId != null && privateRepo.containsKey(accountId)) {
			candidates.add(privateRepo.get(accountId));
		}
		Collections.shuffle(candidates);
		for (RemoteAccountStatus remoteAccountStatus : candidates) {
			if (remoteAccountStatus.eligible(accountId, exclusiveCode)) {
				HttpContext context = remoteAccountStatus.borrowContext();
				remoteAccountStatus.addExclusiveCode(exclusiveCode);
				return new RemoteAccount(
						remoteOj, 
						remoteAccountStatus.getAccountId(), 
						remoteAccountStatus.getPassword(), 
						exclusiveCode, 
						context);
			}
		}
		return null;
	}

	private <V> void execute(final RemoteAccountTask<V> task, final RemoteAccount account) {
		new Task<V>(task.getExecutorTaskType()) {
			@Override
			public V call() throws Exception {
				try {
					task.setAccount(account);
					try {
						task.offerResult(task.call(account));
					} catch (Throwable t) {
						task.offerResult(t);
					}
				} finally {
					task.done();
					remoteAuthTaskExecutor.submit(task);
				}
				return null;
			}
		}.submit();
	}
	
}
