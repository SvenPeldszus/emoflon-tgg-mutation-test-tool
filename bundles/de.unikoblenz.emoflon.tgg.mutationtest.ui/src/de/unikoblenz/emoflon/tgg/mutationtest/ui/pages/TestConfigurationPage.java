package de.unikoblenz.emoflon.tgg.mutationtest.ui.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class TestConfigurationPage extends WizardPage{
	
    private Composite container;
    
    private Text iterations;
    
    private Text timeout;

    public TestConfigurationPage() {
        super("Configuration");
        setTitle("Configuration");
        setDescription("Add further configuration");
    }

    @Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        
        Label label1 = new Label(container, SWT.NONE);
        label1.setText("iterations");

        iterations = new Text(container, SWT.NONE);
        iterations.setText("");
        
        Label label2 = new Label(container, SWT.NONE);
        label2.setText("timeout");
        
        timeout = new Text(container, SWT.NONE);
        timeout.setText("");
        
        
        timeout.addModifyListener(modifyEvent -> {
        	setPageComplete(!timeout.getText().isEmpty() && !iterations.getText().isEmpty());
        });
        
        iterations.addModifyListener(modifyEvent -> {
        	setPageComplete(!timeout.getText().isEmpty() && !iterations.getText().isEmpty());
        });
        
        // required to avoid an error in the system
        setControl(container);
        setPageComplete(false);
    }

	public Text getIterations() {
		return iterations;
	}

	public Text getTimeout() {
		return timeout;
	}
}
