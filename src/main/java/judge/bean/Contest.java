package judge.bean;

import java.util.Date;
import java.util.Set;

/**
 * 比赛信息
 * @author Isun
 *
 */
public class Contest {
	private int id;
	private String title;
	private String description;
	private String announcement;
	private String password;
	private Date beginTime;
	private Date endTime;
	private String hashCode;	//按原题title
	private int enableTimeMachine;

	private User manager;		//管理员
	private ReplayStatus replayStatus;

	private Set<Submission> submissions;
	private Set<Cproblem> cproblems;


	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Date getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public String getHashCode() {
		return hashCode;
	}
	public void setHashCode(String hashCode) {
		this.hashCode = hashCode;
	}
	public User getManager() {
		return manager;
	}
	public void setManager(User manager) {
		this.manager = manager;
	}
	public Set<Submission> getSubmissions() {
		return submissions;
	}
	public void setSubmissions(Set<Submission> submissions) {
		this.submissions = submissions;
	}
	public Set<Cproblem> getCproblems() {
		return cproblems;
	}
	public void setCproblems(Set<Cproblem> cproblems) {
		this.cproblems = cproblems;
	}
	public ReplayStatus getReplayStatus() {
		return replayStatus;
	}
	public void setReplayStatus(ReplayStatus replayStatus) {
		this.replayStatus = replayStatus;
	}
	public String getAnnouncement() {
		return announcement;
	}
	public void setAnnouncement(String announcement) {
		this.announcement = announcement;
	}
	public int getEnableTimeMachine() {
		return enableTimeMachine;
	}
	public void setEnableTimeMachine(int enableTimeMachine) {
		this.enableTimeMachine = enableTimeMachine;
	}
}
