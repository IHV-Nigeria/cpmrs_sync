package org.openmrs.module.cdrsync.api.impl;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.impl.ObsServiceImpl;
import org.openmrs.module.cdrsync.api.CdrSyncObsService;
import org.openmrs.module.cdrsync.api.dao.CdrSyncObsDao;

import java.util.Date;
import java.util.List;

public class CdrSyncObsServiceImpl extends ObsServiceImpl implements CdrSyncObsService {
	
	private CdrSyncObsDao dao;
	
	public void setDao(CdrSyncObsDao dao) {
		this.dao = dao;
	}
	
	@Override
	public List<Obs> getObsByPatientAndLastSyncDate(Patient patient, Date lastSyncDate, Date endDate) {
		return dao.getObsByPatientAndLastSyncDate(patient, lastSyncDate, endDate);
	}
}
