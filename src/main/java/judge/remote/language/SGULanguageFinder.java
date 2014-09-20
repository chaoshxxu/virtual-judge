package judge.remote.language;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import judge.remote.RemoteOj;
import judge.remote.language.common.LanguageFinder;
import judge.tool.Handler;

@Component
public class SGULanguageFinder implements LanguageFinder {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.SGU;
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
        languageList.put("GNU C (MinGW, GCC 4)", "GNU C (MinGW, GCC 4)");
        languageList.put("GNU CPP (MinGW, GCC 4)", "GNU CPP (MinGW, GCC 4)");
        languageList.put("Visual Studio C++ 2010", "Visual Studio C++ 2010");
        languageList.put("C#", "C#");
        languageList.put("Visual Studio C 2010", "Visual Studio C 2010");
        languageList.put("JAVA 7", "JAVA 7");
        languageList.put("Delphi 7.0", "Delphi 7.0");
        return languageList;
    }

}
