package de.unikoblenz.emoflon.tgg.mutationtest.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.unikoblenz.emoflon.tgg.mutationtest.MutationTestExecuter;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.ConfigCreationInputPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.ConfigSelectionPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.PlaceholderPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.ProjectSelectionPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.TestConfigurationPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.TestConfigurationSelectionPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.util.WizardFlowControl;
import de.unikoblenz.emoflon.tgg.mutationtest.util.MutationTestConfiguration;
import de.unikoblenz.emoflon.tgg.mutationtest.util.MutationTestSerializableConfig;

public class MutationTestSetupWizard extends Wizard {

	public MutationTestSetupWizard() {
	}

	private WizardFlowControl flowControl = new WizardFlowControl();

	private TestConfigurationSelectionPage testConfigSelectionPage = new TestConfigurationSelectionPage(flowControl);

	private PlaceholderPage placeholderPage = new PlaceholderPage();

	private ConfigCreationInputPage configCreationInputPage = new ConfigCreationInputPage();

	private ProjectSelectionPage projectSelectionPage = new ProjectSelectionPage();

	private ConfigSelectionPage configSelectionPage = new ConfigSelectionPage();

	private TestConfigurationPage testConfigurationPage = new TestConfigurationPage();

	private MutationTestExecuter mutationTestRunner = new MutationTestExecuter();

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

			Integer iterations = Integer.valueOf(testConfigurationPage.getIterations().getText());

			Integer timeout = Integer.valueOf(testConfigurationPage.getTimeout().getText());

			MutationTestConfiguration configuration = new MutationTestConfiguration(configName, testProject,
					launchConfigFile, iterations, timeout);

			MutationTestSerializableConfig serializableConfig = new MutationTestSerializableConfig(configuration);
			
			if (configCreationInputPage.isSaveConfig()) {
				Gson gson = new Gson();

				String jsonFile = System.getProperty("user.home") + File.separator + "config.json";

				Set<MutationTestSerializableConfig> configs = new HashSet<>();

				if (new File(jsonFile).exists()) {
					try {
						BufferedReader br = new BufferedReader(new FileReader(jsonFile));

						configs = gson.fromJson(br, new TypeToken<HashSet<MutationTestSerializableConfig>>() {
						}.getType());
						br.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				configs.add(serializableConfig);

				String json = gson.toJson(configs);
				try {
					FileWriter writer = new FileWriter(jsonFile);
					writer.write(json);
					writer.close();

				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			mutationTestRunner.executeTests(testProject, launchConfigFile, iterations, timeout);
		} else {
			mutationTestRunner.executeTests(testConfigSelectionPage.getConfiguration());
		}
		return true;
	}

}
