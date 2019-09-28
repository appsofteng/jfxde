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
    private Function<String, String> documentation;
    private ContextMenu contextMenu;

    public DocPopup(Function<String, String> documentation) {
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
                    String newDoc = documentation.apply(url.strip());
                    if (!newDoc.isEmpty()) {
                        webView.getEngine().getLoadWorker().cancel();
                        webView.getEngine().load("");
                        webView.getEngine().loadContent(newDoc);
                    }
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
        back.textProperty().bind(FXResourceBundle.getDefaultBundle().getStringBinding("back"));

        MenuItem forward = new MenuItem();
        forward.textProperty().bind(FXResourceBundle.getDefaultBundle().getStringBinding("forward"));

        contextMenu.getItems().addAll(back, forward);
    }

    void loadContent(String doc) {
        webView.getEngine().loadContent(doc);
    }
}
