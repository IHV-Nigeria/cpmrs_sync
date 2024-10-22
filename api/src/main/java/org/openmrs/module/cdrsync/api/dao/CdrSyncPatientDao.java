package org.openmrs.module.cdrsync.api.dao;

import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.module.cdrsync.model.DatimMap;

import java.util.Date;
import java.util.List;

public interface CdrSyncPatientDao extends PatientDAO {
	
	List<Patient> getPatients(Integer start, Integer length, boolean includeVoided) throws DAOException;
	
	Long getPatientsCount(boolean includeVoided) throws DAOException;
	
	List<Integer> getPatientIds(Integer start, Integer length, boolean includeVoided) throws DAOException;
	
	List<Integer> getPatientIds(boolean includeVoided) throws DAOException;
	
	List<Integer> getPatientsByLastSyncDate(Date from, Date to, List<String> patientIds, boolean includeVoided,
	        Integer start, Integer length) throws DAOException;
	
	Long getPatientCountFromLastSyncDate(Date from, Date to, List<String> patientIds, boolean includeVoided)
	        throws DAOException;
	
	DatimMap getDatimMapByDatimCode(String datimCode) throws DAOException;
}
