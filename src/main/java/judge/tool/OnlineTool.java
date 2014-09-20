package judge.tool;

import java.util.Map;

import judge.bean.User;

import com.opensymphony.xwork2.ActionContext;

public class OnlineTool {
    public static User getCurrentUser() {
        Map session = ActionContext.getContext().getSession();
        return (User) session.get("visitor");
    }
    
    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }
    
}
