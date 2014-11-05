package judge.remote.provider.codeforces;

import java.util.HashMap;
import java.util.LinkedHashMap;

import judge.remote.RemoteOjInfo;
import judge.remote.language.LanguageFinder;
import judge.tool.Handler;

import org.springframework.stereotype.Component;

@Component
public class CodeForcesLanguageFinder implements LanguageFinder {

    @Override
    public RemoteOjInfo getOjInfo() {
        return CodeForcesInfo.INFO;
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
        LinkedHashMap<String, String> languageList = new LinkedHashMap<>();
        languageList.put("10", "GNU C 4");
        languageList.put("1", "GNU C++ 4.7");
        languageList.put("16", "GNU C++0x 4");
        languageList.put("2", "Microsoft Visual C++ 2010");
        languageList.put("9", "C# Mono 2.10");
        languageList.put("29", "MS C# .NET 4");
        languageList.put("28", "D DMD32 Compiler v2");
        languageList.put("32", "Go 1.2");
        languageList.put("12", "Haskell GHC 7.6");
        languageList.put("5", "Java 6");
        languageList.put("23", "Java 7");
        languageList.put("36", "Java 8");
        languageList.put("19", "OCaml 4");
        languageList.put("3", "Delphi 7");
        languageList.put("4", "Free Pascal 2");
        languageList.put("13", "Perl 5.12");
        languageList.put("6", "PHP 5.3");
        languageList.put("7", "Python 2.7");
        languageList.put("31", "Python 3.3");
        languageList.put("8", "Ruby 2");
        languageList.put("20", "Scala 2.11");
        languageList.put("34", "JavaScript V8 3");
        languageList.put("14", "ActiveTcl 8.5");
        languageList.put("15", "Io-2008-01-07 (Win32)");
        languageList.put("17", "Pike 7.8");
        languageList.put("18", "Befunge");
        languageList.put("22", "OpenCobol 1.0");
        languageList.put("25", "Factor");
        languageList.put("26", "Secret_171");
        languageList.put("27", "Roco");
        languageList.put("33", "Ada GNAT 4.7");
        languageList.put("38", "Mysterious Language");
        languageList.put("39", "FALSE");
        return languageList;
    }

    @Override
    public HashMap<String, String> getLanguagesAdapter() {
        // TODO Auto-generated method stub
        return null;
    }

}
