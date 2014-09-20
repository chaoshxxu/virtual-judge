package judge.bean;

import java.util.Date;
import java.util.Set;

import org.apache.struts2.json.annotations.JSON;

public class Description {

    private int id;                //Hibernate统编ID
    private String description;    //题面描述
    private String input;        //输入介绍
    private String output;        //输出介绍
    private String sampleInput;    //样例输入
    private String sampleOutput;//样例输出
    private String hint;        //提示

    private Date updateTime;    //上次成功更新时间
    private String author;        //作者
    private String remarks;        //备注
    private int vote;            //like票数

    private Problem problem;

    private Set<Cproblem> cproblems;


    @JSON(deserialize=false,serialize=false)
    public Problem getProblem() {
        return problem;
    }
    public void setProblem(Problem problem) {
        this.problem = problem;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getInput() {
        return input;
    }
    public void setInput(String input) {
        this.input = input;
    }
    public String getOutput() {
        return output;
    }
    public void setOutput(String output) {
        this.output = output;
    }
    public String getSampleInput() {
        return sampleInput;
    }
    public void setSampleInput(String sampleInput) {
        this.sampleInput = sampleInput;
    }
    public String getSampleOutput() {
        return sampleOutput;
    }
    public void setSampleOutput(String sampleOutput) {
        this.sampleOutput = sampleOutput;
    }
    public String getHint() {
        return hint;
    }
    public void setHint(String hint) {
        this.hint = hint;
    }
    @JSON(format="yyyy-MM-dd")
    public Date getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public String getRemarks() {
        return remarks;
    }
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    public int getVote() {
        return vote;
    }
    public void setVote(int vote) {
        this.vote = vote;
    }
    @JSON(deserialize=false,serialize=false)
    public Set<Cproblem> getCproblems() {
        return cproblems;
    }
    public void setCproblems(Set<Cproblem> cproblems) {
        this.cproblems = cproblems;
    }


}
