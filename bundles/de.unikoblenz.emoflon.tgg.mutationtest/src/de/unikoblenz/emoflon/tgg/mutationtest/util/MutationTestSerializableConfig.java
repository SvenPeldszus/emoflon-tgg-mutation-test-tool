package de.unikoblenz.emoflon.tgg.mutationtest.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;


public class MutationTestSerializableConfig {
	
	private String label;
	
	private String projectName;
	
	private String launchConfigName;
	
	private Integer iterations;
	
	private Integer timeout;

	public MutationTestSerializableConfig(String label, String projectName, String launchConfig, Integer iterations,
			Integer timeout) {
		this.label = label;
		this.projectName = projectName;
		this.launchConfigName = launchConfig;
		this.iterations = iterations;
		this.timeout = timeout;
	}
	
	public MutationTestSerializableConfig(MutationTestConfiguration mutationTestConfiguration) {
		this.label = mutationTestConfiguration.getLabel();
		this.projectName = mutationTestConfiguration.getProject().getName();
		this.launchConfigName = mutationTestConfiguration.getLaunchConfig().getName();
		this.iterations = mutationTestConfiguration.getIterations();
		this.timeout = mutationTestConfiguration.getTimeout();
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getLaunchConfigName() {
		return launchConfigName;
	}

	public void setLaunchConfigName(String launchConfigName) {
		this.launchConfigName = launchConfigName;
	}

	public Integer getIterations() {
		return iterations;
	}

	public void setIterations(Integer iterations) {
		this.iterations = iterations;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	
	

}
