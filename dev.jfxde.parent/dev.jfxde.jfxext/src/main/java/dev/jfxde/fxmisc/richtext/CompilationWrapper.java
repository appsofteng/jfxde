package dev.jfxde.fxmisc.richtext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.tools.Diagnostic;

import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.event.MouseOverTextEvent;

import dev.jfxde.jfx.application.XPlatform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;

public class CompilationWrapper extends StyleClassedTextAreaWrapper {

    private Supplier<CompletableFuture<List<Diagnostic<?>>>> supplier;
    private List<CompletableFuture<List<Diagnostic<?>>>> futures = new ArrayList<>();
    private ObservableList<Diagnostic<?>> diagnostics = FXCollections.observableArrayList();

    public CompilationWrapper(StyleClassedTextArea area, Supplier<CompletableFuture<List<Diagnostic<?>>>> supplier) {
        super(area);

        this.supplier = supplier;
        setListeners();
    }
    
    private void setListeners() {
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        
        Text helperHeightText = new Text("Yy");
        helperHeightText.setFont(textArea.getFont());
        helperHeightText.textProperty().bind(textArea.textProperty());
        helperHeightText.setLineSpacing(1);
        
        Tooltip tooltip = new Tooltip();
        tooltip.setGraphic(textArea);
        tooltip.setAutoHide(true);
        
        area.setMouseOverTextDelay(Duration.ofSeconds(1));
        area.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> {
            int i = e.getCharacterIndex();
            Diagnostic<?> diag = diagnostics.stream()
                    .filter(d -> {
                        var range = getDiagRange(d);
                        return range.getStart() <= i && i < range.getEnd();
                    })
                    .findFirst().orElse(null);            
            
            if (diag != null) {
                area.setMouseOverTextDelay(Duration.ofMillis(1));
                Point2D position = e.getScreenPosition();
                textArea.setText(diag.getCode() + "\n" + diag.getMessage(null));
                double prefTextHeight = helperHeightText.getBoundsInParent().getHeight() + 4;
                textArea.setPrefHeight(prefTextHeight);
                tooltip.show(area, position.getX(), position.getY());
            } else {
                tooltip.hide();
                area.setMouseOverTextDelay(Duration.ofSeconds(1));
            }
        });       
    }

    public ObservableList<Diagnostic<?>> getDiagnostics() {
        return diagnostics;
    }

    void compile() {
        futures.clear();
        var future = supplier.get();

        if (future != null) {
            futures.add(future);
        }
    }

    void showDiags() {
        
        if (futures.isEmpty()) {
            return;
        }
        
        CompletableFuture<List<Diagnostic<?>>> future = futures.get(0);

        future.thenAccept(diags -> XPlatform.runFX(() -> {
            
            diagnostics.clear();

            if (!futures.contains(future)) {
                return;
            }

            diags.forEach(d -> addStyle(getDiagRange(d), List.of("jd-" + d.getKind().name().toLowerCase())));
            diagnostics.addAll(diags);

        }));
    }

    private IndexRange getDiagRange(Diagnostic<?> diagnostic) {

        IndexRange range = null;
        int start = (int) Math.max(diagnostic.getStartPosition(), 0);;
        int end = (int) diagnostic.getEndPosition();
        int position = (int) diagnostic.getPosition();

        if (start < end) {
            range = new IndexRange(start, end);
        } else {

            if (position < getArea().getLength()) {

                String str = getArea().getText(position, position + 1);
                if (str.matches("\\s")) {
                    range = new IndexRange(Math.max(position - 1, 0), position);
                } else {
                    range = new IndexRange(position, position + 1);
                }
            } else {
                range = new IndexRange(position - 1, position);
            }
        }

        return range;
    }
}
