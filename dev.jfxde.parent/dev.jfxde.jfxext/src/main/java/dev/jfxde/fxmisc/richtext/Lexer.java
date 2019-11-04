package dev.jfxde.fxmisc.richtext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Lexer {

    private Pattern pattern;
    private List<String> groups;
    private String openingDelimiterPattern;

    private Lexer(String regex, List<String> groups, String openingDelimiterPattern) {
        this.pattern = Pattern.compile(regex);
        this.groups = groups;
        this.openingDelimiterPattern = openingDelimiterPattern;
    }

    static Lexer get(String language) {
        Lexer lexer = null;
        try (var bis = new BufferedReader(new InputStreamReader(Lexer.class.getResourceAsStream(language)))) {
            List<String> groups = Arrays.asList(bis.readLine().split(","));
            String openingDelimiterPattern = bis.readLine();
            String regex = bis.lines().collect(Collectors.joining());
            lexer = new Lexer(regex, groups, openingDelimiterPattern);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return lexer;
    }

   int tokenize(String input, BiConsumer<Integer, Token> consumer) {
        Matcher matcher = pattern.matcher(input);
        int lastEnd = 0;
        while (matcher.find()) {
            String type = groups.stream().filter(g -> matcher.group(g) != null).findFirst().orElse("");
            Token token = new Token(matcher.start(), matcher.end(), type, matcher.group());
            consumer.accept(lastEnd, token);
            lastEnd = matcher.end();
        }

        return lastEnd;
    }

    String getOpeningDelimiterPattern() {
        return openingDelimiterPattern;
    }
}
