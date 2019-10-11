package dev.jfxde.sysapps.jshell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.jfxext.richtextfx.features.CompletionItem;
import dev.jfxde.jfxext.richtextfx.features.DocRef;
import dev.jfxde.jfxext.util.JavadocUtils;
import jdk.jshell.SourceCodeAnalysis.Documentation;
import jdk.jshell.SourceCodeAnalysis.QualifiedNames;
import picocli.AutoComplete;
import picocli.CommandLine;

public class Completion {

    private Session session;

    public Completion(Session session) {
        this.session = session;
    }

    Collection<CompletionItem> getCompletionItems(CodeArea inputArea) {

        return inputArea.getText().isBlank() || CommandProcessor.isCommand(inputArea.getText()) ? getCommandCompletionItems(inputArea)
                : getCodeCompletionItems(inputArea);
    }

    private Collection<CompletionItem> getCommandCompletionItems(CodeArea inputArea) {
        String[] args = inputArea.getText().strip().split(" +");
        int caretPosition = inputArea.getCaretPosition();
        int argsPosition = (int) inputArea.getText().substring(0, caretPosition).chars().filter(i -> i != 32).count();
        int argIndex = 0;
        int positionInArg = 0;

        int length = 0;
        for (int i = 0; i < args.length; i++) {
            length += args[i].length();

            if (length >= argsPosition) {
                argIndex = i;
                positionInArg = argsPosition - (length - args[i].length());
                break;
            }
        }

        List<CharSequence> candidates = new ArrayList<>();
        int anchor = AutoComplete.complete(session.getCommandProcessor().getCommandLine().getCommandSpec(), args, argIndex, positionInArg,
                caretPosition, candidates);

        if (candidates.size() == 1 && candidates.get(0).length() == 0 && args.length > 0) {
            args = new String[] { args[0], "" };
            argIndex = 1;
            positionInArg = 0;
            anchor = AutoComplete.complete(session.getCommandProcessor().getCommandLine().getCommandSpec(), args, argIndex, positionInArg,
                    caretPosition, candidates);
        }

        String arg = args[argIndex];

        List<CompletionItem> items = new ArrayList<>();

        for (CharSequence candidate : candidates) {

            if (candidate.length() == 0) {
                continue;
            }

            String name = arg.substring(0, positionInArg) + candidate;
            String docCode = args.length <= 1 ? name : (args[0] + "." + name);

            items.add(new CommandCompletionItem(inputArea, anchor, candidate.toString(), name, docCode, this::getCommandHelp));
        }

        return items;
    }

    private String getCommandHelp(DocRef docRef) {
        String help = "";
        CommandLine subcommand = session.getCommandProcessor().getCommandLine().getSubcommands().get(docRef.getDocCode());

        if (subcommand != null) {
            help = "<pre>" + subcommand.getUsageMessage() + "</pre>";
        } else {
            help = session.getContext().rc().getStringOrDefault(docRef.getDocCode(),
                    session.getContext().rc().getStringOrDefault(docRef.getSignature(), ""));
        }

        return help;
    }

    private Collection<CompletionItem> getCodeCompletionItems(CodeArea inputArea) {
        List<CompletionItem> items = new ArrayList<>();

        String code = inputArea.getText();
        int cursor = inputArea.getCaretPosition();

        int[] anchor = new int[1];

        Set<SuggestionCompletionItem> suggestionItems = session.getJshell().sourceCodeAnalysis()
                .completionSuggestions(code, cursor, anchor)
                .stream()
                .map(s -> new SuggestionCompletionItem(inputArea, code, s, anchor))
                .collect(Collectors.toSet());

        for (SuggestionCompletionItem item : suggestionItems) {

            List<Documentation> docs = session.getJshell().sourceCodeAnalysis().documentation(item.getDocRef().getDocCode(),
                    item.getDocRef().getDocCode().length(), false);

            if (docs.isEmpty()) {
                items.add(item);
            } else {
                items.addAll(docs.stream()
                        .map(d -> new SuggestionCompletionItem(inputArea, item.getSuggestion(), item.getAnchor(), item.getDocRef().getDocCode(),
                                d.signature(), this::loadDocumentation))
                        .collect(Collectors.toSet()));
            }
        }

        Collections.sort(items);

        QualifiedNames qualifiedNames = session.getJshell().sourceCodeAnalysis().listQualifiedNames(code, cursor);

        if (!qualifiedNames.isResolvable()) {
            Set<CompletionItem> names = qualifiedNames.getNames()
                    .stream()
                    .map(n -> new QualifiedNameCompletionItem(i -> session.getConsoleView().enter(i) , n, this::loadDocumentation))
                    .sorted()
                    .collect(Collectors.toSet());

            items.addAll(names);
        }

        return items;
    }

    String loadDocumentation(DocRef docRef) {
        Map<String, String> docBlockNames = session.getContext().rc().getStrings(JavadocUtils.getBlockTagNames());
        String documentation = JShellUtils.getDocumentation(session.getJshell(), docRef, docBlockNames);

        return documentation;
    }
}
