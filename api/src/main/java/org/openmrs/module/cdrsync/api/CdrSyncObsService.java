package org.openmrs.module.cdrsync.api;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;

import java.util.Date;
import java.util.List;

public interface CdrSyncObsService extends ObsService {
	
	List<Obs> getObsByPatientAndLastSyncDate(Patient patient, Date lastSyncDate, Date endDate);
}
