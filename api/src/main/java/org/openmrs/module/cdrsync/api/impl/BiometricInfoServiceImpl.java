package org.openmrs.module.cdrsync.api.impl;

import org.openmrs.module.cdrsync.api.BiometricInfoService;
import org.openmrs.module.cdrsync.api.dao.BiometricInfoDao;
import org.openmrs.module.cdrsync.model.BiometricInfo;

import java.util.Date;
import java.util.List;

//@Service
public class BiometricInfoServiceImpl implements BiometricInfoService {
	
	private BiometricInfoDao biometricInfoDao;
	
	public void setDao(BiometricInfoDao biometricInfoDao) {
		this.biometricInfoDao = biometricInfoDao;
	}
	
	@Override
	public List<BiometricInfo> getBiometricInfoByPatientId(Integer patientId) {
		return biometricInfoDao.getBiometricInfoByPatientId(patientId);
	}
	
	@Override
	public List<BiometricInfo> getBiometricInfoByPatientIdAndDateCaptured(Integer patientId, Date dateCaptured) {
		return biometricInfoDao.getBiometricInfoByPatientIdAndDateCaptured(patientId, dateCaptured);
	}
}
