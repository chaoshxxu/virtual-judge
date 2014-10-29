package judge.interceptor;

import java.util.Date;
import java.util.Map;

import judge.bean.User;
import judge.bean.UserSession;
import judge.service.AutoLoginManager;
import judge.service.BaseService;
import judge.service.UserService;
import judge.tool.CookieUtil;
import judge.tool.OnlineTool;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import judge.tool.SpringBean;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AutoLoginInterceptor extends AbstractInterceptor {
    private static final long serialVersionUID = 4012222977733742407L;

    public static final String USER_SESSION_KEY = "visitor";
    public static final String AUTO_LOGGIN_USERNAME_KEY = "twgdh";
    public static final String AUTO_LOGGIN_TOKEN_KEY = "btzhy";
    
    private AutoLoginManager autoLoginManager;
    
    private UserService userService;

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        ActionContext actionContext = invocation.getInvocationContext();  
        if (OnlineTool.isLoggedIn()) {
            return invocation.invoke();
        }
        
        String autoLogginUsername = CookieUtil.getCookie(actionContext, AUTO_LOGGIN_USERNAME_KEY);
        String autoLogginToken = CookieUtil.getCookie(actionContext, AUTO_LOGGIN_TOKEN_KEY);
        if (autoLogginUsername == null || autoLogginToken == null) {
            return invocation.invoke();
        }
        
        boolean usernameTokenValid = autoLoginManager.isValid(autoLogginUsername, autoLogginToken);
        if (usernameTokenValid == false) {
            return invocation.invoke();
        }
        
        Map session = actionContext.getSession();
        User user = userService.getByUsername(autoLogginUsername);
        session.put(USER_SESSION_KEY, user);


        HttpServletRequest request = (HttpServletRequest) actionContext.get(ServletActionContext.HTTP_REQUEST);
        UserSession userSession = new UserSession();
        userSession.setArriveTime(new Date(request.getSession().getCreationTime()));
        userSession.setLoginTime(new Date());
        userSession.setUserAgent((String) session.get("user-agent"));
        userSession.setIp((String) session.get("remoteAddr"));
        userSession.setUser(user);
        session.put("user-session", userSession);

        SpringBean.getBean(BaseService.class).addOrModify(userSession);

        return invocation.invoke();
    }

    public void setAutoLoginManager(AutoLoginManager autoLoginManager) {
        this.autoLoginManager = autoLoginManager;
    }
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    
}