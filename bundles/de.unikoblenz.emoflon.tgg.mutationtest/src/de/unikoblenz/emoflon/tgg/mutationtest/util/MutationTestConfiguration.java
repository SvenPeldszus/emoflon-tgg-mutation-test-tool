package de.unikoblenz.emoflon.tgg.mutationtest.util;

import java.util.Arrays;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

public class MutationTestConfiguration {
	
	private static final Logger LOGGER = Logger.getLogger(MutationTestConfiguration.class);

	private String label;

	private IProject project;

	private ILaunchConfiguration launchConfig;

	private Integer iterations;

	private Integer timeout;
	
	private Boolean skipInitialTests;
	
	private Boolean createCsvOutput;

	public MutationTestConfiguration(String label, IProject project, ILaunchConfiguration launchConfig,
			Integer iterations, Integer timeout, Boolean skipInitialTests, Boolean createCsvOutput) {
		this.label = label;
		this.project = project;
		this.launchConfig = launchConfig;
		this.iterations = iterations;
		this.timeout = timeout;
		this.skipInitialTests = skipInitialTests;
		this.createCsvOutput = createCsvOutput;
	}

	public MutationTestConfiguration(MutationTestSerializableConfig serializableConfig) {
		this.label = serializableConfig.getLabel();

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		this.project = workspaceRoot.getProject(serializableConfig.getProjectName());

		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		
		try {
			Optional<ILaunchConfiguration> launchConfigFile = Arrays.asList(manager.getLaunchConfigurations()).stream()
			.filter(config -> config.getName().equals(serializableConfig.getLaunchConfigName())).findFirst();
			
			if(launchConfigFile.isPresent()) {
				this.launchConfig = launchConfigFile.get();
			} else {
				LOGGER.info("Launch configuration not found: " + serializableConfig.getLaunchConfigName());
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		this.iterations = serializableConfig.getIterations();
		this.timeout = serializableConfig.getTimeout();
		this.skipInitialTests = serializableConfig.getSkipInitialTests();
		this.createCsvOutput = serializableConfig.getCreateCsvOutput();
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

	public Boolean getSkipInitialTests() {
		return skipInitialTests;
	}

	public void setSkipInitialTests(Boolean skipInitialTests) {
		this.skipInitialTests = skipInitialTests;
	}

	public Boolean getCreateCsvOutput() {
		return createCsvOutput;
	}

	public void setCreateCsvOutput(Boolean createCsvOutput) {
		this.createCsvOutput = createCsvOutput;
	}

}
