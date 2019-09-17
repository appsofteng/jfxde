package dev.jfxde.sysapps.settings;

import dev.jfxde.logic.SettingManager;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.PropertyDescriptor;
import dev.jfxde.ui.DesktopEnvironment;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TreeTableCell;
import javafx.scene.paint.Color;

public class SettingTableCell extends TreeTableCell<PropertyDescriptor, String> {

    private ChoiceBox<String> localeChoiceBox;
    private ColorPicker themeColorPicker;

    @Override
    public void startEdit() {
        super.startEdit();

        Node node = getNode();
        if (node != null) {
            setText(null);
            setGraphic(node);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setValue();
        setGraphic(null);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setStyle("");
        } else {
            if (isEditing()) {

                Node node = getNode();
                if (node != null) {
                    setText(null);
                    setGraphic(node);
                } else {
                    setValue();
                    setGraphic(null);
                }
            } else {
                setValue();
                setGraphic(null);
            }
        }
    }

    private void setValue() {

        setText(getItem());

        setStyle("");

        PropertyDescriptor propertyDescriptor = getTreeTableRow().getItem();
        if (propertyDescriptor != null) {

            if (propertyDescriptor.getKey().equals(SettingManager.SYSTEM_THEME_COLOR)) {

                setStyle("-fx-background-color: " + getItem() + ";");
                setText(null);
            }
        }
    }

    private Node getNode() {

        PropertyDescriptor propertyDescriptor = getTreeTableRow().getItem();
        if (propertyDescriptor == null) {
            return null;
        }

        Node node = null;

        if (propertyDescriptor.getKey().equals(SettingManager.SYSTEM_LOCALE)) {
            if (localeChoiceBox == null) {
                localeChoiceBox = new ChoiceBox<String>(Sys.am().getLocales());
                localeChoiceBox.setMaxWidth(Double.MAX_VALUE);
                localeChoiceBox.showingProperty().addListener(o -> {
                    if (!localeChoiceBox.isShowing()) {
                        commitEdit(localeChoiceBox.getSelectionModel().getSelectedItem());
                    }
                });
            }

            localeChoiceBox.getSelectionModel().select(getItem());

            node = localeChoiceBox;
        } else if (propertyDescriptor.getKey().equals(SettingManager.SYSTEM_THEME_COLOR)) {

            if (themeColorPicker == null) {
                themeColorPicker = new ColorPicker();
                themeColorPicker.setOnAction(e -> {
                    String color = themeColorPicker.getValue().toString().replace("0x", "#");
                    commitEdit(color);
                    ((DesktopEnvironment)getScene().getRoot()).setThemeColor(color);
                });

            }

            themeColorPicker.setValue(Color.web(Sys.sm().getThemeColor()));

            node = themeColorPicker;
        }

        return node;
    }

}
