package judge.remote.language;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import judge.remote.RemoteOj;
import judge.remote.language.common.LanguageFinder;
import judge.tool.Handler;

@Component
public class NBUTLanguageFinder implements LanguageFinder {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.NBUT;
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
        languageList.put("1", "GCC");
        languageList.put("2", "G++");
        languageList.put("4", "FPC");
        return languageList;
    }

}
