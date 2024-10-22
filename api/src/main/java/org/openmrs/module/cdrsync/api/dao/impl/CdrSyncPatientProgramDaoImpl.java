package org.openmrs.module.cdrsync.api.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO;
import org.openmrs.module.cdrsync.api.dao.CdrSyncPatientProgramDao;

import java.util.Date;
import java.util.List;

public class CdrSyncPatientProgramDaoImpl extends HibernateProgramWorkflowDAO implements CdrSyncPatientProgramDao {
	
	DbSessionFactory sessionFactory;
	
	private DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PatientProgram> getPatientProgramsByPatientAndLastSyncDate(Patient patient, Date startDate, Date endDate) {
		Criteria criteria = getSession().createCriteria(PatientProgram.class);
		criteria.add(Restrictions.eq("patient", patient));
		if (startDate != null) {
			criteria.add(Restrictions.or(Restrictions.ge("dateCreated", startDate),
			    Restrictions.ge("dateChanged", startDate)));
		}
		if (endDate == null)
			endDate = new Date();
		criteria.add(Restrictions.or(Restrictions.le("dateCreated", endDate), Restrictions.le("dateChanged", endDate)));
		return criteria.list();
	}
}
