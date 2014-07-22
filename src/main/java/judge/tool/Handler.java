package judge.tool;

public interface Handler<T> {

	void handle(T value) throws Exception;

}
