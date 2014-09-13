package judge.remote.language;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import judge.remote.RemoteOj;
import judge.remote.language.common.LanguageFinder;
import judge.tool.Handler;

@Component
public class LightOJLanguageFinder implements LanguageFinder {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.LightOJ;
	}

	@Override
	public boolean isDiverse() {
		return true;
	}

	@Override
	public void getLanguages(String remoteProblemId, Handler<LinkedHashMap<String, String>> handler) {
		// TODO Auto-generated method stub
	}

	@Override
	public LinkedHashMap<String, String> getDefaultLanguages() {
		LinkedHashMap<String, String> languageList = new LinkedHashMap<String, String>();
		languageList.put("C", "C");
		languageList.put("C++", "C++");
		languageList.put("JAVA", "JAVA");
		languageList.put("PASCAL", "PASCAL");
		languageList.put("PYTHON", "PYTHON");
		return languageList;
	}

}
