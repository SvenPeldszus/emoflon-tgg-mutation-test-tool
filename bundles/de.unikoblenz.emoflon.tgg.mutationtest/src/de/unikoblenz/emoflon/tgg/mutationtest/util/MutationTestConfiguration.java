package de.unikoblenz.emoflon.tgg.mutationtest.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;


public class MutationTestConfiguration {
	
	private String label;
	
	private IProject project;
	
	private ILaunchConfiguration launchConfig;
	
	private Integer iterations;
	
	private Integer timeout;

	public MutationTestConfiguration(String label, IProject project, ILaunchConfiguration launchConfig, Integer iterations,
			Integer timeout) {
		this.label = label;
		this.project = project;
		this.launchConfig = launchConfig;
		this.iterations = iterations;
		this.timeout = timeout;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public ILaunchConfiguration getLaunchConfig() {
		return launchConfig;
	}

	public void setLaunchConfig(ILaunchConfiguration launchConfig) {
		this.launchConfig = launchConfig;
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
