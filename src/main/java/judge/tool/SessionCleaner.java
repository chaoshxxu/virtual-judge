package judge.tool;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 销毁垃圾httpSession
 * @author Isun
 *
 */
public class SessionCleaner {
    private final static Logger log = LoggerFactory.getLogger(SessionCleaner.class);

    private static final long ONE_MINUTE = 60 * 1000;
    private static final int ENFORCE_CLEAN_LIMIT = 4000;

    @SuppressWarnings("unchecked")
    public void clean() {
        List<HttpSession> sessions = SessionContext.getInstance().getSessionList();
        
        int totalCnt = sessions.size();
        int loginCnt = 0;
        int transientCnt = 0;
        int leavnCnt = 0;
        int abuseCnt = 0;
        
        // If there are more than 20 unauthenticated sessions from the same IP, destroy all of them
        Map<String, Integer> ipCount = new HashMap<String, Integer>();
        for (HttpSession httpSession : sessions) {
            if (httpSession.getAttribute("visitor") == null) {
                String ip = (String) httpSession.getAttribute("remoteAddr");
                Integer count = ipCount.get(ip);
                ipCount.put(ip, count == null ? 1 : count + 1);
            }
        }

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
            } else {
                String ip = (String) httpSession.getAttribute("remoteAddr");
                Integer count = ipCount.get(ip);
                if (count != null && count > 20) {
                    ++abuseCnt;
                    httpSession.invalidate();
                }
            }
        }
        
        log.info(
                "Clean sessions:" + 
                "  TOTAL:" + totalCnt + 
                "  LOGIN:" + loginCnt + 
                "  TRANSIENT:" + transientCnt + 
                "  LEAVE:" + leavnCnt + 
                "  ABUSE:" + abuseCnt);
    }

}
