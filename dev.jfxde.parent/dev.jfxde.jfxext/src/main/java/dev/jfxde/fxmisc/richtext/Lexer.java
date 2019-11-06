package dev.jfxde.fxmisc.richtext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Lexer {

    private Pattern pattern;
    private List<String> groups;
    private String openTokenPattern;
    private Map<String, String> openCloseTypes = new HashMap<>();
    private Map<String, String> closeOpenTypes = new HashMap<>();
    private List<Token> tokens = new ArrayList<>();
    private Map<String, Deque<Token>> tokenStack = new HashMap<>();
    private Token closeTokenOnChangePosition;

    private Lexer(String regex, List<String> groups, String openingTokenPattern) {
        this.pattern = Pattern.compile(regex);
        this.groups = groups;
        this.openTokenPattern = openingTokenPattern;
        extractOpenCloseTokens();
    }

    private void extractOpenCloseTokens() {

        String open = null;
        for (var group : groups) {
            if (Token.isOpen(group)) {
                open = group;
            } else if (Token.isClose(group)) {
                openCloseTypes.put(open, group);
                closeOpenTypes.put(group, open);
            }
        }
    }

    public Token getCloseTokenOnChangePosition() {
        return closeTokenOnChangePosition;
    }

    static Lexer get(String language) {
        Lexer lexer = null;
        try (var bis = new BufferedReader(new InputStreamReader(Lexer.class.getResourceAsStream(language)))) {
            List<String> groups = Arrays.asList(bis.readLine().split(","));
            String openingTokenPattern = bis.readLine();
            String regex = bis.lines().collect(Collectors.joining());
            lexer = new Lexer(regex, groups, openingTokenPattern);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return lexer;
    }

    int tokenize(String input, int caretPosition, BiConsumer<Integer, Token> consumer) {
        Matcher matcher = pattern.matcher(input);
        int lastEnd = 0;
        tokens.clear();
        tokenStack.clear();
        closeTokenOnChangePosition = null;

        while (matcher.find()) {
            String type = groups.stream().filter(g -> matcher.group(g) != null).findFirst().orElse("");
            Token token = new Token(matcher.start(), matcher.end(), type, matcher.group(), caretPosition);
            updateStack(token);
            tokens.add(token);
            consumer.accept(lastEnd, token);
            lastEnd = matcher.end();
        }

        return lastEnd;
    }

    private void updateStack(Token token) {
        String closeType = openCloseTypes.get(token.getType());
        if (closeType != null) {
            tokenStack.computeIfAbsent(closeType, k -> new ArrayDeque<>()).push(token);
        } else {
            String openType = closeOpenTypes.get(token.getType());
            if (openType != null) {
                var stack = tokenStack.get(token.getType());
                if (stack != null) {
                    var opposite = stack.pop();
                    token.setOppositeToken(opposite);
                    opposite.setOppositeToken(token);
                    if (token.isOnCaretPosition()) {
                        closeTokenOnChangePosition = token;
                    }
                }
            }
        }
    }

    String getOpenTokenPattern() {
        return openTokenPattern;
    }

    Token getToken(int caretPosition) {

        if (tokens.isEmpty() || caretPosition < tokens.get(0).getStart() || caretPosition > tokens.get(tokens.size() - 1).getEnd()) {
            return null;
        }

        Token token = null;
        int index = tokens.size() / 2;
        Set<Integer> indices = new HashSet<>();

        while (index >= 0 && index < tokens.size() && !indices.contains(index)) {
            token = tokens.get(index);
            indices.add(index);

            if (caretPosition < token.getStart()) {
                index = index / 2;
            } else if (caretPosition > token.getEnd()) {
                index = (tokens.size() + index) / 2;
            } else {
                break;
            }

            token = null;
        }

        return token;
    }
}
