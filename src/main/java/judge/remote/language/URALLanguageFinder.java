package judge.remote.language;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import judge.remote.RemoteOj;
import judge.remote.language.common.LanguageFinder;
import judge.tool.Handler;

@Component
public class URALLanguageFinder implements LanguageFinder {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.URAL;
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
		languageList.put("3", "FreePascal 2.0.4");
		languageList.put("9", "Visual C 2010");
		languageList.put("10", "Visual C++ 2010");
		languageList.put("11", "Visual C# 2010");
		languageList.put("12", "Java 1.7");
		languageList.put("14", "Go 1.0.3");
		languageList.put("15", "VB.NET 2010");
		languageList.put("16", "Python 2.7");
		languageList.put("17", "Python 3.3");
		languageList.put("18", "Ruby 1.9.3");
		languageList.put("19", "Haskell 7.6.1");
		languageList.put("20", "GCC 4.7.2");
		languageList.put("21", "G++ 4.7.2");
		languageList.put("22", "GCC 4.7.2 C11");
		languageList.put("23", "G++ 4.7.2 C++11");
		return languageList;
	}

}
