package dev.jfxde.logic.security;

public class CustomSecurityManager extends SecurityManager {

    @Override
    public void checkExit(int arg0) {
        throw new SecurityException("System exit not allowed.");
    }

}
