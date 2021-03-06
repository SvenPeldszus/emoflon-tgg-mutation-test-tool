package de.unikoblenz.emoflon.tgg.mutationtest.ui.pages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class ProjectSelectionPage extends WizardPage {

	private static final String TGG_NATURE_IDENTIFIER = "org.emoflon.ibex.tgg.ide.nature";
	private IProject selectedProject;

	public ProjectSelectionPage() {
		super("Project Selection");
		setTitle("Project Selection");
		setDescription("Select the tgg project that should be tested");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;

		List projectListViewer = new List(container, SWT.BORDER | SWT.SINGLE);
		projectListViewer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = workspaceRoot.getProjects();

		// keep projects in a map for selection
		Map<String, IProject> projectMap = new HashMap<>();
		Arrays.asList(projects).stream().filter(this::hasTggNature)
				.forEach(iproject -> projectMap.put(iproject.toString(), iproject));

		// TODO filter projects for tgg projects
		projectMap.keySet().forEach(projectListViewer::add);

		projectListViewer.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				selectedProject = projectMap.get(projectListViewer.getSelection()[0]);
				setPageComplete(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				selectedProject = projectMap.get(projectListViewer.getSelection()[0]);
				setPageComplete(true);
			}
		});

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

	}

	private boolean hasTggNature(IProject project) {
		try {
			return project.hasNature(TGG_NATURE_IDENTIFIER);
		} catch (CoreException e) {
			return false;
		}
	}

	public IProject getSelectedProject() {
		return selectedProject;
	}

}
