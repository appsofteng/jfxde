package dev.jfxde.sysapps.jshell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import dev.jfxde.api.AppContext;
import dev.jfxde.fxmisc.richtext.CodeAreaExtender;
import dev.jfxde.fxmisc.richtext.CompletionItem;
import dev.jfxde.fxmisc.richtext.TextStyleSpans;
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

        CodeAreaExtender.get(consoleView.getInputArea(), "java")
                .highlighting(consoleView.getConsoleModel().getReadFromPipe())
                .completion(this::codeCompletion, completion::loadDocumentation)
                .indentation();

        CodeAreaExtender.get(consoleView.getOutputArea(), "java")
                .style();

//        AreaExtensions.decorate(consoleView.getInputArea())
//                .add(new BlockEndExtension<>())
//                .add(new HighlightBlockDelimiterExtension<>())

    }

    private List<String> loadHistory() {

        List<String> history = context.dc().fromJson(HISTORY_FILE_NAME, List.class, List.of());
        return history;
    }

    private void codeCompletion(Consumer<Collection<CompletionItem>> behavior) {

        CTask<Collection<CompletionItem>> task = CTask.create(() -> completion.getCompletionItems(consoleView.getInputArea()))
                .onSucceeded(behavior);

        context.tc().executeSequentially(Session.PRIVILEDGED_TASK_QUEUE, task);
    }

    public void stop() {
        var task = CTask.create(() -> session.close())
                .onFinished(t -> consoleView.dispose());

        context.tc().executeSequentially(Session.PRIVILEDGED_TASK_QUEUE, task);
    }
}
