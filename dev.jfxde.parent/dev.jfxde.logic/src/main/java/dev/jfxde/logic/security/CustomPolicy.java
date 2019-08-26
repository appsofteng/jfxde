package dev.jfxde.logic.security;

import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Java\jdk-9.0.1\conf\security\java.security
 * policy.provider=sun.security.provider.PolicyFile
 */
public class CustomPolicy extends Policy {

    private final Policy defaultPolicy;
    private ConcurrentHashMap<CodeSource, PermissionCollection> permissionCollections = new ConcurrentHashMap<>();

    public CustomPolicy(Policy defaultPolicy) {
        this.defaultPolicy = defaultPolicy;
    }

    public void put(CodeSource codeSource, PermissionCollection collection) {
        permissionCollections.put(codeSource, collection);
    }

    void remove(CodeSource codeSource) {
        permissionCollections.remove(codeSource);
    }

    @Override
    public boolean implies(ProtectionDomain domain, Permission permission) {

        PermissionCollection collection = permissionCollections.getOrDefault(domain.getCodeSource(), defaultPolicy.getPermissions(domain));

        return collection.implies(permission);
    }
}
