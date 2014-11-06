/**
 * 处理用户相关功能
 */

package judge.action;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import judge.bean.User;
import judge.bean.UserSession;
import judge.interceptor.AutoLoginInterceptor;
import judge.service.AutoLoginManager;
import judge.service.BaseService;
import judge.service.UserService;
import judge.tool.CookieUtil;
import judge.tool.MD5;
import judge.tool.OnlineTool;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;

@SuppressWarnings("unchecked")
public class UserAction extends BaseAction implements ServletRequestAware {

    private static final long serialVersionUID = -4110838947220309361L;
    private User user;
    private int uid;
    private String username;
    private String nickname;
    private String qq;
    private String school;
    private String email;
    private String blog;
    private int share;
    private String password;
    private String repassword;
    private String newpassword;
    private String redir;
    private HttpServletRequest request;

    private AutoLoginManager autoLoginManager;

    @Autowired
    private BaseService baseService;

    private UserService userService;

    public String login(){
        Map session = ActionContext.getContext().getSession();
        if (OnlineTool.isLoggedIn()) {
            return SUCCESS;
        }

        User user = userService.getByUsername(username);
        if (user == null) {
            json = "Username not exists!";
        } else if (StringUtils.length(password) > 80 || !user.getPassword().equals(MD5.getMD5(password))) {
            UserSession userSession = new UserSession();
            userSession.setArriveTime(new Date(request.getSession().getCreationTime()));
            userSession.setLoginTime(new Date());
            userSession.setUserAgent((String) session.get("user-agent"));
            userSession.setIp((String) session.get("remoteAddr"));
            userSession.setUser(user);
            userSession.setLoginSuccess(0);
            session.put("user-session", userSession);
            baseService.addOrModify(userSession);

            json = "Username and password don't match!";
        } else {
            json = "success";
            session.put("visitor", user);

            //In case this visitor has logged in, remove auto login token for him
            String username = CookieUtil.getCookie(ActionContext.getContext(), AutoLoginInterceptor.AUTO_LOGGIN_USERNAME_KEY);
            String token = CookieUtil.getCookie(ActionContext.getContext(), AutoLoginInterceptor.AUTO_LOGGIN_TOKEN_KEY);
            autoLoginManager.removeToken(username, token);

            token = autoLoginManager.addUserEntry(user.getUsername());
            CookieUtil.addCookie(ActionContext.getContext(), AutoLoginInterceptor.AUTO_LOGGIN_USERNAME_KEY, user.getUsername());
            CookieUtil.addCookie(ActionContext.getContext(), AutoLoginInterceptor.AUTO_LOGGIN_TOKEN_KEY, token);
            
            for (Iterator iterator = session.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                if (key.matches("C\\d+")) {
                    session.remove(key);
                }
            }

            UserSession userSession = new UserSession();
            userSession.setArriveTime(new Date(request.getSession().getCreationTime()));
            userSession.setLoginTime(new Date());
            userSession.setUserAgent((String) session.get("user-agent"));
            userSession.setIp((String) session.get("remoteAddr"));
            userSession.setUser(user);
            userSession.setLoginSuccess(1);
            session.put("user-session", userSession);
            baseService.addOrModify(userSession);

        }
        return SUCCESS;
    }

    public String logout() {
        String username = CookieUtil.getCookie(ActionContext.getContext(), AutoLoginInterceptor.AUTO_LOGGIN_USERNAME_KEY);
        String token = CookieUtil.getCookie(ActionContext.getContext(), AutoLoginInterceptor.AUTO_LOGGIN_TOKEN_KEY);

        autoLoginManager.removeToken(username, token);
        CookieUtil.removeCookie(ActionContext.getContext(), AutoLoginInterceptor.AUTO_LOGGIN_USERNAME_KEY);
        CookieUtil.removeCookie(ActionContext.getContext(), AutoLoginInterceptor.AUTO_LOGGIN_TOKEN_KEY);
        
        request.getSession().invalidate();
        return SUCCESS;
    }

    public String register(){
        json = null;
        if (!username.matches("[0-9a-zA-Z_]+")){
            json = "Username should only contain digits, letters, or '_'s !";
        } else if (username.length() < 2 || username.length() > 16){
            json = "Username should have at least 2 characters and at most 16 characters!";
        } else if (nickname.length() > 20){
            json = "Nickname should have at most 20 characters!";
        } else if (password.length() < 4 || password.length() > 30){
            json = "Password should have at least 4 characters and at most 30 characters!";
        } else if (!password.equals(repassword)){
            json = "Two passwords are not the same!";
        } else if (userService.checkUsername(username)){
            json = "Username has been registered!";
        } else if (qq.length() > 15){
            json = "QQ is too long!";
        } else if (school.length() > 95){
            json = "School is too long!";
        } else if (email.length() > 95){
            json = "Email is too long!";
        } else if (blog.length() > 995){
            json = "Blog is too long!";
        }
        if (json != null){
            return SUCCESS;
        }
        User user = new User(username, MD5.getMD5(password));
        user.setNickname(nickname.trim());
        user.setQq(qq);
        user.setSchool(school);
        user.setEmail(email);
        user.setBlog(blog);
        user.setShare(share);
        baseService.addOrModify(user);
        Map session = ActionContext.getContext().getSession();
        session.put("visitor", user);

        String token = autoLoginManager.addUserEntry(user.getUsername());
        CookieUtil.addCookie(ActionContext.getContext(), AutoLoginInterceptor.AUTO_LOGGIN_USERNAME_KEY, user.getUsername());
        CookieUtil.addCookie(ActionContext.getContext(), AutoLoginInterceptor.AUTO_LOGGIN_TOKEN_KEY, token);

        json = "success";
        return SUCCESS;
    }

    public String toUpdate(){
        user = (User) baseService.query(User.class, uid);
        username = user.getUsername();
        nickname = user.getNickname();
        school = user.getSchool();
        qq = user.getQq();
        email = user.getEmail();
        blog = user.getBlog();
        share = user.getShare();
        uid = user.getId();
        redir = ServletActionContext.getRequest().getHeader("Referer");
        return SUCCESS;
    }


    public String update(){
        user = (User) baseService.query(User.class, uid);
        Map session = ActionContext.getContext().getSession();
        User cUser = (User) session.get("visitor");
        if (user == null || cUser == null || cUser.getId() != user.getId()){
            return ERROR;
        }
        if (!user.getPassword().equals(MD5.getMD5(password))){
            this.addActionError("Enter the correct old password!");
        }
        if (!newpassword.isEmpty() || !repassword.isEmpty()){
            if (newpassword.length() < 4 || newpassword.length() > 30){
                this.addActionError("Password should have at least 4 characters and at most 30 characters!");
            }
            if (!newpassword.equals(repassword)){
                this.addActionError("Passwords are not match!");
            }
            newpassword = MD5.getMD5(newpassword);
            if (!user.getPassword().equals(newpassword)) {
                autoLoginManager.removeUser(user.getUsername());
                String token = autoLoginManager.addUserEntry(user.getUsername());
                CookieUtil.addCookie(ActionContext.getContext(), AutoLoginInterceptor.AUTO_LOGGIN_USERNAME_KEY, user.getUsername());
                CookieUtil.addCookie(ActionContext.getContext(), AutoLoginInterceptor.AUTO_LOGGIN_TOKEN_KEY, token);
                user.setPassword(newpassword);
            }
        }
        if (nickname.length() > 20){
            this.addActionError("Nickname should have at most 20 characters!");
        }
        if (qq.length() > 15){
            this.addActionError("QQ is too long!");
        }
        if (school.length() > 95){
            this.addActionError("School is too long!");
        }
        if (email.length() > 95){
            this.addActionError("Email is too long!");
        }
        if (blog.length() > 995){
            this.addActionError("Blog is too long!");
        }
        if (!this.getActionErrors().isEmpty()){
            return INPUT;
        }
        user.setNickname(nickname.trim());
        user.setQq(qq);
        user.setSchool(school);
        user.setEmail(email);
        user.setBlog(blog);
        user.setShare(share);
        baseService.addOrModify(user);
        session.put("visitor", user);
        return SUCCESS;
    }


    public String profile() {
        user = (User) baseService.query(User.class, uid);
        return SUCCESS;
    }

    public String checkLogInStatus() {
        json = OnlineTool.getCurrentUser() == null ? "false" : "true";
        return SUCCESS;
    }
    
    public String getNewpassword() {
        return newpassword;
    }
    public void setNewpassword(String newpassword) {
        this.newpassword = newpassword;
    }
    public int getShare() {
        return share;
    }
    public void setShare(int share) {
        this.share = share;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public int getUid() {
        return uid;
    }
    public void setUid(int uid) {
        this.uid = uid;
    }
    public String getQq() {
        return qq;
    }
    public void setQq(String qq) {
        this.qq = qq;
    }
    public String getSchool() {
        return school;
    }
    public void setSchool(String school) {
        this.school = school;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getBlog() {
        return blog;
    }
    public void setBlog(String blog) {
        this.blog = blog;
    }
    public String getRedir() {
        return redir;
    }
    public void setRedir(String redir) {
        this.redir = redir;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getRepassword() {
        return repassword;
    }
    public void setRepassword(String repassword) {
        this.repassword = repassword;
    }
    public UserService getUserService() {
        return userService;
    }
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }
    public void setAutoLoginManager(AutoLoginManager autoLoginManager) {
        this.autoLoginManager = autoLoginManager;
    }

}
