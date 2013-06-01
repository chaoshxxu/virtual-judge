package judge.bean;


/**
 * 比赛题目类，用于表示题目和比赛的多对多关系
 * @author Isun
 *
 */
public class Cproblem {
	private int id;

	private String num;
	private String title;

	private Problem problem;
	private Contest contest;

	private Description description;


	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}
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
	public Problem getProblem() {
		return problem;
	}
	public void setProblem(Problem problem) {
		this.problem = problem;
	}
	public Contest getContest() {
		return contest;
	}
	public void setContest(Contest contest) {
		this.contest = contest;
	}
	public Description getDescription() {
		return description;
	}
	public void setDescription(Description description) {
		this.description = description;
	}

}
