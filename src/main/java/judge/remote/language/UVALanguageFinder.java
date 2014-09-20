package judge.remote.language;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import judge.remote.RemoteOj;
import judge.remote.language.common.LanguageFinder;
import judge.tool.Handler;

@Component
public class UVALanguageFinder implements LanguageFinder {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.UVA;
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
        languageList.put("1", "ANSI C 4.8.2");
        languageList.put("2", "JAVA 1.7.0");
        languageList.put("3", "C++ 4.8.2");
        languageList.put("4", "PASCAL 2.6.2");
        languageList.put("5", "C++11 4.8.2");
        return languageList;
    }

}
