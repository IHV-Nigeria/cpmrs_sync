package org.openmrs.module.cdrsync.api.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.db.hibernate.HibernateVisitDAO;
import org.openmrs.module.cdrsync.api.dao.CdrSyncVisitDao;

import java.util.Date;
import java.util.List;

public class CdrSyncVisitDaoImpl extends HibernateVisitDAO implements CdrSyncVisitDao {
	
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
	
	// Get visits by patient where date created, date changed, or date voided is after from parameter and before to parameter
	@Override
	@SuppressWarnings("unchecked")
	public List<Visit> getVisitsByPatientAndDateChanged(Patient patient, Date from, Date to) {
		Criteria criteria = getSession().createCriteria(Visit.class);
		criteria.add(Restrictions.eq("patient", patient));
		criteria.add(Restrictions.or(Restrictions.gt("dateCreated", from), Restrictions.ge("dateChanged", from)));
		criteria.add(Restrictions.or(Restrictions.le("dateCreated", to), Restrictions.le("dateChanged", to)));
		criteria.add(Restrictions.gt("dateVoided", from));
		criteria.add(Restrictions.le("dateVoided", to));
		return criteria.list();
	}
}
