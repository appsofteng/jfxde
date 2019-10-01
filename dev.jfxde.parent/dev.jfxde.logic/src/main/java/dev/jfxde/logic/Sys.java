package dev.jfxde.logic;

import java.net.URI;
import java.security.Policy;
import java.security.URIParameter;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import dev.jfxde.logic.security.CustomPolicy;
import dev.jfxde.logic.security.CustomSecurityManager;

public final class Sys {

    private static final Sys INSTANCE = new Sys();
    private ResourceManager resourceManager;
    private final ExceptionManager exceptionManager = new ExceptionManager();
    private final AppManager appManager = new AppManager();
    private final FileManager fileManager = new FileManager();
    private final DataManager dataManager = new DataManager();
    private final TaskManager taskManager = new TaskManager();
    private final PreferencesManager preferencesManager = new PreferencesManager();
    private final ConsoleManager consoleManager = new ConsoleManager();
    private List<Manager> startSequence;

    private static final Logger LOGGER = Logger.getLogger(Sys.class.getName());

    private Sys() {
    }

    public static Sys get() {
        return INSTANCE;
    }

    public static ResourceManager rm() {
        return INSTANCE.resourceManager;
    }

    public static ExceptionManager em() {
        return INSTANCE.exceptionManager;
    }

    public static AppManager am() {
        return INSTANCE.appManager;
    }

    public static FileManager fm() {
        return INSTANCE.fileManager;
    }

    public static DataManager dm() {

        return INSTANCE.dataManager;
    }

    public static TaskManager tm() {
        return INSTANCE.taskManager;
    }

    public static PreferencesManager pm() {
        return INSTANCE.preferencesManager;
    }

    public static ConsoleManager cm() {
        return INSTANCE.consoleManager;
    }

    public void init(Class<?> resourceCaller, Consumer<Double> progress) throws Exception {
        LOGGER.entering(Sys.class.getName(), "init");

        setSecurityPolicy();
        resourceManager = new ResourceManager(resourceCaller);

        startSequence = List.of(exceptionManager, fileManager, consoleManager, preferencesManager, resourceManager, dataManager, taskManager, appManager);

        double progressStep = 1.0 / startSequence.size();
        double accum = 0;
        for (Manager m : startSequence) {
            m.init();
            accum += progressStep;
            progress.accept(accum);
        }

        LOGGER.exiting(Sys.class.getName(), "init");
    }

    public void stop() throws Exception {
        for (int i = startSequence.size() - 1; i >= 0; i--) {
            startSequence.get(i).stop();
        }
    }

    private void setSecurityPolicy() throws Exception {

        URI uri = getClass().getResource("conf/security/java.policy").toURI();
        Policy defaultPolicy = Policy.getInstance("JavaPolicy", new URIParameter(uri));
        CustomPolicy customPolicy = new CustomPolicy(defaultPolicy);
        Policy.setPolicy(customPolicy);
        System.setSecurityManager(new CustomSecurityManager());
    }
}
