package judge.tool;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

/**
 * 销毁垃圾httpSession
 * @author Isun
 *
 */
public class SessionCleaner {

	private static final long ONE_MINUTE = 60 * 1000;

	@SuppressWarnings("unchecked")
	public void clean() {
		List<HttpSession> sessions = SessionContext.getInstance().getSessionList();
		for (HttpSession httpSession : sessions) {
			long activeLength = httpSession.getLastAccessedTime() - httpSession.getCreationTime();
			long freezeLength = new Date().getTime() - httpSession.getLastAccessedTime();
			if (httpSession.getAttribute("remoteAddr") == null) {
				httpSession.invalidate();
			} else if (httpSession.getAttribute("visitor") == null && httpSession.getAttribute("lpc") == null && freezeLength > 3 * Math.max(activeLength, ONE_MINUTE)) {
				httpSession.invalidate();
			}
		}
	}

}
