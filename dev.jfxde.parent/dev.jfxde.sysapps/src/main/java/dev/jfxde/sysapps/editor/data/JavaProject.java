package dev.jfxde.sysapps.editor.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;


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

    @Override
    public void create(Path path) {
        try {
            Files.createDirectories(path.resolve(MAIN_JAVA));
            Files.createDirectories(path.resolve(MAIN_RESOURCES));
            Files.createDirectories(path.resolve(TEST_JAVA));
            Files.createDirectories(path.resolve(TEST_RESOURCES));
            Files.createDirectories(path.resolve(TARGET_CLASSES));
            Files.createDirectories(path.resolve(TARGET_TEST_CLASSES));
        } catch (IOException e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }
    }
}
