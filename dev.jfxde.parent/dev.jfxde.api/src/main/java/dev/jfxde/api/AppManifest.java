package dev.jfxde.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AppManifest {

    String fqn();
    String name();
    String version();
    String altText() default "";
    String defaultLocale() default "en";
    AppScope scope() default AppScope.PROTOTYPE;
    String vendor();
    String website() default "";
    String[] uriSchemes() default {};
    String[] fileExtensions() default {};
}
