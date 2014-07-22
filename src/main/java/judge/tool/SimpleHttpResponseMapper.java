package judge.tool;

public interface SimpleHttpResponseMapper<T> extends Mapper<SimpleHttpResponse, T> {
	
	T map(SimpleHttpResponse response) throws Exception;

}
