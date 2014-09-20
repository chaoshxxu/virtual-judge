package judge.remote.language;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import judge.remote.RemoteOj;
import judge.remote.language.common.LanguageFinder;
import judge.tool.Handler;

@Component
public class SCULanguageFinder implements LanguageFinder {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.SCU;
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

}
