//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.06.06 at 02:19:42 AM WAT 
//

package org.openmrs.module.cdrsync.container.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class ObsType implements Serializable {
	
	private String obsUuid;
	
	private int obsId;
	
	private int personId;
	
	private int conceptId;
	
	private int encounterId;
	
	private int formId;
	
	private String pmmForm;
	
	private int encounterType;
	
	private Date obsDatetime;
	
	private int locationId;
	
	private int obsGroupId;
	
	private int valueCoded;
	
	private Date valueDatetime;
	
	private BigDecimal valueNumeric;
	
	private String valueText;
	
	private int creator;
	
	private Date dateCreated;
	
	private int voided;
	
	private int voidedBy;
	
	private Date dateVoided;
	
	private String variableName;
	
	private String variableValue;
	
	private String datimId;
	
	private String patientUuid;
	
	private String encounterUuid;
	
	private String visitUuid;
	
	private int datatype;
	
	public String getObsUuid() {
		return obsUuid;
	}
	
	public void setObsUuid(String obsUuid) {
		this.obsUuid = obsUuid;
	}
	
	public int getObsId() {
		return obsId;
	}
	
	public void setObsId(int obsId) {
		this.obsId = obsId;
	}
	
	public int getPersonId() {
		return personId;
	}
	
	public void setPersonId(int personId) {
		this.personId = personId;
	}
	
	public int getConceptId() {
		return conceptId;
	}
	
	public void setConceptId(int conceptId) {
		this.conceptId = conceptId;
	}
	
	public int getEncounterId() {
		return encounterId;
	}
	
	public void setEncounterId(int encounterId) {
		this.encounterId = encounterId;
	}
	
	public int getFormId() {
		return formId;
	}
	
	public void setFormId(int formId) {
		this.formId = formId;
	}
	
	public String getPmmForm() {
		return pmmForm;
	}
	
	public void setPmmForm(String pmmForm) {
		this.pmmForm = pmmForm;
	}
	
	public int getEncounterType() {
		return encounterType;
	}
	
	public void setEncounterType(int encounterType) {
		this.encounterType = encounterType;
	}
	
	public Date getObsDatetime() {
		return obsDatetime;
	}
	
	public void setObsDatetime(Date obsDatetime) {
		this.obsDatetime = obsDatetime;
	}
	
	public int getLocationId() {
		return locationId;
	}
	
	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}
	
	public int getObsGroupId() {
		return obsGroupId;
	}
	
	public void setObsGroupId(int obsGroupId) {
		this.obsGroupId = obsGroupId;
	}
	
	public int getValueCoded() {
		return valueCoded;
	}
	
	public void setValueCoded(int valueCoded) {
		this.valueCoded = valueCoded;
	}
	
	public Date getValueDatetime() {
		return valueDatetime;
	}
	
	public void setValueDatetime(Date valueDatetime) {
		this.valueDatetime = valueDatetime;
	}
	
	public BigDecimal getValueNumeric() {
		return valueNumeric;
	}
	
	public void setValueNumeric(BigDecimal valueNumeric) {
		this.valueNumeric = valueNumeric;
	}
	
	public String getValueText() {
		return valueText;
	}
	
	public void setValueText(String valueText) {
		this.valueText = valueText;
	}
	
	public int getCreator() {
		return creator;
	}
	
	public void setCreator(int creator) {
		this.creator = creator;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public int getVoided() {
		return voided;
	}
	
	public void setVoided(int voided) {
		this.voided = voided;
	}
	
	public int getVoidedBy() {
		return voidedBy;
	}
	
	public void setVoidedBy(int voidedBy) {
		this.voidedBy = voidedBy;
	}
	
	public Date getDateVoided() {
		return dateVoided;
	}
	
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}
	
	public String getVariableName() {
		return variableName;
	}
	
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	
	public String getVariableValue() {
		return variableValue;
	}
	
	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}
	
	public String getDatimId() {
		return datimId;
	}
	
	public void setDatimId(String datimId) {
		this.datimId = datimId;
	}
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public String getEncounterUuid() {
		return encounterUuid;
	}
	
	public void setEncounterUuid(String encounterUuid) {
		this.encounterUuid = encounterUuid;
	}
	
	public String getVisitUuid() {
		return visitUuid;
	}
	
	public void setVisitUuid(String visitUuid) {
		this.visitUuid = visitUuid;
	}
	
	public int getDatatype() {
		return datatype;
	}
	
	public void setDatatype(int datatype) {
		this.datatype = datatype;
	}
}
