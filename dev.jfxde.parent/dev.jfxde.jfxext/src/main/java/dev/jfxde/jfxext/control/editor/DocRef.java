package dev.jfxde.jfxext.control.editor;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class DocRef {

    protected final String docCode;
    protected final String signature;
    protected Function<DocRef, String> documentation;

    public DocRef(String docCode) {
        this(docCode, docCode, dr -> "");
    }

    public DocRef(String docCode, Function<DocRef, String> documentation) {
        this(docCode, docCode, documentation);
    }

    public DocRef(String docCode, String signature) {
        this(docCode, signature, dr -> "");
    }

    public DocRef(String docCode, String signature, Function<DocRef, String> documentation) {
        this.docCode = docCode;
        this.signature = signature;
        this.documentation = documentation;
    }

    public String getDocCode() {
        return docCode;
    }

    public String getSignature() {
        return signature;
    }

    public String getDocumentation() {
        return documentation.apply(this);
    }

    public boolean isUrl() {
        return getDocCode().matches("https?://.*");
    }

    @Override
    public String toString() {
        return new TreeMap<>(Map.of("docCode", docCode, "signature", signature)).toString();
    }
}
