package dev.jfxde.logic.data;

import java.util.Map;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PropertyDescriptor implements Comparable<PropertyDescriptor> {
	
	private StringProperty key = new SimpleStringProperty();
	private StringProperty value = new SimpleStringProperty();
	

	public PropertyDescriptor(Map.Entry<Object, Object> entry) {
		key.set(entry.getKey().toString());
		value.set(entry.getValue().toString());
	}
	
	public ReadOnlyStringProperty keyProperty() {
		return key;
	}
	
	public ReadOnlyStringProperty valueProperty() {
		return value;
	}	
	
	@Override
	public int compareTo(PropertyDescriptor o) {
		return key.get().compareTo(o.key.get());
	}
}
