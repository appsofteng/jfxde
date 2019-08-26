package dev.jfxde.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.security.Permission;

@Repeatable(PermissionEntries.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PermissionEntry {
    
    Class<? extends Permission> type();
    String target() default "";
    String actions() default "";
}
