/**
 * 用于统计网站数据
 */

package judge.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import judge.bean.User;
import judge.tool.OnlineTool;
import judge.tool.PhysicalAddressTool;
import judge.tool.SessionContext;

import com.opensymphony.xwork2.ActionSupport;

@SuppressWarnings( { "unchecked", "serial" })
public class StatAction extends ActionSupport {

	private List dataList;
	private String id;
	private HttpSession session;
	private User user;

	private String ip;
	private Date creationTime;
	private Date lastAccessedTime;
	private String referer;
	private String userAgent;

	private int loginUsers;
	private int ipMapCnt;

	private SessionContext myc = SessionContext.getInstance();

	public String listOnlineUsers() {
		user = OnlineTool.getCurrentUser();
		if (user == null || user.getSup() == 0) {
			return ERROR;
		}

		List<HttpSession> sessionList = myc.getSessionList();
		dataList = new ArrayList();
		loginUsers = 0;
		ipMapCnt = PhysicalAddressTool.getIpMapSize();

		for (int i = 0; i < sessionList.size(); ++i) {
			HttpSession session = sessionList.get(i);
			String ip = (String) session.getAttribute("remoteAddr");
			String ua = (String) session.getAttribute("user-agent");
			user = (User) session.getAttribute("visitor");

			if (ip == null){
				continue;
			}

			List row = new ArrayList<Object>();
			row.add(session.getId());
			if (user != null) {
				row.add(user.getUsername());
				row.add(user.getId());
				++loginUsers;
			} else {
				row.add(null);
				row.add(null);
			}
			row.add(ip.replaceAll("\\s*,\\s*", "\n"));
			row.add(PhysicalAddressTool.getPhysicalAddress(ip));
 			row.add(new Date(session.getCreationTime()));

			long al = (session.getLastAccessedTime() - session.getCreationTime()) / 1000;
			row.add((al / 60 > 0 ? al / 60 + "分" : "") + (al % 60 + "秒"));

			long fl = (new Date().getTime() - session.getLastAccessedTime()) / 1000;
			row.add((fl / 60 > 0 ? fl / 60 + "分" : "") + (fl % 60 + "秒"));

			row.add(findBrowser(ua));
			row.add(findOS(ua));
			dataList.add(row);
		}

		return SUCCESS;
	}

	public String viewOL() {
		session = myc.getSession(id);
		ip = (String) session.getAttribute("remoteAddr");
		creationTime = new Date(session.getCreationTime());
		lastAccessedTime = new Date(session.getLastAccessedTime());
		user = (User) session.getAttribute("visitor");
		referer = (String) session.getAttribute("referer");
		userAgent = (String) session.getAttribute("user-agent");
		return SUCCESS;
	}


	public String findBrowser(String ref) {
		if (ref == null)
			return "Unknown";
		ref = ref.toUpperCase();
		if (ref.contains("MSIE")) {
			return "IE";
		} else if (ref.contains("FIREFOX")) {
			return "Firefox";
		} else if (ref.contains("CHROME")) {
			return "Chrome";
		} else if (ref.contains("NETSCAPE")) {
			return "NetScape";
		} else if (ref.contains("OPERA")) {
			return "Opera";
		} else {
			return "Unknown";
		}
	}

	public String findOS(String ref) {
		if (ref == null)
			return "Unknown";
		ref = ref.toUpperCase();
		if (ref.contains("WINDOWS")) {
			return "Windows";
		} else if (ref.contains("LINUX")) {
			return "Linux";
		} else if (ref.contains("MAC")) {
			return "Mac";
		} else if (ref.contains("UNIX")) {
			return "Unix";
		} else {
			return "Unknown";
		}
	}


	public List getDataList() {
		return dataList;
	}
	public void setDataList(List dataList) {
		this.dataList = dataList;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public HttpSession getSession() {
		return session;
	}
	public void setSession(HttpSession session) {
		this.session = session;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Date getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	public Date getLastAccessedTime() {
		return lastAccessedTime;
	}
	public void setLastAccessedTime(Date lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
	}
	public String getReferer() {
		return referer;
	}
	public void setReferer(String referer) {
		this.referer = referer;
	}
	public String getUserAgent() {
		return userAgent;
	}
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getLoginUsers() {
		return loginUsers;
	}
	public void setLoginUsers(int loginUsers) {
		this.loginUsers = loginUsers;
	}
	public int getIpMapCnt() {
		return ipMapCnt;
	}
	public void setIpMapCnt(int ipMapCnt) {
		this.ipMapCnt = ipMapCnt;
	}
}
