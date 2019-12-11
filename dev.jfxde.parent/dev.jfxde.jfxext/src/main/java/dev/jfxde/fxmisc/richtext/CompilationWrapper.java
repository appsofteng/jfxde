package dev.jfxde.fxmisc.richtext;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.tools.Diagnostic;

import org.fxmisc.richtext.StyleClassedTextArea;

import dev.jfxde.jfx.application.XPlatform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CompilationWrapper extends StyleClassedTextAreaWrapper {

    private Supplier<CompletableFuture<List<Diagnostic<?>>>> supplier;
    private CompletableFuture<List<Diagnostic<?>>> future;
    private ObservableList<Diagnostic<?>> diags = FXCollections.observableArrayList();

    public CompilationWrapper(StyleClassedTextArea area, Supplier<CompletableFuture<List<Diagnostic<?>>>> supplier) {
        super(area);

        this.supplier = supplier;
    }

    void compile() {
        future = supplier.get();
    }

    void showDiags() {
        future.thenAccept(d -> XPlatform.runFX(() -> {
            d.forEach(da -> addStyle((int)da.getStartPosition(), (int)da.getEndPosition(), List.of("jd-diag-error")));
            diags.setAll(d);
        }));
    }
}
