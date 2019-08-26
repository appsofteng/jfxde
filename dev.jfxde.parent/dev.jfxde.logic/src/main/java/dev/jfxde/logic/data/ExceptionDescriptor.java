package dev.jfxde.logic.data;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ExceptionDescriptor {

    private final Throwable throwable;
    private final ObjectProperty<LocalDateTime> timestamp = new SimpleObjectProperty<>();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty message = new SimpleStringProperty();
    private final StringProperty cause = new SimpleStringProperty();
    private final StringProperty causeMessage = new SimpleStringProperty();

    public ExceptionDescriptor(Throwable t) {
        this.throwable = t;
        this.timestamp.set(LocalDateTime.now());
        this.name.set(t.getClass().getName());
        this.message.set(t.getMessage());
        if (t.getCause() != null) {
            this.cause.set(t.getCause().getClass().getName());
            this.causeMessage.set(t.getCause().getMessage());
        }
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public ReadOnlyObjectProperty<LocalDateTime> timestampProperty() {
        return timestamp;
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public ReadOnlyStringProperty messageProperty() {
        return message;
    }

    public ReadOnlyStringProperty causeProperty() {
        return cause;
    }

    public ReadOnlyStringProperty causeMessageProperty() {
        return causeMessage;
    }

    public String getTraceStack() {
        
        StringWriter writer = new StringWriter();
        
        throwable.printStackTrace(new PrintWriter(writer));
        String stackTrace = writer.toString();
        
        return stackTrace;
    }
}
