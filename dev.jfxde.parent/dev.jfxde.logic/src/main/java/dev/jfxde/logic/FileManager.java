package dev.jfxde.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import dev.jfxde.j.nio.file.WatchServiceRegister;
import  dev.jfxde.logic.Constants;
import dev.jfxde.logic.data.FXPath;

public final class FileManager extends Manager {

    private static final String HOME_DIR = System.getProperty("user.home") + "/." + Constants.SYS_NAME + "/" + Constants.SYS_VERSION + "/";
    private static final String USER_DIR = HOME_DIR + "users/default/";
    private static final String START_DIR = System.getProperty("user.dir") + "/";

    public static final Path HOME_APPS_DIR = Path.of(HOME_DIR + "apps");
    public static final Path DATA_DIR = Path.of(USER_DIR + "data");
    public static final Path USER_CONF_DIR = Path.of(USER_DIR + "conf");
    public static final Path DB_DIR = Path.of(USER_DIR + "data/db");
    public static final Path APP_DATA_DIR = Path.of(USER_DIR + "appdata");
    public static final Path USER_PREFS_FILE = Path.of(USER_CONF_DIR + "/preferences.properties");

    public static final Path SYSTEM_CONF_DIR = Path.of(HOME_DIR + "conf");
    public static final Path SYSTEM_PREFS_FILE = Path.of(SYSTEM_CONF_DIR + "/preferences.properties");

    public static final Path LOG_DIR = Path.of(HOME_DIR + Constants.LOG_DIR);

    public static final String DB_URL = "jdbc:h2:" + DB_DIR + "/db;TRACE_LEVEL_FILE=4";

    public static final Path MODULES_DIR = Path.of(START_DIR + "modules/");
    public static final Path APPS_DIR = Path.of(START_DIR  + "apps");
    public static final Path DEFAULT_PREFS_FILE = Path.of(START_DIR + "conf/preferences.properties");

    private static final String LOCK_DIR = HOME_DIR + "lock/";
    private static final String LOCK_FILE = LOCK_DIR + "lock.lck";
    private static final String MESSAGE_FILE = LOCK_DIR + "message";

    private final WatchServiceRegister watchServiceRegister = new WatchServiceRegister();
    private final FileLocker fileLocker = new FileLocker(Path.of(LOCK_FILE), Path.of(MESSAGE_FILE));

    private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());

    FileManager() {
	}

    @Override
    void init() throws Exception {
    	LOGGER.entering(FileManager.class.getName(), "init");
    	Files.createDirectories(HOME_APPS_DIR);
    	Files.createDirectories(APP_DATA_DIR);
    	Files.createDirectories(DATA_DIR);
    	Files.createDirectories(SYSTEM_CONF_DIR);
    	Files.createDirectories(USER_CONF_DIR);
    	Files.createDirectories(DB_DIR);
    	Files.createDirectories(Path.of(LOCK_DIR));
    	watchServiceRegister.start();
        fileLocker.lock();
        FXPath.setWatchServiceRegister(watchServiceRegister);
        LOGGER.exiting(FileManager.class.getName(), "init");
    }

    public void watch(Runnable messageHandler) throws IOException {
        fileLocker.watch(messageHandler, watchServiceRegister);
    }

    void stop() throws Exception {
        watchServiceRegister.stop();
    	fileLocker.stop();
    }
}
