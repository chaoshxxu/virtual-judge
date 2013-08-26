package judge.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import judge.tool.LRUList;
import judge.tool.RandomUtil;

/**
 * 
 * @author Isun
 *
 */
public class AutoLoginManager {
	
	private static final int MAX_TOKENS_PER_USER = 5;
	private static final int TOKEN_LENGTH = 30;
	private static final String TOKEN_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final int MAX_REPO_SIZE = 2000;
	
	/**
	 * username -> tokens
	 * i.e. one username can have multiple valid tokens at the same time.
	 * The tokens of one username is managed using LRU strategy.
	 */
	private ConcurrentHashMap<String, LRUList<String>> repo;
	
	/**
	 * r/w Lock for repo
	 * Note: repo uses ConcurrentHashMap implementation, which is thread safe.
	 * Therefore, manipulating its containing elements has no need to synchronize, and can be
	 * treated as *READ* operation of repo.
	 * However assigning repo with another object is *WRITE* operation of repo.
	 */
	final ReadWriteLock lock = new ReentrantReadWriteLock();
    final Lock readLock = lock.readLock();
    final Lock writeLock = lock.writeLock();
	
	public void init() {
		repo = new ConcurrentHashMap<String, LRUList<String>>();
	}
	
	/**
	 * Should be called when access any URI not logged in
	 * @param username
	 * @param token
	 * @return
	 */
	public boolean isValid(String username, String token) {
		readLock.lock();
		try {
			LRUList<String> tokens = repo.get(username);
			if (tokens == null) {
				return false;
			}
			if (!tokens.contains(token)) {
				return false;
			}
			tokens.put(token);
			return true;
		} finally {
			readLock.unlock();
		}
	}
	
	/**
	 * Should be called on login success or register success
	 * @param username
	 * @return token
	 */
	public String addUserEntry(String username) {
		readLock.lock();
		String token;
		try {
			LRUList<String> tokens = repo.get(username);
			if (tokens == null) {
				tokens = new LRUList<String>(MAX_TOKENS_PER_USER);
				repo.put(username, tokens);
			}
			token = RandomUtil.getRandomString(TOKEN_LENGTH, TOKEN_CHARSET);
			tokens.put(token);
		} finally {
			readLock.unlock();
		}
		
		try {
			reduceRepo();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return token;
	}
	
	/**
	 * Should be called on change password success
	 * @param username
	 * @return
	 */
	public boolean removeUser(String username) {
		readLock.lock();
		try {
			return repo.remove(username) != null;
		} finally {
			readLock.unlock();
		}
	}
	
	/**
	 * Should be called on manually log out
	 * @param username
	 * @param token
	 * @return
	 */
	public boolean removeToken(String username, String token) {
		readLock.lock();
		try {
			if (username == null || token == null) {
				return false;
			}
			LRUList<String> tokens = repo.get(username);
			if (tokens == null) {
				return false;
			}
			return tokens.remove(token);
		} finally {
			readLock.unlock();
		}
	}
	
	/**
	 * After repo size reaches MAX_REPO_SIZE, it will remove the least recently used half
	 */
	private void reduceRepo() {
		writeLock.lock();
		try {
			if (repo.size() <= MAX_REPO_SIZE) {
				return;
			}
			long begin = new Date().getTime();
			System.out.println("Start reduce repo, size: " + repo.size());
			
			List<Date> dates = new ArrayList<Date>();
			for (LRUList<String> tokens : repo.values()) {
				dates.add(tokens.getLastVisitTime());
			}
			Collections.sort(dates);
			Date threshold = dates.get(dates.size() / 2);
			
			ConcurrentHashMap<String, LRUList<String>> tmpRepo = new ConcurrentHashMap<String, LRUList<String>>();
			for (String username : repo.keySet()) {
				LRUList<String> tokens = repo.get(username);
				if (tokens.getLastVisitTime().compareTo(threshold) >= 0) {
					tmpRepo.put(username, tokens);
				}
			}
			System.out.println("Finish reduce repo, size: " + repo.size() + ", using " + (new Date().getTime() - begin) + "ms");
			
			repo = tmpRepo;
		} finally {
			writeLock.unlock();
		}
	}
}
