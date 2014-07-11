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
	private static final int ENFORCE_CLEAN_LIMIT = 4000;

	@SuppressWarnings("unchecked")
	public void clean() {
		List<HttpSession> sessions = SessionContext.getInstance().getSessionList();
		
		int totalCnt = sessions.size();
		int loginCnt = 0;
		int transientCnt = 0;
		int leavnCnt = 0;

		for (HttpSession httpSession : sessions) {
			long activeLength = httpSession.getLastAccessedTime() - httpSession.getCreationTime();
			long freezeLength = new Date().getTime() - httpSession.getLastAccessedTime();
			if (httpSession.getAttribute("visitor") != null || httpSession.getAttribute("lpc") != null) {
				++loginCnt;
				continue;
			}
			if (sessions.size() > ENFORCE_CLEAN_LIMIT && activeLength < 1000) {
				++transientCnt;
				httpSession.invalidate();
			} else if (freezeLength > Math.max(3 * activeLength, 10 * ONE_MINUTE)) {
				++leavnCnt;
				httpSession.invalidate();
			}
		}
		
		System.out.println(
				"Clean sessions:" + 
				"\tTOTAL:" + totalCnt + 
				"\tLOGIN:" + loginCnt + 
				"\tTRANSIENT:" + transientCnt + 
				"\tLEAVE:" + leavnCnt);
	}

}
