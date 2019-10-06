package dev.jfxde.jfxext.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javafx.concurrent.Task;

public abstract class CTask<V> extends Task<V> {


    public static CTask<Void> create(TRunnable task) {
        CTask<Void> t = new CTask<>() {

            @Override
            protected Void call() throws Exception {
                task.run();
                return null;
            }
        };

        t.setOnFailed(e -> {
            throw new RuntimeException(e.getSource().getException());
        });

        return t;
    }

    public static <T> CTask<T> create(Callable<T> task) {
        CTask<T> t = new CTask<>() {

            @Override
            protected T call() throws Exception {
                return task.call();
            }
        };

        t.setOnFailed(e -> {
            throw new RuntimeException(e.getSource().getException());
        });

        return t;
    }

    public CTask<V> onFinished(Consumer<CTask<V>> value) {

        setOnSucceeded(e -> value.accept(this));
        setOnCancelled(e -> value.accept(this));
        setOnFailed(e -> value.accept(this));

        return this;
    }

    public CTask<V> onSucceeded(Consumer<V> value) {

        setOnSucceeded(e -> value.accept(getValue()));
        return this;
    }

    public static interface TRunnable {
        void run() throws Exception;
    }
}
