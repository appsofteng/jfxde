package dev.jfxde.logic.data;

import javafx.beans.property.SimpleLongProperty;

public class DataObj {
	
	private SimpleLongProperty id = new SimpleLongProperty();
	private boolean fetched;
	
	
	public SimpleLongProperty idProperty() {
		return id;
	}
	
	public Long getId() {
		return id.get();
	}
	
	public void setId(Long id) {
		this.id.set(id);
	}
	
	public boolean isFetched() {
		return fetched;
	}
	
	public void setFetched(boolean fetched) {
		this.fetched = fetched;
	}
}
