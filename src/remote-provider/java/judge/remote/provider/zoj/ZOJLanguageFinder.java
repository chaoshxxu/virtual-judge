package judge.remote.provider.zoj;

import java.util.HashMap;
import java.util.LinkedHashMap;

import judge.remote.RemoteOjInfo;
import judge.remote.language.LanguageFinder;
import judge.tool.Handler;

import org.springframework.stereotype.Component;

@Component
public class ZOJLanguageFinder implements LanguageFinder {

    @Override
    public RemoteOjInfo getOjInfo() {
        return ZOJInfo.INFO;
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
        languageList.put("1", "C (gcc 4.4.5)");
        languageList.put("2", "C++ (g++ 4.4.5)");
        languageList.put("3", "FPC (fpc 2.4.0)");
        languageList.put("4", "Java (java 1.6.0)");
        languageList.put("5", "Python (Python 2.6.6)");
        languageList.put("6", "Perl (Perl 5.10.1)");
        languageList.put("7", "Scheme (Guile 1.8.7)");
        languageList.put("8", "PHP (PHP 5.3.2)");
        return languageList;
    }

    @Override
    public HashMap<String, String> getLanguagesAdapter() {
        // TODO Auto-generated method stub
        return null;
    }

}
