package de.unikoblenz.emoflon.tgg.mutationtest.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;

import de.unikoblenz.emoflon.tgg.mutationtest.MutationTestExecuter;

public class WizardHandler extends AbstractHandler {

	private MutationTestSetupWizard mutationTestSetupWizard = new MutationTestSetupWizard();

	private MutationTestExecuter mutationTestRunner = new MutationTestExecuter();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		WizardDialog wizardDialog = new WizardDialog(null, mutationTestSetupWizard);
		if (wizardDialog.open() == Window.OK) {
			mutationTestRunner.executeTests(mutationTestSetupWizard.getMutationTestConfiguration());
		}
		return null;
	}
}
