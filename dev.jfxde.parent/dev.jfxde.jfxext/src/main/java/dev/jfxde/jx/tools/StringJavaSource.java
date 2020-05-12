package dev.jfxde.jx.tools;

import java.nio.file.Path;

import javax.tools.SimpleJavaFileObject;

public class StringJavaSource extends SimpleJavaFileObject {

    private final String code;

    public StringJavaSource(Path path, String code) {
        super(path.toUri(), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
}
