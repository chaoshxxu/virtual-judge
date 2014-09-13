package judge.remote.language.common;

import java.util.LinkedHashMap;

import judge.remote.RemoteOjAware;
import judge.tool.Handler;

/**
 * Find eligible submitting languages for specified problem<br>
 * Implementation should be stateless.
 * 
 * @author Isun
 */
public interface LanguageFinder extends RemoteOjAware {

	/**
	 * Different languages for different problems
	 * 
	 * @return
	 */
	boolean isDiverse();

	/**
	 * {submitted value} -> {displayed value}
	 * May be called dynamically to update.
	 * 
	 * @param remoteProblemId
	 * @return
	 */
	void getLanguages(String remoteProblemId, Handler<LinkedHashMap<String, String>> handler);

	/**
	 * Should return immediately.
	 * @return
	 */
	LinkedHashMap<String, String> getDefaultLanguages();

}
