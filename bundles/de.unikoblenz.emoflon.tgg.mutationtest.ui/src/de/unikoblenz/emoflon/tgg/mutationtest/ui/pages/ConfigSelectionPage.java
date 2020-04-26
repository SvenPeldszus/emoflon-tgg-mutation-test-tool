package de.unikoblenz.emoflon.tgg.mutationtest.ui.pages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

public class ConfigSelectionPage extends WizardPage {

	private ILaunchConfiguration launchConfiguration;

	public ConfigSelectionPage() {
		super("Config Selection");
		setTitle("Config Selection");
		setDescription("Select the test launch configuration containing the tests for the selected project.");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;

		List configListViewer = new List(container, SWT.BORDER | SWT.SINGLE);
		configListViewer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType junitPluginTestType = manager
				.getLaunchConfigurationType("org.eclipse.pde.ui.JunitLaunchConfig");

		Map<String, ILaunchConfiguration> launchConfigMap = new HashMap<>();
		try {
			ILaunchConfiguration[] launchConfigurations = manager.getLaunchConfigurations(junitPluginTestType);

			Arrays.asList(launchConfigurations).stream().forEach(
					iLaunchConfiguration -> launchConfigMap.put(iLaunchConfiguration.toString(), iLaunchConfiguration));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		launchConfigMap.keySet().forEach(configListViewer::add);

		configListViewer.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				launchConfiguration = launchConfigMap.get(configListViewer.getSelection()[0]);
				setPageComplete(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				launchConfiguration = launchConfigMap.get(configListViewer.getSelection()[0]);
				setPageComplete(true);
			}
		});

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

	}

	public ILaunchConfiguration getLaunchConfiguration() {
		return launchConfiguration;
	}

}
