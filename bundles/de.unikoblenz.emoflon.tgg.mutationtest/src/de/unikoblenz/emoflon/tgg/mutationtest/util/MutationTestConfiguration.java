package de.unikoblenz.emoflon.tgg.mutationtest.util;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

public class MutationTestConfiguration {

	private String label;

	private IProject project;

	private ILaunchConfiguration launchConfig;

	private Integer iterations;

	private Integer timeout;

	public MutationTestConfiguration(String label, IProject project, ILaunchConfiguration launchConfig,
			Integer iterations, Integer timeout) {
		this.label = label;
		this.project = project;
		this.launchConfig = launchConfig;
		this.iterations = iterations;
		this.timeout = timeout;
	}

	public MutationTestConfiguration(MutationTestSerializableConfig serializableConfig) {
		this.label = serializableConfig.getLabel();

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		this.project = workspaceRoot.getProject(serializableConfig.getProjectName());

		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		try {
			this.launchConfig = 
					Arrays.asList(manager.getLaunchConfigurations()).stream()
					.filter(config -> config.getName().equals(serializableConfig.getLaunchConfigName())).findFirst().get();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.iterations = serializableConfig.getIterations();
		this.timeout = serializableConfig.getTimeout();

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
