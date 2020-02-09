package de.unikoblenz.emoflon.tgg.mutationtest.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;

public class WizardHandler extends AbstractHandler{

	public void startWizard() {
		
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		WizardDialog wizardDialog = new WizardDialog(null, new MutationTestSetupWizard());
	    if (wizardDialog.open() == Window.OK) {
	        System.out.println("Ok pressed");
	    } else {
	        System.out.println("Cancel pressed");
	    }
		return null;
	}
}


