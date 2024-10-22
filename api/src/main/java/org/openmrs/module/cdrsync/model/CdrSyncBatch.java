package org.openmrs.module.cdrsync.model;

import org.openmrs.BaseOpenmrsMetadata;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "cdrsync.CdrSyncBatch")
@Table(name = "cdr_sync_batch")
public class CdrSyncBatch {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cdr_sync_batch_id")
	private Integer id;
	
	@Column(name = "owner_username")
	private String ownerUsername;
	
	@Column(name = "total_number_of_patients_processed")
	private Integer patientsProcessed;
	
	@Column(name = "total_number_of_patients")
	private Integer patients;
	
	@Column(name = "date_started")
	private Date dateStarted;
	
	//	@Column(name = "date_created")
	//	private Date dateCreated;
	
	@Column(name = "date_completed")
	private Date dateCompleted;
	
	//	@Column(name = "date_updated")
	//	private Date dateUpdated;
	
	@Column(name = "status")
	private String status;
	
	//	@Column(name = "name")
	//	private String name;
	
	@Column(name = "sync_type")
	private String syncType;
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getOwnerUsername() {
		return ownerUsername;
	}
	
	public void setOwnerUsername(String ownerUsername) {
		this.ownerUsername = ownerUsername;
	}
	
	public Integer getPatientsProcessed() {
		return patientsProcessed;
	}
	
	public void setPatientsProcessed(Integer patientsProcessed) {
		this.patientsProcessed = patientsProcessed;
	}
	
	public Integer getPatients() {
		return patients;
	}
	
	public void setPatients(Integer patients) {
		this.patients = patients;
	}
	
	public Date getDateStarted() {
		return dateStarted;
	}
	
	public void setDateStarted(Date dateStarted) {
		this.dateStarted = dateStarted;
	}
	
	//	public Date getDateCreated() {
	//		return dateCreated;
	//	}
	//
	//	public void setDateCreated(Date dateCreated) {
	//		this.dateCreated = dateCreated;
	//	}
	
	public Date getDateCompleted() {
		return dateCompleted;
	}
	
	public void setDateCompleted(Date dateCompleted) {
		this.dateCompleted = dateCompleted;
	}
	
	//	public Date getDateUpdated() {
	//		return dateUpdated;
	//	}
	//
	//	public void setDateUpdated(Date dateUpdated) {
	//		this.dateUpdated = dateUpdated;
	//	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	//	public String getName() {
	//		return name;
	//	}
	//
	//	public void setName(String name) {
	//		this.name = name;
	//	}
	
	public String getSyncType() {
		return syncType;
	}
	
	public void setSyncType(String syncType) {
		this.syncType = syncType;
	}
}
