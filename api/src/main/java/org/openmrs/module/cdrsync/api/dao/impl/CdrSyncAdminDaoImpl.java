package org.openmrs.module.cdrsync.api.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.db.hibernate.HibernateAdministrationDAO;
import org.openmrs.module.cdrsync.api.dao.CdrSyncAdminDao;
import org.openmrs.module.cdrsync.model.CdrSyncBatch;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class CdrSyncAdminDaoImpl extends HibernateAdministrationDAO implements CdrSyncAdminDao {
	
	private final Logger log = Logger.getLogger(this.getClass().getName());
	
	DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public void updateLastSyncGlobalProperty(String propertyName, String propertyValue) {
		Query query = sessionFactory.getCurrentSession()
		        .createQuery("UPDATE GlobalProperty SET propertyValue = :propertyValue WHERE property = :propertyName")
		        .setParameter("propertyName", propertyName).setParameter("propertyValue", propertyValue);
		int s = query.executeUpdate();
		log.info("Finished updating::" + s);
	}
	
	@Override
	public void saveCdrSyncBatch(CdrSyncBatch cdrSyncBatch) {
		//		this.sessionFactory.getCurrentSession().saveOrUpdate(cdrSyncBatch);
		String query = "insert into cdr_sync_batch (status, owner_username, sync_type, "
		        + "total_number_of_patients_processed, total_number_of_patients, date_started, date_completed) values (:status, :ownerUsername, "
		        + ":syncType, :totalNumberOfPatientsProcessed, :totalNumberOfPatients, :dateStarted, :dateCompleted)";
		Query q = sessionFactory.getCurrentSession().createSQLQuery(query);
		q.setParameter("status", cdrSyncBatch.getStatus());
		q.setParameter("ownerUsername", cdrSyncBatch.getOwnerUsername());
		q.setParameter("syncType", cdrSyncBatch.getSyncType());
		q.setParameter("totalNumberOfPatientsProcessed", cdrSyncBatch.getPatientsProcessed());
		q.setParameter("totalNumberOfPatients", cdrSyncBatch.getPatients());
		q.setParameter("dateStarted", cdrSyncBatch.getDateStarted());
		q.setParameter("dateCompleted", null);
		q.executeUpdate();
	}
	
	@Override
	public void updateCdrSyncBatchStatus(int batchId, String status, int patientsProcessed, boolean done) {
		StringBuilder query = new StringBuilder("update cdr_sync_batch set status = :status, "
		        + "total_number_of_patients_processed = :patientsProcessed");
		if (done) {
			query.append(", date_completed = :dateCompleted");
		}
		query.append(" where cdr_sync_batch_id = :batchId");
		Query q = sessionFactory.getCurrentSession().createSQLQuery(query.toString());
		q.setParameter("status", status);
		q.setParameter("patientsProcessed", patientsProcessed);
		if (done) {
			q.setParameter("dateCompleted", new Date());
		}
		q.setParameter("batchId", batchId);
		int i = q.executeUpdate();
		log.info("Finished updating::" + i);
	}
	
	public CdrSyncBatch getCdrSyncBatchByStatusAndOwner(String status, String owner, String syncType) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(CdrSyncBatch.class);
		criteria.add(Restrictions.eq("status", status));
		criteria.add(Restrictions.eq("ownerUsername", owner));
		criteria.add(Restrictions.eq("syncType", syncType));
		return (CdrSyncBatch) criteria.uniqueResult();
	}
	
	public List<CdrSyncBatch> getRecentSyncBatches() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(CdrSyncBatch.class);
		criteria.addOrder(Order.desc("dateStarted"));
		criteria.setMaxResults(10);
		return criteria.list();
	}
}
