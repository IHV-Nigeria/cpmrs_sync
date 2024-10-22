package org.openmrs.module.cdrsync.api.impl;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.impl.EncounterServiceImpl;
import org.openmrs.module.cdrsync.api.CdrSyncEncounterService;
import org.openmrs.module.cdrsync.api.dao.CdrSyncEncounterDao;

import java.util.Date;
import java.util.List;

public class CdrSyncEncounterServiceImpl extends EncounterServiceImpl implements CdrSyncEncounterService {
	
	private CdrSyncEncounterDao cdrSyncEncounterDao;
	
	public void setDao(CdrSyncEncounterDao cdrSyncEncounterDao) {
		this.cdrSyncEncounterDao = cdrSyncEncounterDao;
	}
	
	@Override
	public List<Encounter> getEncountersByEncounterDateTime(Date from, Date to) {
		return cdrSyncEncounterDao.getEncountersByEncounterDateTime(from, to);
	}
	
	@Override
	public List<Encounter> getEncountersByLastSyncDateAndPatient(Date from, Date to, Patient patient) {
		return cdrSyncEncounterDao.getEncountersByLastSyncDateAndPatient(from, to, patient);
	}
}
