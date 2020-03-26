package de.unikoblenz.emoflon.tgg.mutationtest.ui.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ConfigCreationInputPage extends WizardPage {

	private Text configName;

	private boolean saveConfig;

	public ConfigCreationInputPage() {
		super("Configuration details");
		setTitle("Configuration details");
		setDescription("Provide more information on the configuration");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;

		Button checkbox = new Button(container, SWT.CHECK);
		checkbox.setText("Save config");
		checkbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				saveConfig = ((Button) event.getSource()).getSelection();
			}
		});

		new Label(container, SWT.NONE);
		Label label1 = new Label(container, SWT.NONE);
		label1.setText("Config name");

		configName = new Text(container, SWT.NONE);
		configName.setText("");

		setControl(container);
		setPageComplete(true);
	}

	public Text getConfigName() {
		return configName;
	}

	public boolean isSaveConfig() {
		return saveConfig;
	}

}
