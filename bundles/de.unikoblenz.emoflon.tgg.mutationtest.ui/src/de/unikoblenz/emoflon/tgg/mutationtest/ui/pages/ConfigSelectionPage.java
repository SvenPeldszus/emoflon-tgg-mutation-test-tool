package de.unikoblenz.emoflon.tgg.mutationtest.ui.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class ConfigSelectionPage extends WizardPage{
	
    private Composite container;
    
    private ILaunchConfiguration launchConfiguration;

    public ConfigSelectionPage() {
        super("Config Selection");
        setTitle("Config Selection");
        setDescription("Select a launch config..");
    }

    @Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 1;

        Label label1 = new Label(container, SWT.NONE);
        label1.setText("Select a launch config..");

        ListViewer projectListViewer = new ListViewer(container, SWT.BORDER | SWT.SINGLE);
        
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        
//        processContainer(workspaceRoot).stream()
//        	.filter(Objects::nonNull)
//        	.filter(file -> file.getFileExtension().equalsIgnoreCase("launch"))
//        	.forEach(projectListViewer::add);
        
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type =     manager.getLaunchConfigurationType("org.eclipse.cdt.launch.applicationLaunchType");
        ILaunchConfiguration[] launchConfigurations;
        try {
        	launchConfigurations = manager.getLaunchConfigurations(type);
        	
        	Arrays.asList(launchConfigurations).stream()
        	.forEach(projectListViewer::add);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);

        projectListViewer.addSelectionChangedListener(
        		selection -> {
        			if(selection.getSelection() != null) {
        				launchConfiguration = (ILaunchConfiguration) selection.getSelection();
        				setPageComplete(true);
        			}
        		});
        
        // required to avoid an error in the system
        setControl(container);
        setPageComplete(false);

    }
    
    private ArrayList<IFile> processContainer(IContainer container){
    	try {
    	ArrayList<IFile> files = new ArrayList<>();
    	IResource [] members = container.members();
    	for (IResource member : members) {
    		if (member instanceof IContainer) {
    			files.addAll(processContainer((IContainer)member));
    		} else if (member instanceof IFile) {
    			files.add((IFile)member);
    		}
        }
    	return files;
    	} catch(CoreException e) {
    		return null;
    	}
    }

	public ILaunchConfiguration getLaunchConfiguration() {
		return launchConfiguration;
	}

}
