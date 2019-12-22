package dev.jfxde.sysapps.editor.data;

import java.util.Locale;

import javax.tools.Diagnostic;

public class DiagnosticWrapper<S> implements Diagnostic<S> {

    private Diagnostic<?> diagnostic;
    private S source;
    
    public DiagnosticWrapper(Diagnostic<?> diagnostic, S source) {
        this.diagnostic = diagnostic;
        this.source = source;
    }

    @Override
    public Kind getKind() {
        return diagnostic.getKind();
    }

    @Override
    public S getSource() {
        return source;
    }

    @Override
    public long getPosition() {
        return diagnostic.getPosition();
    }

    @Override
    public long getStartPosition() {
        return diagnostic.getStartPosition();
    }

    @Override
    public long getEndPosition() {
        return diagnostic.getEndPosition();
    }

    @Override
    public long getLineNumber() {
        return diagnostic.getLineNumber();
    }

    @Override
    public long getColumnNumber() {
        return diagnostic.getColumnNumber();
    }

    @Override
    public String getCode() {
        return diagnostic.getCode();
    }

    @Override
    public String getMessage(Locale locale) {
        return diagnostic.getMessage(locale);
    }
}
