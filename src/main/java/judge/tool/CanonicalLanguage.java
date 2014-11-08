package judge.tool;

public enum CanonicalLanguage {

    CPP("C++"),
    C("C"),
    JAVA("Java"),
    PASCAL("Pascal"),
    PYTHON("Python"),
    CSHARP("C#"),
    RUBY("Ruby"),
    OTHER("Other"),

    ;////////////////////////

    String literal;

    CanonicalLanguage(String literal) {
        this.literal = literal;
    }
}
