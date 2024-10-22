package org.openmrs.module.cdrsync.api;

import org.openmrs.module.cdrsync.model.BiometricInfo;

import java.util.Date;
import java.util.List;

public interface BiometricInfoService {
	
	List<BiometricInfo> getBiometricInfoByPatientId(Integer patientId);
	
	List<BiometricInfo> getBiometricInfoByPatientIdAndDateCaptured(Integer patientId, Date dateCaptured);
}
