package de.unikoblenz.emoflon.tgg.mutationtest.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.ConfigCreationInputPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.LaunchConfigSelectionPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.PlaceholderPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.ProjectSelectionPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.TestConfigurationPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.TestConfigurationSelectionPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.util.ConfigurationFileHandler;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.util.WizardFlowControl;
import de.unikoblenz.emoflon.tgg.mutationtest.util.MutationTestConfiguration;

public class MutationTestSetupWizard extends Wizard {

	private WizardFlowControl flowControl = new WizardFlowControl();

	private TestConfigurationSelectionPage testConfigSelectionPage = new TestConfigurationSelectionPage(flowControl);

	private PlaceholderPage placeholderPage = new PlaceholderPage();

	private ConfigCreationInputPage configCreationInputPage = new ConfigCreationInputPage();

	private ProjectSelectionPage projectSelectionPage = new ProjectSelectionPage();

	private LaunchConfigSelectionPage configSelectionPage = new LaunchConfigSelectionPage();

	private TestConfigurationPage testConfigurationPage = new TestConfigurationPage();

	private MutationTestConfiguration mutationTestConfiguration;
	
	private ConfigurationFileHandler configurationFileHandler = new ConfigurationFileHandler();

	@Override
	public void addPages() {
		addPage(testConfigSelectionPage);
		addPage(configCreationInputPage);
		addPage(placeholderPage);
		addPage(projectSelectionPage);
		addPage(configSelectionPage);
		addPage(testConfigurationPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage currentPage) {
		if (!flowControl.isNewConfig()) {
			configCreationInputPage.setPageComplete(true);
			projectSelectionPage.setPageComplete(true);
			configSelectionPage.setPageComplete(true);
			testConfigurationPage.setPageComplete(true);
			return placeholderPage;
		}

		if (currentPage == testConfigSelectionPage) {
			return configCreationInputPage;
		}
		if (currentPage == configCreationInputPage) {
			return projectSelectionPage;
		}
		if (currentPage == projectSelectionPage) {
			return configSelectionPage;
		}
		if (currentPage == configSelectionPage) {
			return testConfigurationPage;
		}
		return null;
	}

	@Override
	public boolean performFinish() {
		if (testConfigSelectionPage.isNewConfigurationSelected()) {
			// collect data
			String configName = configCreationInputPage.getConfigName().getText();
			IProject testProject = projectSelectionPage.getSelectedProject();
			ILaunchConfiguration launchConfigFile = configSelectionPage.getLaunchConfiguration();
			Integer iterations = Integer.valueOf(testConfigurationPage.getIterations());
			Integer timeout = Integer.valueOf(testConfigurationPage.getTimeout());
			Boolean skipInitial = testConfigurationPage.getSkipInitial();
			Boolean saveCsvData = testConfigurationPage.getSaveCsvData();

			MutationTestConfiguration configuration = new MutationTestConfiguration(configName, testProject,
					launchConfigFile, iterations, timeout, skipInitial, saveCsvData);

			if (configCreationInputPage.isSaveConfig()) {
				configurationFileHandler.saveConfigurationToJsonFile(configuration);
			}

			mutationTestConfiguration = configuration;
		} else {
			mutationTestConfiguration = testConfigSelectionPage.getConfiguration();
		}
		return true;
	}

	public MutationTestConfiguration getMutationTestConfiguration() {
		return mutationTestConfiguration;
	}

}
