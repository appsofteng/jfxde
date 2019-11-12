package dev.jfxde.j.util;

import java.io.Closeable;
import java.util.concurrent.Callable;

public abstract class LU {

    private LU() {
    }

    public static <T> T of(Callable<T> lambda) {
        T result = null;
        try {
            result = lambda.call();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return result;
    }

    public static void of(Closeable lambda) {
        try {
            lambda.close();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
