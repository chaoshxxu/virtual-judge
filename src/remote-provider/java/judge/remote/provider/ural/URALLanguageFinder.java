package judge.remote.provider.ural;

import java.util.HashMap;
import java.util.LinkedHashMap;

import judge.remote.RemoteOjInfo;
import judge.remote.language.LanguageFinder;
import judge.tool.Handler;

import org.springframework.stereotype.Component;

@Component
public class URALLanguageFinder implements LanguageFinder {

    @Override
    public RemoteOjInfo getOjInfo() {
        return URALInfo.INFO;
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
        LinkedHashMap<String, String> languageList = new LinkedHashMap<>();
        languageList.put("31", "FreePascal 2.6");
        languageList.put("9", "Visual C 2010");
        languageList.put("10", "Visual C++ 2010");
        languageList.put("25", "GCC 4.9");
        languageList.put("26", "G++ 4.9");
        languageList.put("27", "GCC 4.9 C11");
        languageList.put("28", "G++ 4.9 C++11");
        languageList.put("30", "Clang 3.5 C++14");
        languageList.put("32", "Java 1.8");
        languageList.put("11", "Visual C# 2010");
        languageList.put("15", "VB.NET 2010");
        languageList.put("34", "Python 2.7");
        languageList.put("35", "Python 3.4");
        languageList.put("14", "Go 1.3");
        languageList.put("18", "Ruby 1.9");
        languageList.put("19", "Haskell 7.6");
        languageList.put("33", "Scala 2.11");
        return languageList;
    }

    @Override
    public HashMap<String, String> getLanguagesAdapter() {
        HashMap<String, String> adapter = new HashMap<>();
        adapter.put("3", "31");
        adapter.put("12", "32");
        adapter.put("16", "34");
        adapter.put("17", "35");
        adapter.put("20", "25");
        adapter.put("21", "26");
        adapter.put("22", "27");
        adapter.put("23", "28");
        return adapter;
    }

}
