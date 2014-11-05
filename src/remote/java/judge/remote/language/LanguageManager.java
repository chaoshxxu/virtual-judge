package judge.remote.language;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import judge.remote.RemoteOj;

import org.springframework.stereotype.Component;

@Component
public class LanguageManager {

    private Map<RemoteOj, LinkedHashMap<String, String>> ojLanguages = new HashMap<>();
    
    public LinkedHashMap<String, String> getLanguages(RemoteOj remoteOj) {
        if (!ojLanguages.containsKey(remoteOj)) {
            ojLanguages.put(remoteOj, LanguageFindersHolder.getLanguageFinder(remoteOj).getDefaultLanguages());
        }
        return ojLanguages.get(remoteOj);
    }
    
    public LinkedHashMap<String, String> getLanguages(String remoteOj) {
        RemoteOj oj = RemoteOj.valueOf(remoteOj);
        return getLanguages(oj);
    }
}
