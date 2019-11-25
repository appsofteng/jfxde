package dev.jfxde.logic.context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import dev.jfxde.api.FileController;
import dev.jfxde.logic.data.AppDescriptor;
import dev.jfxde.logic.data.AppProviderDescriptor;

public class FileControllerImpl implements FileController {

	private AppDescriptor appDescriptor;

    public FileControllerImpl(AppDescriptor appDescriptor) {
		this.appDescriptor = appDescriptor;
	}

	@Override
    public Path getAppDataDir(String... subDir) {
        AppProviderDescriptor descriptor = appDescriptor.getAppProviderDescriptor();
        Path dataDir = Path.of(descriptor.getAppDataDir(), subDir);

        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return dataDir;
    }
}
