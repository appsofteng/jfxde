package dev.jfxde.logic;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class PrivilegedForkJoinWorkerThread extends ForkJoinWorkerThread {

    protected PrivilegedForkJoinWorkerThread(ForkJoinPool pool) {
        super(pool);
    }
}
