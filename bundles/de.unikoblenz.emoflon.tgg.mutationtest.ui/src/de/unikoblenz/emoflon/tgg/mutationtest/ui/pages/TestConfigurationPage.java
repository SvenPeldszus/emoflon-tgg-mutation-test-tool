package de.unikoblenz.emoflon.tgg.mutationtest.ui.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class TestConfigurationPage extends WizardPage{
	
    private Text iterations;
    
    private Text timeout;
    
    private Button skipInitial;
    
    private Button saveCsvData;

    public TestConfigurationPage() {
        super("Configuration");
        setTitle("Configuration");
        setDescription("Add further configuration");
    }

    @Override
    public void createControl(Composite parent) {
    	Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        
        Label iterationsLabel = new Label(container, SWT.NONE);
        iterationsLabel.setText("iterations");

        iterations = new Text(container, SWT.NONE);
        iterations.setText("");
        
        Label timeoutLabel = new Label(container, SWT.NONE);
        timeoutLabel.setText("timeout");
        
        timeout = new Text(container, SWT.NONE);
        timeout.setText("");
        
        Label skipLabel = new Label(container, SWT.NONE);
        skipLabel.setText("skip initial tests?");
        skipInitial = new Button(container, SWT.CHECK);
        
        Label saveCsvLabel = new Label(container, SWT.NONE);
        saveCsvLabel.setText("save test data as CSV?");
        saveCsvData = new Button(container, SWT.CHECK);
        
        
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

	public String getIterations() {
		return iterations.getText();
	}

	public String getTimeout() {
		return timeout.getText();
	}

	public Boolean getSkipInitial() {
		return skipInitial.getSelection();
	}

	public Boolean getSaveCsvData() {
		return saveCsvData.getSelection();
	}
}
