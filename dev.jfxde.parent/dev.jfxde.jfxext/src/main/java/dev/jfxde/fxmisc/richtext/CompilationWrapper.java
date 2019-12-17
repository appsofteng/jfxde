package dev.jfxde.fxmisc.richtext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.tools.Diagnostic;

import org.fxmisc.richtext.StyleClassedTextArea;

import dev.jfxde.jfx.application.XPlatform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexRange;

public class CompilationWrapper extends StyleClassedTextAreaWrapper {

    private Supplier<CompletableFuture<List<Diagnostic<?>>>> supplier;
    private List<CompletableFuture<List<Diagnostic<?>>>> futures = new ArrayList<>();
    private ObservableList<Diagnostic<?>> diagnoctics = FXCollections.observableArrayList();

    public CompilationWrapper(StyleClassedTextArea area, Supplier<CompletableFuture<List<Diagnostic<?>>>> supplier) {
        super(area);

        this.supplier = supplier;
    }

    public ObservableList<Diagnostic<?>> getDiagnoctics() {
        return diagnoctics;
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

            if (!futures.contains(future)) {
                return;
            }

            diags.forEach(d -> addStyle(getDiagRange(d), List.of("jd-" + d.getKind().name().toLowerCase())));

            diagnoctics.setAll(diags);
        }));
    }

    private IndexRange getDiagRange(Diagnostic<?> diagnostic) {

        IndexRange range = null;
        int start = (int) diagnostic.getStartPosition();
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
