package org.openmrs.module.cdrsync.fragment.controller;

import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.cdrsync.api.CdrContainerService;
import org.openmrs.module.cdrsync.api.CdrSyncAdminService;
import org.openmrs.module.cdrsync.api.impl.CdrContainerServiceImpl;
import org.openmrs.module.cdrsync.api.impl.ContainerServiceImpl;
import org.openmrs.module.cdrsync.model.CdrSyncBatch;
import org.openmrs.module.cdrsync.model.enums.SyncStatus;
import org.openmrs.module.cdrsync.model.enums.SyncType;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class ContainerFragmentController {
	
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private final CdrContainerService containerService;
	
	public ContainerFragmentController() {
		containerService = new CdrContainerServiceImpl();
	}
	
	User user = Context.getAuthenticatedUser();
	
	//	CdrContainerService containerService = Context.getService(CdrContainerService.class);
	
	public void controller(FragmentModel model, @SpringBean("userService") UserService service) {
		String lastSyncDate = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		List<CdrSyncBatch> recentSyncBatches = Context.getService(CdrSyncAdminService.class).getRecentSyncBatches();
		if (lastSyncDate == null || lastSyncDate.isEmpty()) {
			lastSyncDate = "N/A";
		}
		model.addAttribute("users", service.getAllUsers());
		model.addAttribute("lastSyncDate", lastSyncDate);
		model.addAttribute("recentSyncBatches", recentSyncBatches);
	}
	
	public ResponseEntity<Long> getPatientsCount() throws IOException {
		Long response = containerService.getPatientsCount(true);
		logger.info("Total count from db::" + response);
		return new ResponseEntity<Long>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<Long> getPatientsCountFromLastSync() throws IOException {
		String lastSync = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		logger.info("Last sync date from db::" + lastSync);
		long response;
		if (lastSync != null && !lastSync.isEmpty()) {
			try {
				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss");
				Date lastSyncDate = dateFormat.parse(lastSync);
				logger.info("Last sync date::" + lastSyncDate);
				response = containerService.getPatientsCount(lastSyncDate, new Date(), true);
			}
			catch (ParseException e) {
				e.printStackTrace();
				return new ResponseEntity<Long>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			response = containerService.getPatientsCount(true);
		}
		return new ResponseEntity<Long>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<Long> getPatientsCountFromCustomDate(@RequestParam(value = "from") String from,
	        @RequestParam(value = "to") String to) throws IOException {
		logger.info("From custom::" + from + " to " + to);
		long response;
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date fromDate = dateFormat.parse(from);
			Date toDate = dateFormat.parse(to);
			response = containerService.getPatientsCount(fromDate, toDate, true);
		}
		catch (ParseException e) {
			e.printStackTrace();
			return new ResponseEntity<Long>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Long>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<String> getPatientsProcessed(@RequestParam(value = "total") String total,
	        @RequestParam(value = "type") String type) {
		CdrSyncBatch cdrSyncBatch = Context.getService(CdrSyncAdminService.class).getCdrSyncBatchByStatusAndOwner(
		    SyncStatus.IN_PROGRESS.name(), user.getUsername(), type);
		if (cdrSyncBatch == null) {
			cdrSyncBatch = new CdrSyncBatch();
			cdrSyncBatch.setPatientsProcessed(0);
			cdrSyncBatch.setPatients(Integer.parseInt(total));
			cdrSyncBatch.setOwnerUsername(user.getUsername());
			cdrSyncBatch.setDateStarted(new Date());
			cdrSyncBatch.setStatus(SyncStatus.IN_PROGRESS.name());
			cdrSyncBatch.setSyncType(type);
			logger.info("date completed::" + cdrSyncBatch.getDateCompleted());
			Context.getService(CdrSyncAdminService.class).saveCdrSyncBatch(cdrSyncBatch);
		}
		Integer id = cdrSyncBatch.getId();
		logger.info("Batch id::" + id);
		String resp;
		if (id != null) {
			resp = cdrSyncBatch.getPatientsProcessed() + "/" + id;
		} else {
			resp = cdrSyncBatch.getPatientsProcessed() + "/" + 0;
		}
		return new ResponseEntity<String>(resp, HttpStatus.OK);
	}
	
	public void updateCdrSyncBatch(@RequestParam(value = "type") String type,
	        @RequestParam(value = "processed") String processed, @RequestParam(value = "id") String id,
	        @RequestParam(value = "total") String total) {
		int processedPatients = Integer.parseInt(processed);
		int batchId = Integer.parseInt(id);
		Context.getService(CdrSyncAdminService.class).updateCdrSyncBatchStatus(batchId, SyncStatus.IN_PROGRESS.name(),
		    processedPatients, false);
	}
	
	public ResponseEntity<String> getPatientsFromInitial(HttpServletRequest request,
	        @RequestParam(value = "start") String start, @RequestParam(value = "length") String length,
	        @RequestParam(value = "total") String total, @RequestParam(value = "id") String id) throws IOException {
		logger.info("From initial::" + start + " " + length);
		String response;
		int startPoint = Integer.parseInt(start);
		long totalPatients = Long.parseLong(total);
		int lengthOfPatients = Integer.parseInt(length);
		int batchId = Integer.parseInt(id);
		String contextPath = request.getContextPath();
		logger.info("context path: " + contextPath);
		String fullContextPath = request.getSession().getServletContext().getRealPath(contextPath);
		logger.info("full context path: " + fullContextPath);
		response = containerService.getAllPatients(totalPatients, startPoint, lengthOfPatients, SyncType.INITIAL.name(),
		    fullContextPath, contextPath);
		if (response.contains("Sync complete!")) {
			Context.getService(CdrSyncAdminService.class).updateCdrSyncBatchStatus(batchId, SyncStatus.COMPLETED.name(),
			    startPoint, true);
		}
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<String> getPatientsFromLastSync(HttpServletRequest request,
	        @RequestParam(value = "start") String start, @RequestParam(value = "length") String length,
	        @RequestParam(value = "total") String total, @RequestParam(value = "id") String id) throws IOException {
		String lastSync = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		logger.info("From db::" + lastSync);
		String response;
		String contextPath = request.getContextPath();
		String fullContextPath = request.getSession().getServletContext().getRealPath(contextPath);
		if (lastSync != null && !lastSync.isEmpty()) {
			try {
				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss");
				Date lastSyncDate = dateFormat.parse(lastSync);
				logger.info("Last sync date::" + lastSyncDate);
				response = containerService.getAllPatients(Long.valueOf(total), lastSyncDate, new Date(),
				    Integer.parseInt(start), Integer.parseInt(length), SyncType.INCREMENTAL.name(), fullContextPath,
				    contextPath);
				return checkIfSyncHasCompletedAndUpdateSyncBatch(start, total, id, response);
			}
			catch (ParseException e) {
				logger.severe("parse exception::" + e.getMessage());
				e.printStackTrace();
				return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			catch (IOException e) {
				logger.severe("Io exception::" + e.getMessage());
				e.printStackTrace();
				return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
		} else {
			response = containerService.getAllPatients(Long.parseLong(total), Integer.parseInt(start),
			    Integer.parseInt(length), SyncType.INCREMENTAL.name(), fullContextPath, contextPath);
			return checkIfSyncHasCompletedAndUpdateSyncBatch(start, total, id, response);
		}
	}
	
	private ResponseEntity<String> checkIfSyncHasCompletedAndUpdateSyncBatch(String start, String total, String id,
	        String response) {
		if (response.contains("Sync complete!")) {
			Context.getService(CdrSyncAdminService.class).updateCdrSyncBatchStatus(Integer.parseInt(id),
			    SyncStatus.COMPLETED.name(), Integer.parseInt(start), true);
		}
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<String> getPatientsFromCustomDate(HttpServletRequest request,
	        @RequestParam(value = "from") String from, @RequestParam(value = "to") String to,
	        @RequestParam(value = "start") String start, @RequestParam(value = "length") String length,
	        @RequestParam(value = "total") String total, @RequestParam(value = "id") String id) throws ParseException,
	        IOException {
		logger.info(from + ":::" + to);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = dateFormat.parse(from);
		Date endDate = dateFormat.parse(to);
		String contextPath = request.getContextPath();
		String fullContextPath = request.getSession().getServletContext().getRealPath(contextPath);
		String response = containerService.getAllPatients(Long.parseLong(total), startDate, endDate,
		    Integer.parseInt(start), Integer.parseInt(length), SyncType.CUSTOM.name(), fullContextPath, contextPath);
		if (response.contains("Sync complete!")) {
			Context.getService(CdrSyncAdminService.class).updateCdrSyncBatchStatus(Integer.parseInt(id),
			    SyncStatus.COMPLETED.name(), Integer.parseInt(start), true);
		}
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	public void saveLastSync() {
		containerService.saveLastSyncDate();
	}
	
}
