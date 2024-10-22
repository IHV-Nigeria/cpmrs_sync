package org.openmrs.module.cdrsync.api.impl;

import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.impl.PatientServiceImpl;
import org.openmrs.module.cdrsync.api.CdrSyncPatientService;
import org.openmrs.module.cdrsync.api.dao.CdrSyncPatientDao;
import org.openmrs.module.cdrsync.model.DatimMap;

import java.util.Date;
import java.util.List;

public class CdrSyncPatientServiceImpl extends PatientServiceImpl implements CdrSyncPatientService {
	
	private CdrSyncPatientDao dao;
	
	public void setDao(CdrSyncPatientDao dao) {
		this.dao = dao;
	}
	
	@Override
	public Long getPatientsCount(boolean includeVoided) throws DAOException {
		return dao.getPatientsCount(includeVoided);
	}
	
	@Override
	public List<Patient> getPatients(Integer start, Integer length, boolean includeVoided) throws DAOException {
		return dao.getPatients(start, length, includeVoided);
	}
	
	@Override
	public List<Integer> getPatientIds(Integer start, Integer length, boolean includeVoided) throws DAOException {
		return dao.getPatientIds(start, length, includeVoided);
	}
	
	@Override
	public List<Integer> getPatientIds(boolean includeVoided) throws DAOException {
		return dao.getPatientIds(includeVoided);
	}
	
	@Override
	public List<Integer> getPatientsByLastSyncDate(Date from, Date to, List<String> patientIds, boolean includeVoided,
	        Integer start, Integer length) throws DAOException {
		return dao.getPatientsByLastSyncDate(from, to, patientIds, includeVoided, start, length);
	}
	
	@Override
	public Long getPatientCountFromLastSyncDate(Date from, Date to, List<String> patientIds, boolean includeVoided)
	        throws DAOException {
		return dao.getPatientCountFromLastSyncDate(from, to, patientIds, includeVoided);
	}
	
	@Override
	public DatimMap getDatimMapByDatimCode(String datimCode) throws DAOException {
		return dao.getDatimMapByDatimCode(datimCode);
	}
}
