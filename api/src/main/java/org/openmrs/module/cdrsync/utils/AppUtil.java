package org.openmrs.module.cdrsync.utils;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.cdrsync.api.CdrSyncPatientService;
import org.openmrs.module.cdrsync.container.model.Container;
import org.openmrs.module.cdrsync.model.ContainerWrapper;
import org.openmrs.module.cdrsync.model.DatimMap;
import org.openmrs.module.cdrsync.model.EncryptedBody;
import org.openmrs.module.cdrsync.model.enums.SyncType;
import org.openmrs.util.Security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AppUtil {
	
	private static final String datimCode;
	
	private static final String facilityName;
	
	private static final User user;
	
	private static final String partnerShortName;
	
	private static final ObjectMapper objectMapper;
	
	private static final DatimMap datimMap;
	
	static {
		user = Context.getAuthenticatedUser();
		datimCode = Context.getAdministrationService().getGlobalProperty("facility_datim_code");
		facilityName = Context.getAdministrationService().getGlobalProperty("Facility_Name");
		partnerShortName = Context.getAdministrationService().getGlobalProperty("partner_short_name");
		objectMapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		objectMapper.setDateFormat(df);
		
		datimMap = Context.getService(CdrSyncPatientService.class).getDatimMapByDatimCode(datimCode);
	}
	
	private AppUtil() {
	}
	
	public static String ensureDownloadDirectoryExists(String contextPath) {
		String downloadDirectory = Paths.get(new File(contextPath).getParentFile().toString(), "downloads").toString();
		File file = new File(downloadDirectory);
		if (!file.exists() && !file.mkdirs()) {
			throw new RuntimeException("Unable to create download directory");
		}
		return downloadDirectory;
	}
	
	public static String ensureReportDirectoryExists(String contextPath, String reportName, int start) {
		String downloadDirectory = ensureDownloadDirectoryExists(contextPath);
		String reportDirectory = Paths.get(downloadDirectory, reportName).toString();
		File file = new File(reportDirectory);
		if (!file.exists() && !file.mkdirs()) {
			throw new RuntimeException("Unable to create report directory");
		} else if (file.exists()) {
			if (start == 0) {
				try {
					System.out.println("Cleaning report directory");
					FileUtils.cleanDirectory(file);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return reportDirectory;
	}
	
	public static List<Integer> getConfidentialConcepts() {
		return new ArrayList<>(Arrays.asList(159635, 162729, 160638, 160641, 160642));
	}
	
	public static void writeContainerToFile(Container container, String fileName, String reportFolder) throws IOException {
		
		File folder = new File(reportFolder);
		
		File dir = new File(folder, "jsonFiles");
		if (!dir.exists() && !dir.mkdirs()) {
			throw new RuntimeException("Unable to create directory " + dir.getAbsolutePath());
		}
		String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(container);
		File file = new File(dir, fileName);
		FileUtils.writeStringToFile(file, json, "UTF-8");
		
		if (dir.listFiles() != null && Objects.requireNonNull(dir.listFiles()).length == 10000) {
			try {
				String facility = facilityName.replaceAll(" ", "_");
				String dateString = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
				ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(folder.getAbsolutePath(),
				    partnerShortName + "_" + datimCode + "_" + facility + "_" + dateString + "_" + new Date().getTime()
				            + ".zip")));
				zipDirectory(dir, dir.getName(), zos);
				zos.close();
				FileUtils.cleanDirectory(dir);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static String zipFolder(String type, String reportFolder, String contextPath) {
		File folder = new File(reportFolder);
		File dir = new File(folder, "jsonFiles");
		StringBuilder result = new StringBuilder();
		String facility = facilityName.replaceAll(" ", "_");
		if (dir.listFiles() != null) {
			ZipOutputStream zipOutputStream;
			try {
				String dateString = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
				zipOutputStream = new ZipOutputStream(Files.newOutputStream(Paths.get(folder.getAbsolutePath(),
				    partnerShortName + "_" + datimCode + "_" + facility + "_" + dateString + "_" + new Date().getTime()
				            + ".zip")));
				zipDirectory(dir, dir.getName(), zipOutputStream);
				zipOutputStream.close();
				FileUtils.deleteDirectory(dir);
				
				File[] files = folder.listFiles();
				if (files != null) {
					for (File file : files) {
						if (file.getName().endsWith(".zip")) {
							System.out.println("Context path: " + contextPath);
							String filePath = file.getAbsolutePath();
							int index = filePath.lastIndexOf(contextPath.substring(1));
							filePath = filePath.substring(index);
							filePath = filePath.replace("\\", "\\\\");
							filePath = "\\\\" + filePath;
							result.append(filePath).append("&&");
						}
					}
				} else {
					System.out.println("No files found in the folder");
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return "Sync complete!," + result.toString().trim();
	}
	
	private static void zipDirectory(File directory, String baseName, ZipOutputStream zos) throws IOException {
		File[] files = directory.listFiles();
		if (files != null) {
			byte[] buffer = new byte[1024];
			for (File file : files) {
				if (file.isDirectory()) {
					String name = baseName + "/" + file.getName();
					zipDirectory(file, name, zos);
				} else {
					FileInputStream fis = new FileInputStream(file);
					zos.putNextEntry(new ZipEntry(baseName + "/" + file.getName()));
					int length;
					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}
					zos.closeEntry();
					fis.close();
				}
			}
		}
	}
	
	public static void syncContainersToCdr(List<Container> containers) throws IOException {
		ContainerWrapper containerWrapper = new ContainerWrapper(containers);
		if (Context.getRuntimeProperties().getProperty("cdr.sync.url") == null) {
			System.out.println("Setting sync url");
			Context.getRuntimeProperties().setProperty("cdr.sync.url", "http://localhost:8484/sync-containers");
		}
		String url = Context.getRuntimeProperties().getProperty("cdr.sync.url");
		System.out.println("Syncing to CDR::" + url);
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
			String json = objectMapper.writeValueAsString(containerWrapper);
			String encryptedJson = Security.encrypt(json);
			EncryptedBody encryptedBody = new EncryptedBody(encryptedJson);
			HttpPost post = new HttpPost(url);
			HttpGet get = new HttpGet(url);
			get.setHeader("Content-Type", "application/json");
			post.setHeader("Content-Type", "application/json");
			post.setEntity(new StringEntity(objectMapper.writeValueAsString(encryptedBody)));
			try (CloseableHttpResponse response = httpClient.execute(post)){
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					String responseBody = EntityUtils.toString(response.getEntity());
					System.out.println("After successfully sending request::" + responseBody);
				} else {
					System.out.println("error sending request");
					System.out.println("status code::" + statusCode);
					throw new IOException("error sending request to cdr");
				}
			}
		}
	}
}
