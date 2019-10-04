package dev.jfxde.jfxext.control.editor;

import static javafx.scene.input.MouseButton.PRIMARY;
import static org.fxmisc.wellbehaved.event.EventPattern.mousePressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

import java.util.function.Function;

import org.fxmisc.wellbehaved.event.Nodes;

import dev.jfxde.jfxext.util.FXResourceBundle;
import dev.jfxde.jfxext.util.JSUtils;
import dev.jfxde.jfxext.util.LayoutUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

public class DocPopup extends Tooltip {

    private WebView webView = new WebView();
    private Function<DocRef, String> documentation;
    private ContextMenu contextMenu;
    private ObservableList<DocRef> history = FXCollections.observableArrayList();
    private IntegerProperty historyIndex = new SimpleIntegerProperty(-1);

    public DocPopup(Function<DocRef, String> documentation) {
        this.documentation = documentation;
        setMinSize(10, 10);
        setPrefSize(CompletionPopup.DEFAULT_WIDTH, CompletionPopup.DEFAULT_HEIGHT);
        webView.setFocusTraversable(false);
        webView.setContextMenuEnabled(false);
        StackPane pane = new StackPane(webView);
        pane.setPadding(new Insets(5));
        setGraphic(pane);
        LayoutUtils.makeResizable(this, pane, 5);
        createContextMenu();
        setBehavior();
    }

    private void setBehavior() {
        Nodes.addInputMap(webView,
                sequence(consume(mousePressed(PRIMARY).onlyIf(e -> e.getClickCount() == 1), e -> webView.setFocusTraversable(true))));

        webView.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {

            if (e.getButton() == MouseButton.PRIMARY) {
                String url = JSUtils.getLinkUrl(webView.getEngine(), e.getX(), e.getY());
                if (url != null) {
                    history.remove(getHistoryIndex() + 1, history.size());
                    loadContent(new DocRef(url, documentation));
                }
            }

            if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(webView.getScene().getWindow(), e.getScreenX(), e.getScreenY());
            }
        });
    }

    private void createContextMenu() {
        contextMenu = new ContextMenu();
        contextMenu.setAutoHide(true);

        MenuItem back = new MenuItem();
        back.textProperty().bind(FXResourceBundle.getBundle().getStringBinding("back"));
        back.disableProperty().bind(historyIndex.lessThanOrEqualTo(0));
        back.setOnAction(e -> back());

        MenuItem forward = new MenuItem();
        forward.textProperty().bind(FXResourceBundle.getBundle().getStringBinding("forward"));
        forward.disableProperty().bind(Bindings.isEmpty(history).or(historyIndex.isEqualTo(Bindings.size(history).subtract(1))));
        forward.setOnAction(e -> forward());

        contextMenu.getItems().addAll(back, forward);
    }

    boolean loadContent(DocRef docRef) {

        if (docRef.getDocCode().isEmpty()) {
            return false;
        }

        String doc = docRef.getDocumentation();
        if (!doc.isEmpty()) {
            history.add(docRef);
            moveHistory(1);
            webView.getEngine().getLoadWorker().cancel();
            webView.getEngine().load("");
            webView.getEngine().loadContent(doc);
        } else if (docRef.isUrl()) {
            history.add(docRef);
            moveHistory(1);
        }

        return !doc.isBlank();
    }

    private void load(DocRef docRef) {
        String doc = docRef.getDocumentation();
        if (!doc.isEmpty()) {
            webView.getEngine().getLoadWorker().cancel();
            webView.getEngine().load("");
            webView.getEngine().loadContent(doc);
        }
    }

    private int getHistoryIndex() {
        return historyIndex.get();
    }

    private void moveHistory(int value) {
        historyIndex.set(historyIndex.get() + value);
    }

    private void back() {
        if (getHistoryIndex() > 0) {
            moveHistory(-1);
            loadHistory();
        }
    }

    private void forward() {
        if (getHistoryIndex() + 1 < history.size()) {
            moveHistory(1);
            loadHistory();
        }
    }

    private void loadHistory() {
        DocRef docRef = history.get(getHistoryIndex());

        if (docRef.isUrl()) {
            webView.getEngine().load(docRef.getDocCode());
        } else {
            load(docRef);
        }
    }
}
