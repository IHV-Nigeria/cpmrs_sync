package org.openmrs.module.cdrsync.api.dao;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.db.VisitDAO;

import java.util.Date;
import java.util.List;

public interface CdrSyncVisitDao extends VisitDAO {
	
	List<Visit> getVisitsByPatientAndDateChanged(Patient patient, Date from, Date to);
}
