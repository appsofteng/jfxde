package dev.jfxde.api;

import java.nio.file.Path;

public interface FileController {

	Path getAppDataDir(String... subDir);
}
