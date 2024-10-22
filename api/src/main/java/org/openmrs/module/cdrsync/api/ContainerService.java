package org.openmrs.module.cdrsync.api;

import org.openmrs.module.cdrsync.container.model.Container;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface ContainerService {
	
	void createContainer(List<Container> containers, AtomicInteger count, Integer patientId, String reportFolder)
	        throws IOException;
	
	void createContainerFromLastSyncDate(List<Container> containers, AtomicInteger count, Integer patientId, Date from,
	        Date to);
}
