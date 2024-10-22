package org.openmrs.module.cdrsync.api.dao;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.db.EncounterDAO;

import java.util.Date;
import java.util.List;

public interface CdrSyncEncounterDao extends EncounterDAO {
	
	List<Encounter> getEncountersByEncounterDateTime(Date from, Date to);
	
	List<Encounter> getEncountersByLastSyncDateAndPatient(Date from, Date to, Patient patient);
}
