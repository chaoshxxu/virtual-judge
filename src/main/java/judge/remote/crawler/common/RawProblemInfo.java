package judge.remote.crawler.common;

/**
 * Direct crawling result from original OJ 
 * @author Isun
 *
 */
public class RawProblemInfo {
    
    public String title;        //标题

    public int timeLimit;        //时间限制(ms)
    public int memoryLimit;        //内存限制(KB)
    
    public String description;    //题面描述
    public String input;        //输入介绍
    public String output;        //输出介绍
    public String sampleInput;    //样例输入
    public String sampleOutput;//样例输出
    public String hint;        //提示
    public String source;        //出处

    public String url;            //题面原始url

}
