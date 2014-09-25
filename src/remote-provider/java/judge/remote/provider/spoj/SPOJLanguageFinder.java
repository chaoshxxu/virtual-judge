package judge.remote.provider.spoj;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import judge.remote.RemoteOj;
import judge.remote.language.LanguageFinder;
import judge.tool.Handler;

@Component
public class SPOJLanguageFinder implements LanguageFinder {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.SPOJ;
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
        languageList.put("7", "ADA 95 (gnat 4.3.2)");
        languageList.put("13", "Assembler (nasm 2.03.01)");
        languageList.put("104", "Awk (gawk-3.1.6)");
        languageList.put("28", "Bash (bash-4.0.37)");
        languageList.put("12", "Brainf**k (bff 1.0.3.1)");
        languageList.put("11", "C (gcc 4.3.2)");
        languageList.put("27", "C# (gmcs 2.0.1)");
        languageList.put("41", "C++ (g++ 4.3.2)");
        languageList.put("1", "C++ (g++ 4.0.0-8)");
        languageList.put("34", "C99 strict (gcc 4.3.2)");
        languageList.put("14", "Clips (clips 6.24)");
        languageList.put("111", "Clojure (clojure 1.1.0)");
        languageList.put("31", "Common Lisp (sbcl 1.0.18)");
        languageList.put("32", "Common Lisp (clisp 2.44.1)");
        languageList.put("20", "D (gdc 4.1.3)");
        languageList.put("36", "Erlang (erl 5.6.3)");
        languageList.put("124", "F# (fsharp 2.0.0)");
        languageList.put("5", "Fortran 95 (gfortran 4.3.2)");
        languageList.put("114", "Go (gc 2010-07-14)");
        languageList.put("21", "Haskell (ghc 6.10.4)");
        languageList.put("16", "Icon (iconc 9.4.3)");
        languageList.put("9", "Intercal (ick 0.28-4)");
        languageList.put("24", "JAR (JavaSE 6)");
        languageList.put("10", "Java (JavaSE 6)");
        languageList.put("35", "JavaScript (rhino 1.7R1-2)");
        languageList.put("26", "Lua (luac 5.1.3)");
        languageList.put("30", "Nemerle (ncc 0.9.3)");
        languageList.put("25", "Nice (nicec 0.9.6)");
        languageList.put("8", "Ocaml (ocamlopt 3.10.2)");
        languageList.put("22", "Pascal (fpc 2.2.4)");
        languageList.put("2", "Pascal (gpc 20070904)");
        languageList.put("3", "Perl (perl 5.12.1)");
        languageList.put("29", "PHP (php 5.2.6)");
        languageList.put("19", "Pike (pike 7.6.112)");
        languageList.put("15", "Prolog (swipl 5.6.58)");
        languageList.put("4", "Python (python 2.7)");
        languageList.put("116", "Python 3 (python 3.2.3)");
        languageList.put("126", "Python 3 nbc (python 3.2.3 nbc)");
        languageList.put("17", "Ruby (ruby 1.9.3)");
        languageList.put("39", "Scala (scala 2.8.0)");
        languageList.put("33", "Scheme (guile 1.8.5)");
        languageList.put("18", "Scheme (stalin 0.11)");
        languageList.put("46", "Sed (sed-4.2)");
        languageList.put("23", "Smalltalk (gst 3.0.3)");
        languageList.put("38", "Tcl (tclsh 8.5.3)");
        languageList.put("42", "TECS ()");
        languageList.put("62", "Text (plain text)");
        languageList.put("6", "Whitespace (wspace 0.3)");
        return languageList;
    }

}
