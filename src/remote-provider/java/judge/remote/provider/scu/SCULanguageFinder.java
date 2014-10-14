package judge.remote.provider.scu;

import java.util.HashMap;
import java.util.LinkedHashMap;

import judge.remote.RemoteOjInfo;
import judge.remote.language.LanguageFinder;
import judge.tool.Handler;

import org.springframework.stereotype.Component;

@Component
public class SCULanguageFinder implements LanguageFinder {

    @Override
    public RemoteOjInfo getOjInfo() {
        return SCUInfo.INFO;
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
        languageList.put("C++ (G++-3)", "C++ (G++-3)");
        languageList.put("C (GCC-3)", "C (GCC-3)");
        languageList.put("JAVA", "JAVA");
        languageList.put("PASCAL (GPC)", "PASCAL (GPC)");
        return languageList;
    }

    @Override
    public HashMap<String, String> getLanguagesAdapter() {
        // TODO Auto-generated method stub
        return null;
    }

}
