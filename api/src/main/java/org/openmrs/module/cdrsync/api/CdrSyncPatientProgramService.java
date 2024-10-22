package org.openmrs.module.cdrsync.api;

import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.ProgramWorkflowService;

import java.util.Date;
import java.util.List;

public interface CdrSyncPatientProgramService extends ProgramWorkflowService {
	
	List<PatientProgram> getPatientProgramsByPatientAndLastSyncDate(Patient patient, Date startDate, Date endDate);
}
