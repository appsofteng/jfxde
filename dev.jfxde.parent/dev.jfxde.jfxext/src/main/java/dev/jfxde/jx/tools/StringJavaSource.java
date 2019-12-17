package dev.jfxde.jx.tools;

import java.net.URI;
import java.nio.file.Path;

import javax.tools.SimpleJavaFileObject;

public class StringJavaSource extends SimpleJavaFileObject {

    private final String code;

    public StringJavaSource(String name, String code) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
    }
    
    public StringJavaSource(Path name, String code) {
        super(URI.create("string:///" + name.toString()), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
}
