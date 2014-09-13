package judge.remote.language;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import judge.remote.RemoteOj;
import judge.remote.language.common.LanguageFinder;
import judge.tool.Handler;

@Component
public class FZULanguageFinder implements LanguageFinder {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.FZU;
	}

	@Override
	public boolean isDiverse() {
		return false;
	}

	@Override
	public void getLanguages(String remoteProblemId, Handler<LinkedHashMap<String, String>> handler) {
		// TODO Auto-generated method stub
	}

	@Override
	public LinkedHashMap<String, String> getDefaultLanguages() {
		LinkedHashMap<String, String> languageList = new LinkedHashMap<String, String>();
		languageList.put("0", "GNU C++");
		languageList.put("1", "GNU C");
		languageList.put("2", "Pascal");
		languageList.put("3", "Java");
		languageList.put("4", "Visual C++");
		languageList.put("5", "Visual C");
		return languageList;
	}

}
