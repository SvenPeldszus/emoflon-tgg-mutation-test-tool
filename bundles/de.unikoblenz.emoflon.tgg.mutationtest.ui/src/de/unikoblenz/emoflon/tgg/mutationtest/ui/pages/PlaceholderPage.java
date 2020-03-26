package de.unikoblenz.emoflon.tgg.mutationtest.ui.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class PlaceholderPage extends WizardPage {

	public PlaceholderPage() {
		super("Placeholder");
		setTitle("Placeholder");
//		setDescription("Provide more information on the configuration");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		setControl(container);
		setPageComplete(true);
	}

}
