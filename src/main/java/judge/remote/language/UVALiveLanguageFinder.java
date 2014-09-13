package judge.remote.language;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import judge.remote.RemoteOj;
import judge.remote.language.common.LanguageFinder;
import judge.tool.Handler;

@Component
public class UVALiveLanguageFinder implements LanguageFinder {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.UVALive;
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
		languageList.put("1", "ANSI C 4.5.3");
		languageList.put("2", "JAVA 1.6.0");
		languageList.put("3", "C++ 4.5.3");
		languageList.put("4", "PASCAL 2.4.0");
		return languageList;
	}

}
