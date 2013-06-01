package judge.bean;

import java.util.Date;

/**
 * session信息
 * @author Isun
 *
 */
public class Vlog {

	private int id;
	private String sessionId;
	private String ip;
	private Date createTime;
	private long duration;
	private String referer;
	private String userAgent;
	private int loginer;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
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
	public int getLoginer() {
		return loginer;
	}
	public void setLoginer(int loginer) {
		this.loginer = loginer;
	}



}