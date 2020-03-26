package de.unikoblenz.emoflon.tgg.mutationtest.ui.pages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.unikoblenz.emoflon.tgg.mutationtest.util.MutationTestSerializableConfig;

public class ProjectSelectionPage extends WizardPage {

	private Composite container;

	private IProject selectedProject;

	private Map<String, IProject> projectMap = new HashMap<>();

	private List projectListViewer;

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

		projectListViewer = new List(container, SWT.BORDER | SWT.SINGLE);

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = workspaceRoot.getProjects();

		// keep projects in a map for selection

		Arrays.asList(projects).stream().filter(this::hasTggNature).forEach(iproject -> projectMap.put(iproject.toString(), iproject));

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

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

	}

	private boolean hasTggNature(IProject project) {
		try {
			return project.hasNature("org.emoflon.ibex.tgg.ide.nature");
		} catch (CoreException e) {
			return false;
		}
	}

	public IProject getSelectedProject() {
		return selectedProject;
	}

}
