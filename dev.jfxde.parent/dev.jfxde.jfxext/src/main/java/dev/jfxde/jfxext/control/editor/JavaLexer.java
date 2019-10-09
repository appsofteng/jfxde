package dev.jfxde.jfxext.control.editor;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaLexer extends Lexer {

    private final String[] KEYWORDS;
    private final String KEYWORD_PATTERN;
    private final Pattern PATTERN;

    public JavaLexer() {
        this(LANG_KEYWORDS);
    }

    private JavaLexer(String[] keywords) {
        this.KEYWORDS = keywords;
        this.KEYWORD_PATTERN = "\\b(?:" + String.join("|", KEYWORDS) + ")\\b";
        this.PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<PARENOPEN>" + PAREN_OPEN_PATTERN + ")"
            + "|(?<PARENCLOSE>" + PAREN_CLOSE_PATTERN + ")"
            + "|(?<BRACEOPEN>" + BRACE_OPEN_PATTERN + ")"
            + "|(?<BRACECLOSE>" + BRACE_CLOSE_PATTERN + ")"
            + "|(?<BRACKETOPEN>" + BRACKET_OPEN_PATTERN + ")"
            + "|(?<BRACKETCLOSE>" + BRACKET_CLOSE_PATTERN + ")"
            + "|(?<GENERICTYPE>" + GENERIC_TYPE_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<CHAR>" + CHAR_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
        );
    }

    private static final String[] LANG_KEYWORDS = new String[] {
        "abstract", "assert", "boolean", "break", "byte",
        "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else",
        "enum", "extends", "final", "finally", "float",
        "for", "goto", "if", "implements", "import",
        "instanceof", "int", "interface", "long", "native",
        "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super",
        "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "var", "void", "volatile", "while"
    };

    private static final String[] MODULE_KEYWORDS = new String[] {
        "exports", "module", "opens", "provides", "requires", "static", "to",
        "transitive", "uses", "with"
    };


    private static final String PAREN_OPEN_PATTERN = "\\(";
    private static final String PAREN_CLOSE_PATTERN = "\\)";
    private static final String BRACE_OPEN_PATTERN = "\\{";
    private static final String BRACE_CLOSE_PATTERN = "\\}";
    private static final String BRACKET_OPEN_PATTERN = "\\[";
    private static final String BRACKET_CLOSE_PATTERN = "\\]";
    private static final String GENERIC_TYPE_PATTERN = "(?:\\<[\\w\\s\\?\\<\\>,]*\\>)|(?:\\<.+extends.+\\&.+\\>)";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String CHAR_PATTERN = "'\\S+'";
    private static final String STRING_PATTERN = "\"(?:[^\"\\\\]|\\\\.)*+\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*[\\s\\S]*?\\*/";

    private static final Pattern GENERIC_TYPE_TOKEN_PATTERN = Pattern.compile(
        "(?<KEYWORD>\\b(?:extends|super)\\b)"
        + "|(?<CHEVRONOPEN>\\<)|(?<CHEVRONCLOSE>\\>)"
    );

    private static final Pattern DELIMITER_PATTERN = Pattern.compile(
          "(?<PARENOPEN>" + PAREN_OPEN_PATTERN + ")"
        + "|(?<PARENCLOSE>" + PAREN_CLOSE_PATTERN + ")"
        + "|(?<BRACEOPEN>" + BRACE_OPEN_PATTERN + ")"
        + "|(?<BRACECLOSE>" + BRACE_CLOSE_PATTERN + ")"
        + "|(?<BRACKETOPEN>" + BRACKET_OPEN_PATTERN + ")"
        + "|(?<BRACKETCLOSE>" + BRACKET_CLOSE_PATTERN + ")"
        + "|(?<CHEVRONOPEN>\\<)|(?<CHEVRONCLOSE>\\>)"
    );

    private static final Map<String,String> DELIMITERS = Map.of("}", "{", ")", "(", "]", "[", ">", "<");
    private static final String OPENING_DELIMITERS_PATTERN = "[\\{\\(\\[\\<]";
    private static final Map<String,Pattern> TOKEN_PATTERNS = Map.of("generic-type", GENERIC_TYPE_TOKEN_PATTERN, "string", DELIMITER_PATTERN);
    private static final Map<String,BiFunction<Matcher,String,Token>> TOKEN_FUNCTIONS = Map.of("generic-type", JavaLexer::getGenericTypeToken, "string", JavaLexer::getDefaultToken);

    public String getCss() {

        return getClass().getResource("java-syntax.css").toExternalForm();
    }

    @Override
    String getCssEdit() {
        return getClass().getResource("java-syntax-edit.css").toExternalForm();
    }

    Pattern getPattern() {
        return PATTERN;
    }

    boolean isDelimiter(String str) {
        return DELIMITERS.keySet().contains(str) || DELIMITERS.values().contains(str);
    }

    boolean isOpeningDelimiter(String str) {
        return DELIMITERS.values().contains(str);
    }

    boolean isClosingDelimiter(String str) {
        return DELIMITERS.keySet().contains(str);
    }

    String getOpeningDelimitersPattern() {
        return OPENING_DELIMITERS_PATTERN;
    }

    String getOpeningDelimiter(String closingDelimiter) {
        return DELIMITERS.get(closingDelimiter);
    }

    String getSingleLineComment() {
        return "//";
    }

    Token getToken(Matcher matcher, String defaultStyleClass) {
        String value = "";
        String styleClass =
                (value = matcher.group("KEYWORD")) != null ? "keyword" :
                (value = matcher.group("PARENOPEN")) != null ? "paren-open" :
                (value = matcher.group("PARENCLOSE")) != null ? "paren-close" :
                (value = matcher.group("BRACEOPEN")) != null ? "brace-open" :
                (value = matcher.group("BRACECLOSE")) != null ? "brace-close" :
                (value = matcher.group("BRACKETOPEN")) != null ? "bracket-open" :
                (value = matcher.group("BRACKETCLOSE")) != null ? "bracket-close" :
                (value = matcher.group("GENERICTYPE")) != null ? "generic-type" :
                (value = matcher.group("SEMICOLON")) != null ? "semicolon" :
                (value = matcher.group("CHAR")) != null ? "char" :
                (value = matcher.group("STRING")) != null ? "string" :
                (value = matcher.group("COMMENT")) != null ? "comment" :
                defaultStyleClass;

        Token token = new Token(styleClass, value, TOKEN_PATTERNS.get(styleClass), TOKEN_FUNCTIONS.get(styleClass));

        return token;
    }

    private static Token getGenericTypeToken(Matcher matcher, String defaultStyleClass) {
        String value = "";
        String styleClass =
                (value = matcher.group("KEYWORD")) != null ? "keyword" :
                (value = matcher.group("CHEVRONOPEN")) != null ? "chevron-open" :
                (value = matcher.group("CHEVRONCLOSE")) != null ? "chevron-close" :
                defaultStyleClass;

        Token token = new Token(styleClass, value);

        return token;
    }

    private static Token getDefaultToken(Matcher matcher, String defaultStyleClass) {
        String value = matcher.group();

        Token token = new Token(defaultStyleClass, value);

        return token;
    }

}
