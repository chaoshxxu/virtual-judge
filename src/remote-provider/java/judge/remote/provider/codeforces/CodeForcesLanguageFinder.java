package judge.remote.provider.codeforces;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import judge.remote.RemoteOj;
import judge.remote.language.LanguageFinder;
import judge.tool.Handler;

@Component
public class CodeForcesLanguageFinder implements LanguageFinder {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.CodeForces;
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
        languageList.put("1", "GNU C++ 4.6");
        languageList.put("2", "Microsoft Visual C++ 2005+");
        languageList.put("3", "Delphi 7");
        languageList.put("4", "Free Pascal 2");
        languageList.put("5", "Java 6");
        languageList.put("6", "PHP 5.2+");
        languageList.put("7", "Python 2.6+");
        languageList.put("8", "Ruby 1.7+");
        languageList.put("9", "C# Mono 2.6+");
        languageList.put("10", "GNU C 4");
        languageList.put("12", "Haskell GHC 6.12");
        languageList.put("13", "Perl 5.12+");
        languageList.put("14", "ActiveTcl 8.5");
        languageList.put("15", "Io-2008-01-07 (Win32)");
        languageList.put("16", "GNU C++0x 4");
        languageList.put("17", "Pike 7.8");
        languageList.put("19", "OCaml 3.12");
        languageList.put("20", "Scala 2.9");
        languageList.put("23", "Java 7");
        languageList.put("28", "D DMD32 Compiler v2");
        return languageList;
    }

}
