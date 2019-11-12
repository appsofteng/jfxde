package dev.jfxde.logic.data;

import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.SimpleLongProperty;

public class FXBasicFileAttributes {

    private static final String TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private FXPath fxpath;
    private LongProperty size;
    private LongProperty creationTime;
    private LongProperty lastModifiedTime;

    public FXBasicFileAttributes(FXPath fxpath) {

        this.fxpath = fxpath;

        try {
            var fileAttributes = Files.getFileAttributeView(fxpath.getPath(), BasicFileAttributeView.class);
            setCreationTime(fileAttributes.readAttributes().creationTime().toMillis());
            setLastModifiedTime(fileAttributes.readAttributes().lastModifiedTime().toMillis());

            if (!fxpath.isDirectory()) {
                setSize(Files.size(fxpath.getPath()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setSize(long value) {
        getSizeProperty().set(value);
    }

    public ReadOnlyLongProperty sizeProperty() {
        return getSizeProperty();
    }

    private LongProperty getSizeProperty() {

        if (size == null) {
            size = new SimpleLongProperty() {
                @Override
                public Object getBean() {
                    return fxpath;
                }

                @Override
                public String toString() {
                    return fxpath.isDirectory() ? "" : NumberFormat.getInstance().format(Math.ceil(get() / 1024.0)) + " KiB";
                }
            };
        }

        return size;
    }

    private void setCreationTime(long value) {
        getCreationTimeProperty().set(value);
    }

    public ReadOnlyLongProperty creationTimeProperty() {
        return getCreationTimeProperty();
    }

    private LongProperty getCreationTimeProperty() {
        if (creationTime == null) {
            creationTime = new SimpleLongProperty() {

                @Override
                public Object getBean() {
                    return fxpath;
                }

                public String toString() {
                    return new SimpleDateFormat(TIME_FORMAT).format(get());
                };

            };
        }

        return creationTime;
    }

    long getLastModifiedTime() {
        return getLastModifiedTimeProperty().get();
    }

    void setLastModifiedTime(long value) {
        getLastModifiedTimeProperty().set(value);
    }

    public ReadOnlyLongProperty lastModifiedTimeProperty() {
        return getLastModifiedTimeProperty();
    }

    private LongProperty getLastModifiedTimeProperty() {
        if (lastModifiedTime == null) {
            lastModifiedTime = new SimpleLongProperty() {

                @Override
                public Object getBean() {
                    return fxpath;
                }

                public String toString() {
                    return new SimpleDateFormat(TIME_FORMAT).format(get());
                };

            };
        }

        return lastModifiedTime;
    }
}
