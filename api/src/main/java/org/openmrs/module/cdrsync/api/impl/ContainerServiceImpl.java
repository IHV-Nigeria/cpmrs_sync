package org.openmrs.module.cdrsync.api.impl;

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.cdrsync.api.*;
import org.openmrs.module.cdrsync.container.model.*;
import org.openmrs.module.cdrsync.container.model.EncounterType;
import org.openmrs.module.cdrsync.container.model.PatientIdentifierType;
import org.openmrs.module.cdrsync.container.model.VisitType;
import org.openmrs.module.cdrsync.model.BiometricInfo;
import org.openmrs.module.cdrsync.model.DatimMap;
import org.openmrs.module.cdrsync.utils.AppUtil;
import org.openmrs.util.Security;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.openmrs.module.cdrsync.utils.AppUtil.writeContainerToFile;

public class ContainerServiceImpl implements ContainerService {
	
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private static final String datimCode;
	
	private static final String facilityName;
	
	private static final User user;
	
	private static final ObjectMapper objectMapper;
	
	private static final DatimMap datimMap;
	
	static {
		user = Context.getAuthenticatedUser();
		datimCode = Context.getAdministrationService().getGlobalProperty("facility_datim_code");
		facilityName = Context.getAdministrationService().getGlobalProperty("Facility_Name");
		objectMapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		objectMapper.setDateFormat(df);
		
		datimMap = Context.getService(CdrSyncPatientService.class).getDatimMapByDatimCode(datimCode);
	}
	
	@Override
	public void createContainer(List<Container> containers, AtomicInteger count, Integer patientId, String reportFolder)
	        throws IOException {
		Patient patient = Context.getPatientService().getPatient(patientId);
		if (patient != null) {
			Container container = new Container();
			Date[] touchTimeDate = new Date[1];
			touchTimeDate[0] = patient.getDateChanged() != null ? patient.getDateChanged()
			        : patient.getDateCreated() != null ? patient.getDateCreated() : null;
			container.setMessageHeader(buildMessageHeader());
			container.setMessageData(buildMessageData(patient, touchTimeDate));
			setContainerTouchTimeAndFileName(containers, patient, touchTimeDate, container, reportFolder);
		}
	}
	
	@Override
	public void createContainerFromLastSyncDate(List<Container> containers, AtomicInteger count, Integer patientId,
	        Date from, Date to) {
		Patient patient = Context.getPatientService().getPatient(patientId);
		Date[] touchTimeDate = new Date[1];
		touchTimeDate[0] = patient.getDateChanged() != null ? patient.getDateChanged()
		        : patient.getDateCreated() != null ? patient.getDateCreated() : null;
		Container container = new Container();
		container.setMessageData(buildMessageDataFromLastSync(patient, touchTimeDate, from, to));
		container.setMessageHeader(buildMessageHeader());
		//		setContainerTouchTimeAndFileName(containers, patient, touchTimeDate, container, reportFolder); //todo
	}
	
	private void setContainerTouchTimeAndFileName(List<Container> containers, Patient patient, Date[] touchTimes,
	        Container container, String reportFolder) throws IOException {
		container.setId(patient.getUuid());
		container.getMessageHeader().setTouchTime(touchTimes[0]);
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String touchTimeString = df.format(container.getMessageHeader().getTouchTime());
		String fileName = patient.getUuid() + "_" + touchTimeString + "_" + datimCode + ".json";
		container.getMessageHeader().setFileName(fileName);
		
		writeContainerToFile(container, fileName, reportFolder);
		
		//		containers.add(container);
		//		if (containers.size() == 50) {
		//			try {
		//				syncContainersToCdr(containers);
		//				containers.clear();
		//			}
		//			catch (IOException e) {
		//				containers.clear();
		//				throw new RuntimeException(e);
		//			}
		//
		//		}
	}
	
	private MessageHeaderType buildMessageHeader() {
		MessageHeaderType messageHeaderType = new MessageHeaderType();
		messageHeaderType.setFacilityName(facilityName);
		messageHeaderType.setFacilityDatimCode(datimCode);
		if (datimMap != null) {
			messageHeaderType.setFacilityLga(datimMap.getLgaName());
			messageHeaderType.setFacilityState(datimMap.getStateName());
		}
		messageHeaderType.setMessageCreationDateTime(new Date());
		messageHeaderType.setMessageSchemaVersion(new BigDecimal("1.1"));
		messageHeaderType.setMessageStatusCode("SYNCED");
		messageHeaderType.setMessageUniqueID(UUID.randomUUID().toString());
		messageHeaderType.setMessageSource("NMRS");
		return messageHeaderType;
	}
	
	private MessageDataType buildMessageData(Patient patient, Date[] touchTime) {
        MessageDataType messageDataType = new MessageDataType();
        List<EncounterProvider> providers = new ArrayList<>();
        List<Obs> obsList = new ArrayList<>();
        messageDataType.setDemographics(buildDemographics(patient, touchTime));
        messageDataType.setVisits(buildVisits(patient, touchTime));
        messageDataType.setEncounters(buildEncounters(patient, providers, touchTime, obsList));
        messageDataType.setObs(buildObs(patient, touchTime, obsList));
        messageDataType.setEncounterProviders(buildEncounterProviders(providers, patient, touchTime));
        messageDataType.setPatientBiometrics(buildPatientBiometrics(patient, touchTime));
        messageDataType.setPatientPrograms(buildPatientProgram(patient, touchTime));
        messageDataType.setPatientIdentifiers(buildPatientIdentifier(patient, touchTime));
        return messageDataType;
    }
	
	private MessageDataType buildMessageDataFromLastSync(Patient patient, Date[] touchTime, Date from, Date to) {
        MessageDataType messageData = new MessageDataType();
        List<EncounterProvider> providers = new ArrayList<>();
        List<Obs> obsList = new ArrayList<>();
        messageData.setDemographics(buildDemographics(patient, touchTime));
        messageData.setVisits(buildVisits(patient, touchTime, from, to));
        messageData.setEncounters(buildEncounters(patient, providers, touchTime, obsList, from, to));
        messageData.setObs(buildObs(patient, touchTime, obsList));
        messageData.setEncounterProviders(buildEncounterProviders(providers, patient, touchTime));
        messageData.setPatientBiometrics(buildPatientBiometrics(patient, touchTime, from));
        messageData.setPatientPrograms(buildPatientProgram(patient, touchTime, from, to));
        messageData.setPatientIdentifiers(buildPatientIdentifier(patient, touchTime, from));
        return messageData;
    }
	
	private DemographicsType buildDemographics(Patient patient, Date[] touchTime) {
		DemographicsType demographicsType = new DemographicsType();
		PersonAddress personAddress = patient.getPersonAddress();
		if (personAddress != null)
			setPatientTouchTime(touchTime, personAddress.getDateChanged(), personAddress.getDateCreated());
		
		PersonName personName = patient.getPersonName();
		if (personName != null)
			setPatientTouchTime(touchTime, personName.getDateChanged(), personName.getDateCreated());
		
		PersonAttribute personAttribute = patient.getAttribute(8);
		if (personAttribute != null)
			setPatientTouchTime(touchTime, personAttribute.getDateChanged(), personAttribute.getDateCreated());
		return setContainerDemographics(patient, touchTime, demographicsType, personAddress);
	}
	
	private void checkUpdatedDate(Date[] touchTimes, Date dateChanged, Date dateCreated) {
		if (dateChanged != null) {
			if (touchTimes[0].before(dateChanged))
				touchTimes[0] = dateChanged;
		} else if (dateCreated != null) {
			if (touchTimes[0].before(dateCreated))
				touchTimes[0] = dateCreated;
		}
	}
	
	private DemographicsType setContainerDemographics(Patient patient, Date[] touchTime, DemographicsType demographicsType,
	        PersonAddress personAddress) {
		if (personAddress != null) {
			if (personAddress.getAddress1() != null && !personAddress.getAddress1().isEmpty()) {
				demographicsType.setAddress1(Security.encrypt(personAddress.getAddress1()));
			}
			if (personAddress.getAddress2() != null && !personAddress.getAddress2().isEmpty()) {
				demographicsType.setAddress2(Security.encrypt(personAddress.getAddress2()));
			}
			demographicsType.setCityVillage(personAddress.getCityVillage());
			demographicsType.setStateProvince(personAddress.getStateProvince());
			demographicsType.setCountry(personAddress.getCountry());
		}
		if (patient.getVoided()) {
			demographicsType.setVoided(1);
			demographicsType.setVoidedBy(patient.getVoidedBy() != null ? patient.getVoidedBy().getId() : 0);
			demographicsType.setDateVoided(patient.getDateVoided());
			if (touchTime[0] == null || touchTime[0].before(patient.getDateVoided()))
				touchTime[0] = patient.getDateVoided();
			demographicsType.setVoidedReason(patient.getPersonVoidReason());
		} else
			demographicsType.setVoided(0);
		
		if (patient.getCreator() != null)
			demographicsType.setCreator(patient.getCreator().getId());
		if (patient.getDateCreated() != null)
			demographicsType.setDateCreated(patient.getDateCreated());
		
		demographicsType.setBirthdate(patient.getBirthdate());
		demographicsType.setBirthdateEstimated(patient.getBirthdateEstimated() ? 1 : 0);
		try {
			demographicsType.setChangedBy(patient.getChangedBy() != null ? patient.getChangedBy().getId() : 0);
		}
		catch (Exception e) {
			demographicsType.setChangedBy(0);
		}
		demographicsType.setDeathdateEstimated(patient.getDeathdateEstimated() ? 1 : 0);
		demographicsType.setDeathDate(patient.getDeathDate());
		demographicsType.setDead(patient.getDead() ? 1 : 0);
		demographicsType.setCauseOfDeath(patient.getCauseOfDeath() != null ? patient.getCauseOfDeath().getName().getName()
		        : null);
		demographicsType.setFirstName(patient.getGivenName() != null ? Security.encrypt(patient.getGivenName()) : "");
		demographicsType.setLastName(patient.getFamilyName() != null ? Security.encrypt(patient.getFamilyName()) : "");
		demographicsType.setMiddleName(patient.getMiddleName() != null ? Security.encrypt(patient.getMiddleName()) : "");
		PersonAttribute personAttribute = patient.getAttribute(8);
		demographicsType.setPhoneNumber(personAttribute != null ? Security.encrypt(personAttribute.getValue()) : "");
		demographicsType.setGender(patient.getGender());
		demographicsType.setPatientUuid(patient.getUuid());
		demographicsType.setPatientId(patient.getPersonId());
		demographicsType.setDatimId(datimCode);
		demographicsType.setDateChanged(patient.getDateChanged());
		return demographicsType;
	}
	
	private void setPatientTouchTime(Date[] touchTime, Date dateChanged, Date dateCreated) {
		if (dateChanged == null && dateCreated == null)
			return;
		
		if (dateChanged != null) {
			if (touchTime[0] == null || touchTime[0].before(dateChanged))
				touchTime[0] = dateChanged;
			return;
		}
		if (touchTime[0] == null || touchTime[0].before(dateCreated))
			touchTime[0] = dateCreated;
		
	}
	
	private List<VisitType> buildVisits(Patient patient, Date[] touchTime) {
        List<VisitType> visitTypes = new ArrayList<>();
        List<Visit> visits = Context.getVisitService().getVisits(
                null, Collections.singletonList(patient), null, null, null,
                null, null, null, null, true, true
        );
        if (visits != null && !visits.isEmpty()) {
            buildContainerVisitType(patient, touchTime, visitTypes, visits);
        }
        return visitTypes;
    }
	
	private List<VisitType> buildVisits(Patient patient, Date[] touchTimes, Date startDate, Date endDate) {
        List<VisitType> visitTypes = new ArrayList<>();
        List<Visit> visits = Context.getService(CdrSyncVisitService.class).getVisitsByPatientAndDateChanged(patient, startDate, endDate);
        if (visits != null && !visits.isEmpty()) {
            buildContainerVisitType(patient, touchTimes, visitTypes, visits);
        }
        return visitTypes;
    }
	
	private void buildContainerVisitType(Patient patient, Date[] touchTime, List<VisitType> visitTypes, List<Visit> visits) {
        visits.forEach(visit -> {
            VisitType visitType = new VisitType();
            visitType.setVisitId(visit.getVisitId());
            visitType.setPatientId(visit.getPatient().getPatientId());
            visitType.setVisitTypeId(visit.getVisitType() != null ? visit.getVisitType().getVisitTypeId() : 0);
            visitType.setDateStarted(visit.getStartDatetime());
            visitType.setCreator(visit.getCreator() != null ? visit.getCreator().getId() : 0);
            visitType.setDateStopped(visit.getStopDatetime());
            visitType.setDateCreated(visit.getDateCreated());
            visitType.setChangedBy(visit.getChangedBy() != null ? visit.getChangedBy().getId() : 0);
            visitType.setDateChanged(visit.getDateChanged());
            visitType.setVoided(visit.getVoided() ? 1 : 0);
            visitType.setVoidedBy(visit.getVoided() ? visit.getVoidedBy() != null ? visit.getVoidedBy().getId() : 0 : 0);
            visitType.setDateVoided(visit.getDateVoided());
            visitType.setVisitUuid(visit.getUuid());
            visitType.setLocationId(visit.getLocation() != null ? visit.getLocation().getLocationId() : 0);
            visitType.setPatientUuid(patient.getPerson().getUuid());
            visitType.setDatimId(datimCode);
            visitTypes.add(visitType);
            if (visit.getDateChanged() != null) {
                if (touchTime[0] == null || touchTime[0].before(visit.getDateChanged()))
                    touchTime[0] = visit.getDateChanged();
            } else {
                if (visit.getDateCreated() != null && (touchTime[0] == null || touchTime[0].before(visit.getDateCreated())))
                    touchTime[0] = visit.getDateCreated();
            }
            if (visit.getDateVoided() != null && (touchTime[0] == null || touchTime[0].before(visit.getDateVoided()))) {
                touchTime[0] = visit.getDateVoided();
            }
        });
    }
	
	private List<PatientBiometricType> buildPatientBiometrics(Patient patient, Date[] touchTime) {
        List<PatientBiometricType> patientBiometricTypes = new ArrayList<>();
        List<BiometricInfo> biometricInfos = Context.getService(BiometricInfoService.class).getBiometricInfoByPatientId(patient.getPatientId());
        if (biometricInfos != null && !biometricInfos.isEmpty()) {
            buildContainerBiometricType(patient, touchTime, patientBiometricTypes, biometricInfos);
        }
        return patientBiometricTypes;
    }
	
	private List<PatientBiometricType> buildPatientBiometrics(Patient patient, Date[] touchTimes, Date startDate) {
        List<PatientBiometricType> patientBiometricTypes = new ArrayList<>();
        List<BiometricInfo> biometricInfos = Context.getService(BiometricInfoService.class).getBiometricInfoByPatientIdAndDateCaptured(patient.getPatientId(), startDate);
        if (biometricInfos != null && !biometricInfos.isEmpty()) {
            buildContainerBiometricType(patient, touchTimes, patientBiometricTypes, biometricInfos);
        }
        return patientBiometricTypes;
    }
	
	private void buildContainerBiometricType(Patient patient, Date[] touchTime, List<PatientBiometricType> patientBiometricTypes, List<BiometricInfo> biometricInfos) {
        biometricInfos.forEach(biometricInfo -> {
            PatientBiometricType patientBiometricType = new PatientBiometricType();
            patientBiometricType.setBiometricInfoId(biometricInfo.getBiometricInfoId());
            patientBiometricType.setPatientId(patient.getPatientId());
            patientBiometricType.setCreator(biometricInfo.getCreator());
            patientBiometricType.setPatientUuid(patient.getPerson().getUuid());
            patientBiometricType.setDateCreated(biometricInfo.getDateCreated());
            patientBiometricType.setDatimId(datimCode);
            patientBiometricType.setFingerPosition(biometricInfo.getFingerPosition());
            patientBiometricType.setImageDpi(biometricInfo.getImageDPI());
            patientBiometricType.setImageHeight(biometricInfo.getImageHeight());
            patientBiometricType.setImageQuality(biometricInfo.getImageQuality());
            patientBiometricType.setImageWidth(biometricInfo.getImageWidth());
            patientBiometricType.setManufacturer(biometricInfo.getManufacturer());
            patientBiometricType.setModel(biometricInfo.getModel());
            patientBiometricType.setSerialNumber(biometricInfo.getSerialNumber());
            patientBiometricType.setTemplate(biometricInfo.getTemplate());
            patientBiometricTypes.add(patientBiometricType);
            if (touchTime[0] == null || touchTime[0].before(biometricInfo.getDateCreated()))
                touchTime[0] = biometricInfo.getDateCreated();
        });
    }
	
	private List<PatientProgramType> buildPatientProgram(Patient patient, Date[] touchTime) {
        List<PatientProgramType> patientProgramTypes = new ArrayList<>();
        List<PatientProgram> patientPrograms = Context.getProgramWorkflowService().getPatientPrograms(
                patient, null, null, null, null, null, true
        );
        if (patientPrograms != null && !patientPrograms.isEmpty()) {
            buildContainerPatientProgramType(touchTime, patientProgramTypes, patientPrograms);
        }
        return patientProgramTypes;
    }
	
	private List<PatientProgramType> buildPatientProgram(Patient patient, Date[] touchTimes, Date from, Date to) {
        List<PatientProgramType> patientProgramTypes = new ArrayList<>();
        List<PatientProgram> patientPrograms = Context.getService(CdrSyncPatientProgramService.class)
                .getPatientProgramsByPatientAndLastSyncDate(patient, from, to);
        if (patientPrograms != null && !patientPrograms.isEmpty()) {
            buildContainerPatientProgramType(touchTimes, patientProgramTypes, patientPrograms);
        }
        return patientProgramTypes;
    }
	
	private void buildContainerPatientProgramType(Date[] touchTime, List<PatientProgramType> patientProgramTypes, List<PatientProgram> patientPrograms) {
        patientPrograms.forEach(patientProgram -> {
            PatientProgramType patientProgramType = new PatientProgramType();
            patientProgramType.setPatientProgramId(patientProgram.getPatientProgramId());
            patientProgramType.setPatientId(patientProgram.getPatient().getPatientId());
            patientProgramType.setProgramId(patientProgram.getProgram() != null ?
                    patientProgram.getProgram().getProgramId() : 0);
            patientProgramType.setProgramName(patientProgram.getProgram() != null ?
                    patientProgram.getProgram().getName() : "");
            patientProgramType.setDateEnrolled(patientProgram.getDateEnrolled());
            patientProgramType.setDateCompleted(patientProgram.getDateCompleted());
            patientProgramType.setOutcomeConceptId(patientProgram.getOutcome() != null ?
                    patientProgram.getOutcome().getConceptId() : 0);
            patientProgramType.setCreator(patientProgram.getCreator() != null ?
                    patientProgram.getCreator().getId() : 0);
            patientProgramType.setDateCreated(patientProgram.getDateCreated());
            patientProgramType.setDateChanged(patientProgram.getDateChanged());
            patientProgramType.setChangedBy(patientProgram.getChangedBy() != null ?
                    patientProgram.getChangedBy().getId() : 0);
            patientProgramType.setVoided(patientProgram.getVoided() ? 1 : 0);
            patientProgramType.setVoidedBy(patientProgram.getVoided() ? patientProgram.getVoidedBy() != null ? patientProgram.getVoidedBy().getId() : 0 : 0);
            patientProgramType.setDateVoided(patientProgram.getDateVoided());
            patientProgramType.setPatientProgramUuid(patientProgram.getUuid());
            patientProgramType.setPatientUuid(patientProgram.getPatient().getPerson().getUuid());
            patientProgramType.setLocationId(patientProgram.getLocation() != null ?
                    patientProgram.getLocation().getLocationId() : 0);
            patientProgramType.setDatimId(datimCode);
            patientProgramTypes.add(patientProgramType);
            updatePatientTouchTime(touchTime, patientProgram.getDateChanged(), patientProgram.getDateCreated(), patientProgram.getDateVoided());
        });
    }
	
	private List<PatientIdentifierType> buildPatientIdentifier (Patient patient, Date[] touchTime) {
        List<PatientIdentifierType> patientIdentifierTypes = new ArrayList<>();
        Set<PatientIdentifier> patientIdentifiers = patient.getIdentifiers();
        if (patientIdentifiers != null && !patientIdentifiers.isEmpty()) {
            buildContainerPatientIdentifier(patient, touchTime, patientIdentifierTypes, patientIdentifiers);
        }
        return patientIdentifierTypes;
    }
	
	private List<PatientIdentifierType> buildPatientIdentifier (Patient patient, Date[] touchTimes, Date lastSyncDate) {
        List<PatientIdentifierType> patientIdentifierTypes = new ArrayList<>();
        Set<PatientIdentifier> patientIdentifiers = patient.getIdentifiers();
        if (patientIdentifiers != null && !patientIdentifiers.isEmpty()) {
            Set<PatientIdentifier> updatedPatientIdentifiers = patientIdentifiers.stream()
                    .filter(patientIdentifier -> (patientIdentifier.getDateChanged() != null &&
                            patientIdentifier.getDateChanged().after(lastSyncDate)) ||
                            (patientIdentifier.getDateVoided() != null &&
                                    patientIdentifier.getDateVoided().after(lastSyncDate)) ||
                            (patientIdentifier.getDateCreated() != null &&
                                    patientIdentifier.getDateCreated().after(lastSyncDate)))
                    .collect(Collectors.toSet());
            if (!updatedPatientIdentifiers.isEmpty()) {
                buildContainerPatientIdentifier(patient, touchTimes, patientIdentifierTypes, updatedPatientIdentifiers);
            }
        }
        return patientIdentifierTypes;
    }
	
	private void buildContainerPatientIdentifier(Patient patient, Date[] touchTime, List<PatientIdentifierType> patientIdentifierTypes, Set<PatientIdentifier> updatedPatientIdentifiers) {
        updatedPatientIdentifiers.forEach(patientIdentifier -> {
            PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
            patientIdentifierType.setPatientIdentifierId(patientIdentifier.getPatientIdentifierId());
            patientIdentifierType.setPatientId(patientIdentifier.getPatient().getPatientId());
            patientIdentifierType.setIdentifier(patientIdentifier.getIdentifier());
            patientIdentifierType.setIdentifierType(patientIdentifier.getIdentifierType() != null ? patientIdentifier.getIdentifierType().getId() : 0);
            patientIdentifierType.setPreferred(patientIdentifier.getPreferred() ? 1 : 0);
            patientIdentifierType.setCreator(patientIdentifier.getCreator() != null ? patientIdentifier.getCreator().getId() : 0);
            patientIdentifierType.setPatientIdentifierUuid(patientIdentifier.getUuid());
            patientIdentifierType.setDateChanged(patientIdentifier.getDateChanged());
            patientIdentifierType.setDateCreated(patientIdentifier.getDateCreated());
            patientIdentifierType.setChangedBy(patientIdentifier.getChangedBy() != null ? patientIdentifier.getChangedBy().getId() : 0);
            patientIdentifierType.setVoided(patientIdentifier.getVoided() ? 1 : 0);
            patientIdentifierType.setVoidedBy(patientIdentifier.getVoided() ? patientIdentifier.getVoidedBy() != null ? patientIdentifier.getVoidedBy().getId() : 0 : 0);
            patientIdentifierType.setDateVoided(patientIdentifier.getDateVoided());
            patientIdentifierType.setDatimId(datimCode);
            patientIdentifierType.setPatientUuid(patient.getUuid());
            patientIdentifierTypes.add(patientIdentifierType);
            updatePatientTouchTime(touchTime, patientIdentifier.getDateChanged(), patientIdentifier.getDateCreated(), patientIdentifier.getDateVoided());
        });
    }
	
	private List<EncounterType> buildEncounters(
            Patient patient, List<EncounterProvider> providers, Date[] touchTime, List<Obs> obsList
    ) {
        List<EncounterType> encounterTypes = new ArrayList<>();
        List<Encounter> encounterList = Context.getEncounterService().getEncounters(patient, null, null, null, null, null, null, null, null, true);
        if (encounterList != null && !encounterList.isEmpty())
            buildContainerEncounterType(patient, providers, touchTime, encounterTypes, encounterList, obsList);
        return encounterTypes;
    }
	
	private List<EncounterType> buildEncounters(
            Patient patient, List<EncounterProvider> providers, Date[] touchTimes,
            List<Obs> obsList, Date fromDate, Date toDate) {
        List<EncounterType> encounterTypes = new ArrayList<>();
        List<Encounter> encounterList = Context.getService(CdrSyncEncounterService.class).getEncountersByLastSyncDateAndPatient(fromDate, toDate, patient);
        if (encounterList != null && !encounterList.isEmpty()) {
            buildContainerEncounterType(patient, providers, touchTimes, encounterTypes, encounterList, obsList);
        }
        return encounterTypes;
    }
	
	private void buildContainerEncounterType(
            Patient patient, List<EncounterProvider> providers, Date[] touchTime,
            List<EncounterType> encounterTypes, List<Encounter> encounters, List<Obs> obsList
    ) {
        encounters.forEach(encounter -> {
            EncounterType encounterType = new EncounterType();
            encounterType.setPatientUuid(patient.getPerson().getUuid());
            encounterType.setDatimId(datimCode);
            if (encounter.getVisit() != null) {
                encounterType.setVisitId(encounter.getVisit().getVisitId());
                encounterType.setVisitUuid(encounter.getVisit().getUuid());
            }
            encounterType.setEncounterUuid(encounter.getUuid());
            encounterType.setEncounterId(encounter.getEncounterId());
            encounterType.setEncounterTypeId(encounter.getEncounterType() != null ?
                    encounter.getEncounterType().getEncounterTypeId() : 0);
            encounterType.setPatientId(patient.getPatientId());
            encounterType.setLocationId(encounter.getLocation() != null ? encounter.getLocation().getLocationId() : 0);
            try {
                encounterType.setFormId(encounter.getForm() != null ? encounter.getForm().getFormId() : 0);
                encounterType.setPmmForm(encounter.getForm() != null ? encounter.getForm().getName() != null ? encounter.getForm().getName() : "" : "");
            } catch (Exception e) {
                logger.warning("Error getting form name: " + e.getMessage());
            }
            encounterType.setEncounterDatetime(encounter.getEncounterDatetime());
            encounterType.setCreator(encounter.getCreator() != null ? encounter.getCreator().getId() : 0);
            encounterType.setDateCreated(encounter.getDateCreated());
            try {
                encounterType.setChangedBy(encounter.getChangedBy() != null ? encounter.getChangedBy().getId() : 0);
            } catch (Exception e) {
                logger.warning("Error getting changed by: " + e.getMessage());
            }
            encounterType.setDateChanged(encounter.getDateChanged());
            encounterType.setVoided(encounter.getVoided() ? 1 : 0);
            encounterType.setVoidedBy(encounter.getVoided() ?
                    encounter.getVoidedBy() != null ?
                            encounter.getVoidedBy().getId() : 0 : 0);
            encounterType.setDateVoided(encounter.getDateVoided());
            encounterTypes.add(encounterType);
            Set<EncounterProvider> encounterProviders = encounter.getEncounterProviders();
            if (!encounterProviders.isEmpty()) {
                providers.addAll(encounterProviders);
            }
            updatePatientTouchTime(touchTime, encounter.getDateChanged(), encounter.getDateCreated(), encounter.getDateVoided());
            Set<Obs> obsSet = encounter.getAllObs(true);
            if (obsSet != null && !obsSet.isEmpty()) {
                obsList.addAll(obsSet);
            }
        });
    }
	
	private void updatePatientTouchTime(Date[] touchTime, Date dateChanged, Date dateCreated, Date dateVoided) {
		if (dateChanged == null && dateVoided == null && dateCreated == null)
			return;
		if (touchTime[0] == null) {
			if (dateChanged != null)
				touchTime[0] = dateChanged;
			else if (dateVoided != null)
				touchTime[0] = dateVoided;
			else
				touchTime[0] = dateCreated;
		}
		if (dateChanged != null && touchTime[0].before(dateChanged)) {
			touchTime[0] = dateChanged;
		} else {
			if (dateCreated != null && touchTime[0].before(dateCreated))
				touchTime[0] = dateCreated;
		}
		if (dateVoided != null && touchTime[0].before(dateVoided))
			touchTime[0] = dateVoided;
		
	}
	
	private List<ObsType> buildObs(Patient patient, Date[] touchTime, List<Obs> obsList) {
        List<ObsType> obsTypeList = new ArrayList<>();
        if (obsList != null && !obsList.isEmpty())
            buildContainerObsType(patient, touchTime, obsTypeList, obsList);
        return obsTypeList;
    }
	
	private void buildContainerObsType(Patient patient, Date[] touchTimes, List<ObsType> obsTypeList, List<Obs> obsList) {
        List<Integer> confidentialConcepts = AppUtil.getConfidentialConcepts(); //todo get from global property
        obsList.forEach(obs -> {
            ObsType obsType = new ObsType();
            obsType.setPatientUuid(patient.getUuid());
            obsType.setDatimId(datimCode);
            obsType.setObsUuid(obs.getUuid());
            obsType.setObsId(obs.getObsId());
            obsType.setPersonId(obs.getPersonId());
            try {
                obsType.setConceptId(obs.getConcept() != null ? obs.getConcept().getConceptId() : 0);
                obsType.setVariableName(obs.getConcept() != null ? obs.getConcept().getName() != null ? obs.getConcept().getName().getName() : "" : "");
                obsType.setDatatype(obs.getConcept() != null ? obs.getConcept().getDatatype() != null ? obs.getConcept().getDatatype().getConceptDatatypeId() : 0 : 0);
            } catch (Exception e) {
                logger.warning("Error getting concept name: " + e.getMessage());
            }

            if (obs.getEncounter() != null) {
                obsType.setEncounterId(obs.getEncounter().getEncounterId());
                obsType.setEncounterUuid(obs.getEncounter().getUuid());
                try {
                    obsType.setPmmForm(obs.getEncounter().getForm() != null ?
                            obs.getEncounter().getForm().getName() : "");
                    obsType.setFormId(obs.getEncounter().getForm() != null ?
                            obs.getEncounter().getForm().getFormId() : 0);
                } catch (Exception e) {
                    logger.warning("Error getting form name: " + e.getMessage());
                }
                obsType.setEncounterType(obs.getEncounter().getEncounterType() != null ?
                        obs.getEncounter().getEncounterType().getEncounterTypeId() : 0);
                obsType.setVisitUuid(obs.getEncounter().getVisit() != null ?
                        obs.getEncounter().getVisit().getUuid() : "");
            }
            obsType.setObsDatetime(obs.getObsDatetime());
            try {
                obsType.setObsGroupId(obs.getObsGroup() != null ? obs.getObsGroup().getObsId() : 0);
                obsType.setValueCoded(obs.getValueCoded() != null ? obs.getValueCoded().getConceptId() : 0);
            } catch (Exception e) {
                logger.warning("Error getting valueCode/obsGroup id: " + e.getMessage());
            }

            obsType.setValueDatetime(obs.getValueDatetime());
            obsType.setValueNumeric(obs.getValueNumeric() != null ?
                    BigDecimal.valueOf(obs.getValueNumeric()) : null);
            if (confidentialConcepts.contains(obsType.getConceptId())) {
                obsType.setValueText(obs.getValueText() != null ? Security.encrypt(obs.getValueText()) : "");
                String variableValue = "";
                if (obs.getValueCoded() != null) {
                    if (obs.getValueCoded().getName() != null) {
                        variableValue = Security.encrypt(obs.getValueCoded().getName().getName());
                    }
                } else if (obs.getValueText() != null) {
                    variableValue = Security.encrypt(obs.getValueText());
                } else if (obs.getValueDatetime() != null) {
                    variableValue = Security.encrypt(String.valueOf(obs.getValueDatetime()));
                } else if (obs.getValueNumeric() != null) {
                    variableValue = Security.encrypt(String.valueOf(obs.getValueNumeric()));
                }
                obsType.setVariableValue(variableValue);
            } else {
                obsType.setValueText(obs.getValueText());
                try {
                    String variableValue = "";
                    if (obs.getValueCoded() != null) {
                        if (obs.getValueCoded().getName() != null) {
                            variableValue = obs.getValueCoded().getName().getName();
                        }
                    } else if (obs.getValueText() != null) {
                        variableValue = obs.getValueText();
                    } else if (obs.getValueDatetime() != null) {
                        variableValue = String.valueOf(obs.getValueDatetime());
                    } else if (obs.getValueNumeric() != null) {
                        variableValue = String.valueOf(obs.getValueNumeric());
                    }
                    obsType.setVariableValue(variableValue);
                } catch (Exception e) {
                    logger.warning("error get concept id: " + e.getMessage());
                }
            }
            try {
                obsType.setCreator(obs.getCreator() != null ? obs.getCreator().getId() : 0);
            } catch (Exception e) {
                logger.warning("Error getting creator id: " + e.getMessage());
            }
            obsType.setDateCreated(obs.getDateCreated());

            obsType.setLocationId(obs.getLocation() != null ? obs.getLocation().getLocationId() : 0);
            obsType.setVoided(obs.getVoided() ? 1 : 0);
            obsType.setVoidedBy(obs.getVoided() ? obs.getVoidedBy() != null ? obs.getVoidedBy().getId() : 0 : 0);
            obsType.setDateVoided(obs.getDateVoided());
            obsTypeList.add(obsType);
            updatePatientTouchTime(touchTimes, obs.getDateChanged(), obs.getDateCreated(), obs.getDateVoided());
        });
    }
	
	private List<EncounterProviderType> buildEncounterProviders(List<EncounterProvider> providers, Patient patient, Date[] touchTimes) {
        List<EncounterProviderType> encounterProviderTypes = new ArrayList<>();
        if (providers != null && !providers.isEmpty()) {
            providers.forEach(encounterProvider -> {
                EncounterProviderType providerType = new EncounterProviderType();
                providerType.setEncounterProviderId(encounterProvider.getEncounterProviderId());
                providerType.setEncounterId(encounterProvider.getEncounter() != null ? encounterProvider.getEncounter().getEncounterId() : 0);
                providerType.setProviderId(encounterProvider.getProvider() != null ? encounterProvider.getProvider().getProviderId() : 0);
                providerType.setEncounterRoleId(encounterProvider.getEncounterRole() != null ? encounterProvider.getEncounterRole().getEncounterRoleId() : 0);
                providerType.setCreator(encounterProvider.getCreator() != null ? encounterProvider.getCreator().getId() : 0);
                providerType.setDateCreated(encounterProvider.getDateCreated());
                providerType.setChangedBy(encounterProvider.getChangedBy() != null ? encounterProvider.getChangedBy().getId() : 0);
                providerType.setDateChanged(encounterProvider.getDateChanged());
                providerType.setVoided(encounterProvider.getVoided() ? 1 : 0);
                providerType.setDateVoided(encounterProvider.getDateVoided());
                providerType.setVoidedBy(encounterProvider.getVoidedBy() != null ? encounterProvider.getVoidedBy().getId() : 0);
                providerType.setVoidedReason(encounterProvider.getVoidReason());
                providerType.setEncounterProviderUuid(encounterProvider.getUuid());
                providerType.setEncounterUuid(encounterProvider.getEncounter() != null ? encounterProvider.getEncounter().getUuid() : null);
                providerType.setVisitUuid(encounterProvider.getEncounter() != null ? encounterProvider.getEncounter().getVisit() != null ? encounterProvider.getEncounter().getVisit().getUuid() : null : null);
                providerType.setLocationId(encounterProvider.getEncounter() != null ? encounterProvider.getEncounter().getLocation() != null ? encounterProvider.getEncounter().getLocation().getLocationId() : 0 : 0);
                providerType.setPatientUuid(patient.getUuid());
                providerType.setDatimId(datimCode);
                encounterProviderTypes.add(providerType);
                updatePatientTouchTime(touchTimes, encounterProvider.getDateChanged(), encounterProvider.getDateCreated(), encounterProvider.getDateVoided());
            });
        }
        return encounterProviderTypes;
    }
}
