package de.unikoblenz.emoflon.tgg.mutationtest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

public class MutationTestExecuter {
	
	public void executeTests(IProject testProject, IFile launchConfigFile, Integer iterations, Integer timeout) {
		System.out.println(testProject);
		System.out.println(launchConfigFile);
		System.out.println(iterations);
		System.out.println(timeout);
	}

}
