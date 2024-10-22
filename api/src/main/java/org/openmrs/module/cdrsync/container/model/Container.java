/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.cdrsync.container.model;

/**
 *
 * @author Innocent
 */

import java.io.Serializable;

public class Container implements Serializable {
	
	String id;
	
	private MessageHeaderType messageHeader;
	
	private MessageDataType messageData;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public MessageHeaderType getMessageHeader() {
		return messageHeader;
	}
	
	public void setMessageHeader(MessageHeaderType messageHeader) {
		this.messageHeader = messageHeader;
	}
	
	public MessageDataType getMessageData() {
		return messageData;
	}
	
	public void setMessageData(MessageDataType messageData) {
		this.messageData = messageData;
	}
}
