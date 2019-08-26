package dev.jfxde.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "APP_PROVIDER")
@NamedQuery(name = "AppProvider.findByFqn", query = "SELECT a FROM AppProviderEntity a WHERE a.fqn = ?1")
public class AppProviderEntity extends DataEntity {

	private Long id;
	private String fqn;
	private boolean allowed;
	private String permissionChecksum = "";
	
	@Id
	@GeneratedValue(generator = "AppProviderSeq", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "AppProviderSeq", sequenceName = "app_provider_seq", allocationSize = 1)
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(unique = true)
	public String getFqn() {
		return fqn;
	}
	
	public void setFqn(String fqn) {
		this.fqn = fqn;
	}
	
	public boolean isAllowed() {
		return allowed;
	}
	
	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}
	
	@Column(name = "permission_checksum")
	public String getPermissionChecksum() {
		return permissionChecksum;
	}
	
	public void setPermissionChecksum(String permissionChecksum) {
		this.permissionChecksum = permissionChecksum;
	}
}
