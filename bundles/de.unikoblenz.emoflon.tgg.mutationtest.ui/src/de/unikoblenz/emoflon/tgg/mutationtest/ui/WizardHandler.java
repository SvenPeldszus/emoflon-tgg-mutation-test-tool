package de.unikoblenz.emoflon.tgg.mutationtest.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.unikoblenz.emoflon.tgg.mutationtest.MutationTestExecuter;

public class WizardHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MutationTestSetupWizard mutationTestSetupWizard = new MutationTestSetupWizard();
		WizardDialog wizardDialog = new WizardDialog(null, mutationTestSetupWizard);
		if (wizardDialog.open() == Window.OK) {
			MutationTestExecuter mutationTestRunner = new MutationTestExecuter(mutationTestSetupWizard.getMutationTestConfiguration());
			mutationTestRunner.executeTests();
		}	
		return null;
	}
}
