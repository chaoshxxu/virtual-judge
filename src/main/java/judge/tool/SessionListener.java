package judge.tool;

import judge.bean.User;
import judge.bean.UserSession;
import judge.service.BaseService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener {
    public static Map userMap = new HashMap();
    private SessionContext myc = SessionContext.getInstance();

    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        myc.AddSession(session);
    }

    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();

        UserSession userSession = (UserSession) session.getAttribute("user-session");
        if (userSession != null) {
            userSession.setReferer((String) session.getAttribute("referer"));
            userSession.setLeaveTime(new Date());
            SpringBean.getBean(BaseService.class).addOrModify(userSession);
        }

        myc.DelSession(session);
    }
}
