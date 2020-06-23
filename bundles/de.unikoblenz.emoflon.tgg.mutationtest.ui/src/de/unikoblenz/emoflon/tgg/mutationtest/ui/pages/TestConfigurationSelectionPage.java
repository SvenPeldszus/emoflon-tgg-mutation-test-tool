package de.unikoblenz.emoflon.tgg.mutationtest.ui.pages;

import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.unikoblenz.emoflon.tgg.mutationtest.ui.util.ConfigurationFileHandler;
import de.unikoblenz.emoflon.tgg.mutationtest.ui.util.WizardFlowControl;
import de.unikoblenz.emoflon.tgg.mutationtest.util.MutationTestConfiguration;
import de.unikoblenz.emoflon.tgg.mutationtest.util.MutationTestSerializableConfig;

public class TestConfigurationSelectionPage extends WizardPage {
	
	private ConfigurationFileHandler configurationFileHandler = new ConfigurationFileHandler();

	private MutationTestConfiguration configuration;

	private MutationTestConfiguration newConfiguration = new MutationTestConfiguration("New configuration", null, null,
			null, null, null, null);

	private WizardFlowControl flowControl;

	public TestConfigurationSelectionPage(WizardFlowControl flowControl) {
		super("Load configuration");
		setTitle("Load configuration");
		setDescription("Use a saved configuration or create a new one..");
		this.flowControl = flowControl;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		Label label1 = new Label(container, SWT.NONE);
		label1.setText("Select a configuration");

		setPageComplete(true);

		final ComboViewer viewer = new ComboViewer(container, SWT.READ_ONLY);

		viewer.setContentProvider(ArrayContentProvider.getInstance());

		/* if the current person is selected, show text */
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MutationTestConfiguration) {
					MutationTestConfiguration current = (MutationTestConfiguration) element;
					return current.getLabel();
				}
				return super.getText(element);
			}
		});

		/* within the selection event, tell the object it was selected */
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				MutationTestConfiguration current = (MutationTestConfiguration) selection.getFirstElement();
				configuration = current;
				flowControl.setNewConfig(current == newConfiguration);
			}
		});

		viewer.add(newConfiguration);
		viewer.setSelection(new StructuredSelection(viewer.getElementAt(0)), true);

		Set<MutationTestSerializableConfig> configs = configurationFileHandler.readConfigsFromFile();
		configs.stream().map(MutationTestConfiguration::new).forEach(viewer::add);

		// required to avoid an error in the system
		setControl(container);

	}

	public boolean isNewConfigurationSelected() {
		return (configuration == newConfiguration);
	}

	public MutationTestConfiguration getConfiguration() {
		return configuration;
	}

}
