package judge.tool;

import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SessionListener implements HttpSessionListener {
	public static Map userMap = new HashMap();
	private SessionContext myc = SessionContext.getInstance();

	public void sessionCreated(HttpSessionEvent httpSessionEvent) {
		HttpSession session = httpSessionEvent.getSession();
		myc.AddSession(session);
	}

	public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
		HttpSession session = httpSessionEvent.getSession();
		myc.DelSession(session);
	}
}
