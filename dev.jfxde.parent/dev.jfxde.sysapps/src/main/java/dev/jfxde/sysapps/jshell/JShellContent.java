package dev.jfxde.sysapps.jshell;

import java.util.ArrayList;
import java.util.List;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.SplitConsoleView;
import dev.jfxde.jfxext.control.editor.CompletionBehavior;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.jfxext.util.TaskUtils;
import dev.jfxde.logic.JsonUtils;
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
        consoleView = new SplitConsoleView(loadHistory());
        setCenter(consoleView);
        session = new Session(context, this, consoleView.getConsoleModel(), consoleView.getHistory());
        completion = new Completion(session);
        setBehavior();
    }

    private void setBehavior() {

        consoleView.getConsoleModel().getInputToOutput().addListener((Change<? extends TextStyleSpans> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    List<? extends TextStyleSpans> added = new ArrayList<>(c.getAddedSubList());
                    for (TextStyleSpans span : added) {
                        session.process(span.getText());
                    }
                }
            }
        });

        consoleView.getHistory().addListener((Change<? extends String> c) -> {

            while (c.next()) {

                if (c.wasAdded() || c.wasRemoved()) {
                    List<? extends String> history = new ArrayList<>(consoleView.getHistory());
                    context.tc().executeSequentially(TaskUtils
                            .createTask(() -> JsonUtils.toJson(history, context.fc().getAppDataDir().resolve(HISTORY_FILE_NAME))));
                }
            }
        });

        consoleView.getEditor().add(new CompletionBehavior<>(this::codeCompletion, completion::loadDocumentation));
    }

    private List<String> loadHistory() {
        @SuppressWarnings("unchecked")
        List<String> history = JsonUtils.fromJson(context.fc().getAppDataDir().resolve(HISTORY_FILE_NAME), List.class, List.of());
        return history;
    }

    private void codeCompletion(CompletionBehavior<CodeArea> behavior) {

        context.tc().executeSequentially(TaskUtils.createTask(() -> completion.getCompletionItems(behavior.getArea()), behavior::showCompletionItems));
    }

    public void stop() {
        context.tc().executeSequentially(TaskUtils.createTask(() -> session.getJshell().close(), () -> consoleView.dispose()));
    }
}
