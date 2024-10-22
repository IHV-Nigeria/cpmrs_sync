package org.openmrs.module.cdrsync.api.impl;

import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.impl.ProgramWorkflowServiceImpl;
import org.openmrs.module.cdrsync.api.CdrSyncPatientProgramService;
import org.openmrs.module.cdrsync.api.dao.CdrSyncPatientProgramDao;

import java.util.Date;
import java.util.List;

public class CdrSyncPatientProgramServiceImpl extends ProgramWorkflowServiceImpl implements CdrSyncPatientProgramService {
	
	private CdrSyncPatientProgramDao dao;
	
	public void setDao(CdrSyncPatientProgramDao dao) {
		this.dao = dao;
	}
	
	@Override
	public List<PatientProgram> getPatientProgramsByPatientAndLastSyncDate(Patient patient, Date startDate, Date endDate) {
		return dao.getPatientProgramsByPatientAndLastSyncDate(patient, startDate, endDate);
	}
}
