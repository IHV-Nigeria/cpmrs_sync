package org.openmrs.module.cdrsync.api;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.VisitService;
import org.openmrs.module.cdrsync.CdrsyncConfig;

import java.util.Date;
import java.util.List;

public interface CdrSyncVisitService extends VisitService {
	
	@Authorized(CdrsyncConfig.MODULE_PRIVILEGE)
	List<Visit> getVisitsByPatientAndDateChanged(Patient patient, Date from, Date to);
}
