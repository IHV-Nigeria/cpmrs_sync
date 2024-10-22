package org.openmrs.module.cdrsync.api.dao;

import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.db.ProgramWorkflowDAO;

import java.util.Date;
import java.util.List;

public interface CdrSyncPatientProgramDao extends ProgramWorkflowDAO {
	
	List<PatientProgram> getPatientProgramsByPatientAndLastSyncDate(Patient patient, Date startDate, Date endDate);
}
