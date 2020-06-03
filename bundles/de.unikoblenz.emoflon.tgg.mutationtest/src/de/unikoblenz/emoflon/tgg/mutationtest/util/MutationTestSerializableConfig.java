package de.unikoblenz.emoflon.tgg.mutationtest.util;


public class MutationTestSerializableConfig {
	
	private String label;
	
	private String projectName;
	
	private String launchConfigName;
	
	private Integer iterations;
	
	private Integer timeout;
	
	private Boolean skipInitialTests;
	
	private Boolean createCsvOutput;

	public MutationTestSerializableConfig(String label, String projectName, String launchConfig, Integer iterations,
			Integer timeout, Boolean skipInitialTests,  Boolean createCsvOutput) {
		this.label = label;
		this.projectName = projectName;
		this.launchConfigName = launchConfig;
		this.iterations = iterations;
		this.timeout = timeout;
		this.skipInitialTests = skipInitialTests;
		this.createCsvOutput = createCsvOutput;
	}
	
	public MutationTestSerializableConfig(MutationTestConfiguration mutationTestConfiguration) {
		this.label = mutationTestConfiguration.getLabel();
		this.projectName = mutationTestConfiguration.getProject().getName();
		this.launchConfigName = mutationTestConfiguration.getLaunchConfig().getName();
		this.iterations = mutationTestConfiguration.getIterations();
		this.timeout = mutationTestConfiguration.getTimeout();
		this.skipInitialTests = mutationTestConfiguration.getSkipInitialTests();
		this.createCsvOutput = mutationTestConfiguration.getCreateCsvOutput();
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
