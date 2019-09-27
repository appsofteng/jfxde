package dev.jfxde.jfxext.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import com.github.javaparser.javadoc.description.JavadocInlineTag.Type;
import com.github.javaparser.utils.Utils;

public final class JavadocUtils {

    private JavadocUtils() {
    }

    public static Set<String> getBlockTagNames() {
        Set<String> names = Arrays.stream(JavadocBlockTag.Type.values()).map(t -> Utils.screamingToCamelCase(t.name())).collect(Collectors.toSet());
        names.add("apiNote");
        names.add("implNote");
        names.add("implSpec");
        names.add("jls");

        return names;
    }

    public static String toHtml(String source, Map<String, String> blockNames) {

        JavadocComment comment = new JavadocComment(source);
        Javadoc javadoc = comment.parse();

        String html = javadoc.getDescription().getElements()
                .stream()
                .map(JavadocUtils::descriptionElementToHtml)
                .collect(Collectors.joining());

        List<String> blockPassed = new ArrayList<>();
        html += "<br><br>" + javadoc.getBlockTags().stream().map(t -> blockTagToHtml(t, blockNames, blockPassed)).collect(Collectors.joining());

        return html;
    }

    private static String descriptionElementToHtml(JavadocDescriptionElement element) {

        String html = "";

        if (element instanceof JavadocInlineTag) {
            JavadocInlineTag tag = ((JavadocInlineTag) element);
            html = getInlineTagHtml(tag);
        } else {
            html = element.toText();
        }

        return html;
    }

    private static String getInlineTagHtml(JavadocInlineTag tag) {
        String html = "";

        if (tag.getContent() != null) {
            if (tag.getType() == Type.CODE) {
                html = String.format(" <code>%s</code> ", tag.getContent());
            } else if (tag.getType() == Type.LINK || tag.getType() == Type.LINKPLAIN) {
                String[] parts = tag.getContent().strip().split(" +");
                if (parts.length > 1) {
                    html = String.format(" <a href=\"%s\">%s</a> ", parts[0], parts[1]);
                } else if (parts.length == 1) {
                    html = String.format(" <a href=\"%s\">%s</a> ", parts[0], parts[0].replace("#", "."));
                }

                if (tag.getType() == Type.LINK) {
                    html = String.format("<code>%s</code>", html);
                }
            }
        }

        return html;
    }

    private static String blockTagToHtml(JavadocBlockTag tag, Map<String, String> blockNames, List<String> blockPassed) {
        StringBuilder html = new StringBuilder();

        if (!blockPassed.contains(tag.getTagName())) {
            blockPassed.add(tag.getTagName());
            html.append(String.format("<strong>%s:</strong><br>", blockNames.getOrDefault(tag.getTagName(), "@" + tag.getTagName())));
        }

        html.append("<p>");

        if (tag.getType() == JavadocBlockTag.Type.SEE) {
            seeTagToHtml(tag, html);
        } else if (tag.getType() == JavadocBlockTag.Type.THROWS || tag.getType() == JavadocBlockTag.Type.EXCEPTION) {
            throwsTagToHtml(tag, html);
        } else {

            if (tag.getName().isPresent()) {
                html.append(String.format("<code>%s</code> - ", tag.getName().get()));
            }

            html.append(tag.getContent().getElements().stream().map(JavadocUtils::descriptionElementToHtml).collect(Collectors.joining()));
        }

        html.append("</p>");

        return html.toString();
    }

    private static void seeTagToHtml(JavadocBlockTag tag, StringBuilder html) {
        String name = tag.getContent().getElements().stream().map(JavadocUtils::descriptionElementToHtml).collect(Collectors.joining()).strip();

        html.append(String.format("<code><a href=\"%s\">%s</a></code>", name, name.replace("#", ".")));
    }

    private static void throwsTagToHtml(JavadocBlockTag tag, StringBuilder html) {
        String content = tag.getContent().getElements().stream().map(JavadocUtils::descriptionElementToHtml).collect(Collectors.joining()).strip();
        String[] parts = content.split(" +", 2);

        String name = parts.length > 0 ? parts[0].strip() : "";
        String description = parts.length > 1 ? parts[1].strip() : "";

        html.append(String.format("<code><a href=\"%s\">%s</a></code> - %s", name, name.replace("#", "."), description));
    }
}
