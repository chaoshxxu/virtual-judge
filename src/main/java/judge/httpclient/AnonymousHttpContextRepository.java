package judge.httpclient;

import java.util.Stack;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * HttpContext as reusable resource should be reused as more as possible to:<br>
 * 1. Decrease HttpContext instances created;<br>
 * 2. Avoid remote OJs creating new sessions for each request from Virtual Judge.<br>
 * 
 * @author Isun
 *
 */
@Repository
public class AnonymousHttpContextRepository {
	private final static Logger log = LoggerFactory.getLogger(AnonymousHttpContextRepository.class);

	private Stack<HttpContext> contexts = new Stack<HttpContext>();
	private ReentrantLock lock = new ReentrantLock(); 

	private final static String RESERVED_FLAG = "trcnkq";
	private final int MAX_SIZE = 100;
	
	public HttpContext acquire() {
		lock.lock();
		try {
			if (contexts.isEmpty()) {
				return build();
			} else {
				return contexts.pop();
			}
		} finally {
			lock.unlock();
		}
	}
	
	public void release(HttpContext context) {
		if (context.getAttribute(RESERVED_FLAG) != null && !contexts.contains(context)) {
			lock.lock();
			try {
				contexts.push(context);
				if (contexts.size() > MAX_SIZE) {
					slim();
				}
			} finally {
				lock.unlock();
			}
		}
	}
	
	/**
	 * Decrease the size of contexts to half of MAX_SIZE
	 */
	private void slim() {
		log.info("Slimming AnonymousHttpContextRepository !");
		 
		int halfSize = MAX_SIZE / 2;
		Stack<HttpContext> temp = new Stack<HttpContext>();
		while (contexts.size() > halfSize) {
			temp.push(contexts.pop());
		}
		contexts.clear();
		while (!temp.isEmpty()) {
			contexts.push(temp.pop());
		}
	}
			
	private HttpContext build() {
		CookieStore cookieStore = new BasicCookieStore();
		HttpContext context = new BasicHttpContext();
		context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
		context.setAttribute(RESERVED_FLAG, true);
		return context;
	}

}
