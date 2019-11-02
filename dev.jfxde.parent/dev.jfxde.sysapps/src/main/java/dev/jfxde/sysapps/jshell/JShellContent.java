package dev.jfxde.sysapps.jshell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.api.AppContext;
import dev.jfxde.fxmisc.richtext.TextStyleSpans;
import dev.jfxde.fxmisc.richtext.features.AreaFeatures;
import dev.jfxde.fxmisc.richtext.features.BlockEndFeature;
import dev.jfxde.fxmisc.richtext.features.CompletionFeature;
import dev.jfxde.fxmisc.richtext.features.CompletionItem;
import dev.jfxde.fxmisc.richtext.features.HighlightBlockDelimiterFeature;
import dev.jfxde.fxmisc.richtext.features.IndentationFeature;
import dev.jfxde.fxmisc.richtext.features.Lexer;
import dev.jfxde.fxmisc.richtext.features.LexerFeature;
import dev.jfxde.jfx.concurrent.CTask;
import dev.jfxde.jfx.scene.control.SplitConsoleView;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.layout.BorderPane;

public class JShellContent extends BorderPane {

    private static final String HISTORY_FILE_NAME = "history.json";

    private AppContext context;
    private SplitConsoleView consoleView;
    private Session session;
    private Completion completion;

    public JShellContent(AppContext context) {
        this.context = context;
        consoleView = new SplitConsoleView(loadHistory(), List.of("block-delimiter-match"));
        setCenter(consoleView);
        session = new Session(this);
        completion = new Completion(session);
        getProperties().put(getClass(), consoleView.getInputArea());
        setBehavior();
    }

    AppContext getContext() {
        return context;
    }

    SplitConsoleView getConsoleView() {
        return consoleView;
    }

    private void setBehavior() {

        consoleView.getConsoleModel().getInputToOutput().addListener((Change<? extends TextStyleSpans> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    List<? extends TextStyleSpans> added = new ArrayList<>(c.getAddedSubList());
                    for (TextStyleSpans span : added) {
                        session.processBatch(span.getText());
                    }
                }
            }
        });

        consoleView.getHistory().addListener((Change<? extends String> c) -> {

            while (c.next()) {

                if (c.wasAdded() || c.wasRemoved()) {
                    List<? extends String> history = new ArrayList<>(consoleView.getHistory());
                    context.dc().toJson(history, HISTORY_FILE_NAME);
                }
            }
        });

        Lexer lexer = Lexer.get("file.java", CommandProcessor.COMMAND_PATTERN);
        LexerFeature<CodeArea> lexerFeature = new LexerFeature<>(lexer);
        lexerFeature.setDisableHighlight(consoleView.getConsoleModel().getReadFromPipe());
        AreaFeatures.decorate(consoleView.getInputArea())
                .add(new CompletionFeature<>(this::codeCompletion, completion::loadDocumentation))
                .add(new BlockEndFeature<>())
                .add(new IndentationFeature<>())
                .add(lexerFeature)
                .add(new HighlightBlockDelimiterFeature<>())
                .init();
        consoleView.getOutputArea().getStylesheets().add(lexer.getCss());
    }

    private List<String> loadHistory() {

        List<String> history = context.dc().fromJson(HISTORY_FILE_NAME, List.class, List.of());
        return history;
    }

    private void codeCompletion(CompletionFeature<CodeArea> behavior) {

        CTask<Collection<CompletionItem>> task = CTask.create(() -> completion.getCompletionItems(behavior.getArea()))
                .onSucceeded(behavior::showCompletionItems);

        context.tc().executeSequentially(Session.PRIVILEDGED_TASK_QUEUE, task);
    }

    public void stop() {
        var task = CTask.create(() -> session.close())
                .onFinished(t -> consoleView.dispose());

        context.tc().executeSequentially(Session.PRIVILEDGED_TASK_QUEUE, task);
    }
}
