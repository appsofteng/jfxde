package dev.jfxde.sysapps.jshell;

public abstract class Processor {

    protected Session session;

    Processor(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    abstract void process(String input);

}
