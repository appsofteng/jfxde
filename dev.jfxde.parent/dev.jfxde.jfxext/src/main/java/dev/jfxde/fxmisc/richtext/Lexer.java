package dev.jfxde.fxmisc.richtext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Token closeTokenOnPosition;

    private Lexer(String regex, List<String> groups, String openingTokenPattern) {
        this.pattern = Pattern.compile(regex);
        this.groups = groups;
        this.openTokenPattern = openingTokenPattern;
        extractOpenCloseTokens();
    }

    private void extractOpenCloseTokens() {

        String open = null;
        for (var group : groups) {
            if (group.toLowerCase().endsWith("open")) {
                open = group;
            } else if (group.toLowerCase().endsWith("close")) {
                openCloseTypes.put(open, group);
                closeOpenTypes.put(group, open);
            }
        }
    }

    public Token getCloseTokenOnPosition() {
        return closeTokenOnPosition;
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

    int tokenize(String input, int changePosition, BiConsumer<Integer, Token> consumer) {
        Matcher matcher = pattern.matcher(input);
        int lastEnd = 0;
        tokens.clear();
        tokenStack.clear();
        closeTokenOnPosition = null;

        while (matcher.find()) {
            String type = groups.stream().filter(g -> matcher.group(g) != null).findFirst().orElse("");
            Token token = new Token(matcher.start(), matcher.end(), type, matcher.group());
            updateStack(token, changePosition);
            tokens.add(token);
            consumer.accept(lastEnd, token);
            lastEnd = matcher.end();
        }

        return lastEnd;
    }

    private void updateStack(Token token, int changePosition) {
        String closeType = openCloseTypes.get(token.getType());
        if (closeType != null) {
            tokenStack.computeIfAbsent(closeType, k -> new ArrayDeque<>()).push(token);
        } else {
            String openType = closeOpenTypes.get(token.getType());
            if (openType != null) {
                var stack = tokenStack.get(token.getType());
                if (stack != null) {
                    token.setOppositeToken(stack.pop());
                    if (token.isWithin(changePosition)) {
                        closeTokenOnPosition = token;
                    }
                }
            }
        }
    }

    String getOpenTokenPattern() {
        return openTokenPattern;
    }

    Token getOppositeToken(int position) {
        Token result = null;
        int index = getTokenIndex(position);

        if (index < 0) {
            return null;
        }

        Token token = tokens.get(index);
        String oppositeType = openCloseTypes.get(token.getType());

        if (oppositeType != null) {
            result = getOppositeToken(token, index, 1, oppositeType);

        } else {
            oppositeType = closeOpenTypes.get(token.getType());
            if (oppositeType != null) {
                result = getOppositeToken(token, index, -1, oppositeType);

            }
        }

        return result;
    }

    private int getTokenIndex(int position) {

        if (position < 0 || tokens.isEmpty() || position >= tokens.get(tokens.size() - 1).getEnd()) {
            return -1;
        }

        int index = tokens.size() / 2;

        while (index >= 0 && index < tokens.size()) {
            Token token = tokens.get(index);

            if (position < token.getStart()) {
                index = index / 2;
            } else if (position >= token.getEnd()) {
                index = (tokens.size() + index) / 2;
            } else {
                break;
            }
        }

        return index;
    }

    private Token getOppositeToken(Token token, int index, int step, String oppositeType) {

        Token opposite = null;
        int i = index + step;
        int level = 1;

        while (i >= 0 && i < tokens.size()) {
            Token next = tokens.get(i);

            if (next.getType().equals(token.getType())) {
                level++;
            } else if (next.getType().equals(oppositeType)) {
                level--;
                if (level == 0) {
                    opposite = next;
                    break;
                }
            }

            i += step;
        }

        return opposite;
    }
}
