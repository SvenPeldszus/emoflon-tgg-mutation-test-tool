package de.unikoblenz.emoflon.tgg.mutationtest.ui.pages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class TestConfigurationSelectionPage extends WizardPage{
	
    private Composite container;

    public TestConfigurationSelectionPage() {
        super("Load configuration");
        setTitle("Load configuration");
        setDescription("Use an existing configuration or create a new one..");
    }

    @Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 1;
        Label label1 = new Label(container, SWT.NONE);
        label1.setText("Select a configuration");

        Combo comboBox = new Combo(container, SWT.SINGLE);
        
        comboBox.add("new configuration");
        comboBox.select(0);
        setPageComplete(true);
        
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);

        
        
        // required to avoid an error in the system
        setControl(container);
//        setPageComplete(false);

    }

}
