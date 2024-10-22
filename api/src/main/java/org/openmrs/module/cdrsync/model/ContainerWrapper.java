package org.openmrs.module.cdrsync.model;

import org.openmrs.module.cdrsync.container.model.Container;

import java.util.List;

public class ContainerWrapper {
	
	private List<Container> containers;
	
	public ContainerWrapper(List<Container> containers) {
		this.containers = containers;
	}
	
	public List<Container> getContainers() {
		return containers;
	}
	
	public void setContainers(List<Container> containers) {
		this.containers = containers;
	}
}
