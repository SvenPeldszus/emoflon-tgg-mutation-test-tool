package de.unikoblenz.emoflon.tgg.mutationtest.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import de.unikoblenz.emoflon.tgg.mutationtest.MutationTestExecuter;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.ConfigSelectionPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.ProjectSelectionPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.TestConfigurationPage;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.pages.TestConfigurationSelectionPage;

public class MutationTestSetupWizard extends Wizard {

	public MutationTestSetupWizard() {
	}

	private TestConfigurationSelectionPage testConfigSelectionPage = new TestConfigurationSelectionPage();

	private ProjectSelectionPage projectSelectionPage = new ProjectSelectionPage();

	private ConfigSelectionPage configSelectionPage = new ConfigSelectionPage();

	private TestConfigurationPage testConfigurationPage = new TestConfigurationPage();

	private MutationTestExecuter mutationTestRunner = new MutationTestExecuter();

	@Override
	public void addPages() {
		addPage(testConfigSelectionPage);
		addPage(projectSelectionPage);
		addPage(configSelectionPage);
		addPage(testConfigurationPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage currentPage) {
//	    if (todo.isDone()) {
//	       return specialPage;
//	    }
		if (currentPage == testConfigSelectionPage) {
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
		// collect data
		IProject testProject = projectSelectionPage.getSelectedProject();
		
		IFile launchConfigFile = configSelectionPage.getLaunchConfigFile();
		
		Integer iterations = Integer.valueOf(testConfigurationPage.getIterations().getText());
		
		Integer timeout = Integer.valueOf(testConfigurationPage.getTimeout().getText());

		//call mutation test runner
		mutationTestRunner.executeTests(testProject, launchConfigFile, iterations, timeout);
		return true;
	}

}
