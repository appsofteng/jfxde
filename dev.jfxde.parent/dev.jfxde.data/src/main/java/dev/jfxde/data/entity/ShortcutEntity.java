package dev.jfxde.data.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "SHORTCUT")
public class ShortcutEntity extends DataEntity {

	private Long id;
    private String name;
    private String fqn;
    private String uri;
    private int position;
	private DesktopEntity desktop;

	@Id
	@GeneratedValue(generator = "ShortcutSeq", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "ShortcutSeq", sequenceName = "shortcut_seq", allocationSize = 1)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name = "desktop_id")
	public DesktopEntity getDesktop() {
		return desktop;
	}

	public void setDesktop(DesktopEntity desktop) {
		this.desktop = desktop;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFqn() {
		return fqn;
	}

	public void setFqn(String fqn) {
		this.fqn = fqn;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
}
