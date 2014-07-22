package judge.tool;

public interface Mapper<S, T> {

	T map(S value) throws Exception;

}
