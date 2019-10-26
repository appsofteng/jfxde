package dev.jfxde.data.dao;

import java.io.File;

import dev.jfxde.data.entity.DataRoot;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

public class StorageManager {

    private EmbeddedStorageManager storageManager;

    public StorageManager(DataRoot dataRoot, File dir) {
        storageManager = EmbeddedStorage.start(dataRoot, dir);
    }

    public void storeRoot() {
        storageManager.storeRoot();
    }

    public void store(Object instance) {
        storageManager.store(instance);
    }

    public void shutdown() {
        storageManager.shutdown();
    }
}
