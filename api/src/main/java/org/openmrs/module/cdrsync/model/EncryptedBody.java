package org.openmrs.module.cdrsync.model;

public class EncryptedBody {
	
	private String encryptedContainer;
	
	public EncryptedBody(String encryptedJson) {
		this.encryptedContainer = encryptedJson;
	}
	
	public EncryptedBody() {
	}
	
	public String getEncryptedContainer() {
		return encryptedContainer;
	}
	
	public void setEncryptedContainer(String encryptedContainer) {
		this.encryptedContainer = encryptedContainer;
	}
}
