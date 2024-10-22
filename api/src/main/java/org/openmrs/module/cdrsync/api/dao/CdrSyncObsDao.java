package org.openmrs.module.cdrsync.api.dao;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.db.ObsDAO;

import java.util.Date;
import java.util.List;

public interface CdrSyncObsDao extends ObsDAO {
	
	List<Obs> getObsByPatientAndLastSyncDate(Patient patient, Date lastSyncDate, Date endDate);
}
