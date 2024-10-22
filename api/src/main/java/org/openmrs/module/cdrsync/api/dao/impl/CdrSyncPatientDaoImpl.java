package org.openmrs.module.cdrsync.api.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.db.hibernate.HibernatePatientDAO;
import org.openmrs.module.cdrsync.api.dao.CdrSyncPatientDao;
import org.openmrs.module.cdrsync.model.DatimMap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class CdrSyncPatientDaoImpl extends HibernatePatientDAO implements CdrSyncPatientDao {
	
	private final Logger log = Logger.getLogger(this.getClass().getName());
	
	DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public List<Patient> getPatients(Integer start, Integer length, boolean includeVoided) throws DAOException {
		String queryStr;
		if (includeVoided) {
			queryStr = "from Patient";
		} else {
			queryStr = "from Patient p where p.voided = :voided";
		}
		Query query = sessionFactory.getCurrentSession().createQuery(queryStr);
		query.setFirstResult(start);
		query.setMaxResults(length);
		return query.list();
	}
	
	@Override
	public Long getPatientsCount(boolean includeVoided) throws DAOException {
		String queryStr;
		if (includeVoided) {
			queryStr = "select count(*) from Patient";
		} else {
			queryStr = "select count(*) from Patient p where p.voided = :voided";
		}
		Query query = sessionFactory.getCurrentSession().createQuery(queryStr);
		return (Long) query.uniqueResult();
	}
	
	@Override
	public List<Integer> getPatientIds(Integer start, Integer length, boolean includeVoided) throws DAOException {
		String queryStr;
		if (includeVoided) {
			queryStr = "select p.patientId from Patient p";
		} else {
			queryStr = "select p.patientId from Patient p where p.voided = :voided";
		}
		Query query = sessionFactory.getCurrentSession().createQuery(queryStr);
		query.setFirstResult(start);
		query.setMaxResults(length);
		return query.list();
	}
	
	@Override
	public List<Integer> getPatientIds(boolean includeVoided) throws DAOException {
		String queryStr;
		if (includeVoided) {
			queryStr = "select p.patientId from Patient p";
		} else {
			queryStr = "select p.patientId from Patient p where p.voided = :voided";
		}
		Query query = sessionFactory.getCurrentSession().createQuery(queryStr);
		return query.list();
	}
	
	@Override
	public List<Integer> getPatientsByLastSyncDate(Date from, Date to, List<String> patientIds, boolean includeVoided,
	        Integer start, Integer length) throws DAOException {
		
		SQLQuery sql = buildQuery(from, to, patientIds, includeVoided);
		if (start != null)
			sql.setFirstResult(start);
		if (length != null)
			sql.setMaxResults(length);
		
		return (List<Integer>) sql.list();
	}
	
	@Override
	public Long getPatientCountFromLastSyncDate(Date from, Date to, List<String> patientIds, boolean includeVoided)
	        throws DAOException {
		SQLQuery sql = buildQuery(from, to, patientIds, includeVoided);
		
		List<Integer> ret = sql.list();
		
		return (long) ret.size();
	}
	
	private SQLQuery buildQuery(Date from, Date to, List<String> patientIds, boolean includeVoided) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String fromStr = sdf.format(from);
		String toStr = sdf.format(to);
		String query = "SELECT DISTINCT patient_id FROM ("
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "patient", "patient_id", false) + " UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "person", "person_id", false) + "  UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "person_address", "person_id", false)
		        + "  UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "patient_identifier", "patient_id", false)
		        + "  UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "patient_program", "patient_id", false)
		        + "  UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "person_attribute", "person_id", false)
		        + "  UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "person_name", "person_id", false)
		        + "  UNION ALL " + getQueryString(fromStr, toStr, patientIds, includeVoided, "obs", "person_id", true)
		        + "  UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "encounter", "patient_id", false)
		        + "  UNION ALL " + getQueryString(fromStr, toStr, patientIds, includeVoided, "visit", "patient_id", false)
		        + ") AS patient_id";
		SQLQuery sql = sessionFactory.getCurrentSession().createSQLQuery(query);
		//		if (from != null) {
		//			System.out.println("from: " + from);
		//			sql.setTimestamp("from", from);
		//		}
		//		if (to != null) {
		//			System.out.println("to: " + to);
		//			sql.setTimestamp("to", to);
		//		}
		if (patientIds != null && patientIds.size() > 0)
			sql.setParameterList("patientIds", patientIds);
		
		return sql;
	}
	
	private String getQueryString(String from, String to, List<String> patientIds, boolean includeVoided, String tableName,
	        String fieldName, boolean noDateChanged) {
		StringBuilder query = new StringBuilder();
		if (tableName.equals("person") || tableName.equals("person_address") || tableName.equals("person_attribute")
		        || tableName.equals("person_name")) {
			query.append("  SELECT ").append(tableName).append(".").append(fieldName).append(" AS patient_id FROM ")
			        .append(tableName).append(" INNER JOIN patient ON ").append(tableName)
			        .append(".person_id = patient.patient_id WHERE TRUE");
			setRangeQuery(from, to, tableName, query);
		} else {
			if (noDateChanged) {
				query.append("  SELECT ").append(tableName).append(".").append(fieldName).append(" AS patient_id FROM ")
				        .append(tableName).append(" WHERE TRUE");
				if (from != null)
					query.append(" AND ").append(tableName).append(".date_created >= '").append(from).append("' ");
				if (to != null)
					query.append(" AND ").append(tableName).append(".date_created <= '").append(to).append("' ");
			} else {
				query.append("  SELECT ").append(tableName).append(".").append(fieldName).append(" AS patient_id FROM ")
				        .append(tableName).append(" WHERE TRUE");
				setRangeQuery(from, to, tableName, query);
			}
		}
		if (!includeVoided)
			query.append(" AND ").append(tableName).append(".voided = FALSE ");
		if (patientIds != null && patientIds.size() > 0)
			query.append(" AND ").append(tableName).append(".").append(fieldName).append(" IN (:patientIds)  ");
		return query.toString();
	}
	
	private void setRangeQuery(String from, String to, String tableName, StringBuilder query) {
		if (from != null)
			query.append(" AND (").append(tableName).append(".date_created >= '").append(from).append("' OR ")
			        .append(tableName).append(".date_changed >= '").append(from).append("' ) ");
		if (to != null)
			query.append(" AND (").append(tableName).append(".date_created <= '").append(to).append("' OR ")
			        .append(tableName).append(".date_changed <= '").append(to).append("' ) ");
	}
	
	@Override
	public DatimMap getDatimMapByDatimCode(String datimCode) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DatimMap.class);
		criteria.add(Restrictions.eq("datimCode", datimCode));
		return (DatimMap) criteria.uniqueResult();
	}
}
