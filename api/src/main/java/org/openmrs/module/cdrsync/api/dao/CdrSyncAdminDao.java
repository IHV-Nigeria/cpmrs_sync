package org.openmrs.module.cdrsync.api.dao;

import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.module.cdrsync.model.CdrSyncBatch;

import java.util.List;

public interface CdrSyncAdminDao extends AdministrationDAO {
	
	void updateLastSyncGlobalProperty(String propertyName, String propertyValue);
	
	void saveCdrSyncBatch(CdrSyncBatch cdrSyncBatch);
	
	void updateCdrSyncBatchStatus(int batchId, String status, int patientsProcessed, boolean done);
	
	CdrSyncBatch getCdrSyncBatchByStatusAndOwner(String status, String owner, String syncType);
	
	List<CdrSyncBatch> getRecentSyncBatches();
}
