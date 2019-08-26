package dev.jfxde.data.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "DESKTOP")
@NamedQuery(name = "Desktop.fetch", query = "SELECT d FROM DesktopEntity d JOIN FETCH d.shortcuts WHERE d.id = ?1")
public class DesktopEntity extends DataEntity {
	
	private Long id;
	private boolean active;
    private List<ShortcutEntity> shortcuts = new ArrayList<>();
	
    public DesktopEntity() {
	}
    
	public DesktopEntity(Long id) {
		this.id = id;
	}

	@Id
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	@OneToMany(mappedBy = "desktop", fetch = FetchType.LAZY)
	public List<ShortcutEntity> getShortcuts() {
		return shortcuts;
	}
	
	public void setShortcuts(List<ShortcutEntity> shortcuts) {
		this.shortcuts = shortcuts;
	}
}
