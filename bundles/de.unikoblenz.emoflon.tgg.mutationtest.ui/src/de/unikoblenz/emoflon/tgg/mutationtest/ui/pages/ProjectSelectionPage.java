package de.unikoblenz.emoflon.tgg.mutationtest.ui.pages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class ProjectSelectionPage extends WizardPage{
	
    private Composite container;
    
    private IProject selectedProject;

    public ProjectSelectionPage() {
        super("Project Selection");
        setTitle("Project Selection");
        setDescription("Select a project to proceed..");
    }

    @Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 1;
        Label label1 = new Label(container, SWT.NONE);
        label1.setText("Select a project..");

        ListViewer projectListViewer = new ListViewer(container, SWT.BORDER | SWT.SINGLE);
        
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = workspaceRoot.getProjects();
        
        //TODO filter projects for tgg projects
        projectListViewer.add(projects);
        
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);

        projectListViewer.addSelectionChangedListener(
        		selection -> {
        			if(selection.getSelection() != null) {
        				selectedProject = (IProject) selection.getSelection();
        				setPageComplete(true);
        			}
        		});
        
        // required to avoid an error in the system
        setControl(container);
        setPageComplete(false);

    }

	public IProject getSelectedProject() {
		return selectedProject;
	}
    
}
