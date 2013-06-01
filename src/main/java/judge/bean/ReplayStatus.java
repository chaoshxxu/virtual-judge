package judge.bean;

import java.util.Set;

/**
 * 比赛回放信息
 * @author Isun
 *
 */
public class ReplayStatus {
	private int id;
	private String data;

	private Set<Contest> contests;


	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public Set<Contest> getContests() {
		return contests;
	}
	public void setContests(Set<Contest> contests) {
		this.contests = contests;
	}
}
