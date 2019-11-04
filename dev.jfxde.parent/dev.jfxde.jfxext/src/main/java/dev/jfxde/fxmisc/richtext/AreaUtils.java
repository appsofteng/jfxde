package dev.jfxde.fxmisc.richtext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.fxmisc.richtext.GenericStyledArea;

import dev.jfxde.j.util.LU;
import dev.jfxde.jfx.application.XPlatform;

public final class AreaUtils {

    private AreaUtils() {
    }

    public static void readText(GenericStyledArea<?, ?, ?> area, Path path) {

        CompletableFuture.supplyAsync(() -> LU.of(() -> Files.readString(path)))
                .thenAccept(s -> XPlatform.runFX(() -> {
                    area.replaceText(0, 0, s);
                    area.getUndoManager().forgetHistory();
                    area.requestFocus();
                    area.moveTo(0);
                    area.requestFollowCaret();
                }));

    }

    public static void writeText(GenericStyledArea<?, ?, ?> area, Path path, Runnable runnable) {

        CompletableFuture.runAsync(() -> LU.of(() -> Files.writeString(path, area.getText())))
                .thenRun(() -> XPlatform.runFX(runnable));

    }
}
