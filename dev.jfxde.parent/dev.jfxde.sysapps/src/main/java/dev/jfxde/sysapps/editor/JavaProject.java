package dev.jfxde.sysapps.editor;

import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.jfxde.logic.data.FXPath;


class JavaProject extends Project {

    private static final Logger LOGGER = Logger.getLogger(JavaProject.class.getName());

    private static final String MAIN = "src/main";
    private static final String MAIN_JAVA = MAIN + "/java";
    private static final String MAIN_RESOURCES = MAIN + "/resources";
    private static final String TEST = "src/test";
    private static final String TEST_JAVA = TEST + "/java";
    private static final String TEST_RESOURCES = TEST + "/resources";
    private static final String TARGET_CLASSES = "target/classes";
    private static final String TARGET_TEST_CLASSES = "target/test-classes";

    private FXPath path;

    public JavaProject(FXPath path) {

        this.path = path;
    }

    @Override
    void create() {
        try {
            Files.createDirectories(path.getPath().resolve(MAIN_JAVA));
            Files.createDirectories(path.getPath().resolve(MAIN_RESOURCES));
            Files.createDirectories(path.getPath().resolve(TEST_JAVA));
            Files.createDirectories(path.getPath().resolve(TEST_RESOURCES));
            Files.createDirectories(path.getPath().resolve(TARGET_CLASSES));
            Files.createDirectories(path.getPath().resolve(TARGET_TEST_CLASSES));
        } catch (IOException e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }
    }
}
