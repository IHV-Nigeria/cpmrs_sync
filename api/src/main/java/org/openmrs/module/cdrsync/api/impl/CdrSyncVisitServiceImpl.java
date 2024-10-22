package org.openmrs.module.cdrsync.api.impl;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.impl.VisitServiceImpl;
import org.openmrs.module.cdrsync.api.CdrSyncVisitService;
import org.openmrs.module.cdrsync.api.dao.CdrSyncVisitDao;

import java.util.Date;
import java.util.List;

public class CdrSyncVisitServiceImpl extends VisitServiceImpl implements CdrSyncVisitService {
	
	private CdrSyncVisitDao cdrSyncVisitDao;
	
	public void setDao(CdrSyncVisitDao cdrSyncVisitDao) {
		this.cdrSyncVisitDao = cdrSyncVisitDao;
	}
	
	@Override
	public List<Visit> getVisitsByPatientAndDateChanged(Patient patient, Date from, Date to) {
		return cdrSyncVisitDao.getVisitsByPatientAndDateChanged(patient, from, to);
	}
}
