package dev.jfxde.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import  dev.jfxde.logic.Constants;

public final class FileManager extends Manager {

    private static final String HOME_DIR = System.getProperty("user.home") + "/." + Constants.SYS_NAME + "/" + Constants.SYS_VERSION + "/";
    private static final String USER_DIR = System.getProperty("user.dir") + "/";

    public static final Path APPS_DIR = Paths.get(HOME_DIR + "apps");
    public static final Path DATA_DIR = Paths.get(HOME_DIR + "data");
    public static final Path CONF_DIR = Paths.get(HOME_DIR + "conf");
    public static final Path DB_DIR = Paths.get(HOME_DIR + "data/db");
    public static final Path APP_DATA_DIR = Paths.get(HOME_DIR + "appdata");
    public static final Path CONF_FILE = Paths.get(CONF_DIR + "/settings.properties");

    public static final Path LOG_DIR = Paths.get(HOME_DIR + "/log");

    public static final String DB_URL = "jdbc:h2:" + DB_DIR + "/db;TRACE_LEVEL_FILE=4";

    public static final Path MODULES_DIR = Paths.get(USER_DIR + "modules/");
    public static final Path DEFAULT_CONF_FILE = Paths.get(USER_DIR + "conf/settings.properties");

    private static final String LOCK_DIR = HOME_DIR + "lock/";
    private static final String LOCK_FILE = LOCK_DIR + "lock.lck";
    private static final String MESSAGE_FILE = LOCK_DIR + "message";
    private final FileLocker fileLocker = new FileLocker(Paths.get(LOCK_FILE), Paths.get(MESSAGE_FILE));

    private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());

    FileManager() {
	}

    @Override
    void init() throws Exception {
    	LOGGER.entering(FileManager.class.getName(), "init");
    	Files.createDirectories(APP_DATA_DIR);
    	Files.createDirectories(DATA_DIR);
    	Files.createDirectories(CONF_DIR);
    	Files.createDirectories(DB_DIR);
    	Files.createDirectories(Paths.get(LOCK_DIR));
        fileLocker.lock();
        LOGGER.exiting(FileManager.class.getName(), "init");
    }

    public void watch(Runnable messageHandler) throws IOException {
        fileLocker.watch(messageHandler);
    }

    void stop() throws Exception {
    	fileLocker.stop();
    }
}
