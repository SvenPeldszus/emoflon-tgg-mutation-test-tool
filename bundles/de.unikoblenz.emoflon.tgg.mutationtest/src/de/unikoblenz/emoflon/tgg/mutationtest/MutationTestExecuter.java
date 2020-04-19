package de.unikoblenz.emoflon.tgg.mutationtest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.emoflon.ibex.tgg.ide.admin.IbexTGGNature;
import org.gravity.eclipse.io.ExtensionFileVisitor;
import org.junit.runner.JUnitCore;
import org.moflon.core.build.MoflonBuildJob;
import org.moflon.tgg.mosl.tgg.TripleGraphGrammarFile;
import org.eclipse.debug.core.model.RuntimeProcess;

import de.unikoblenz.emoflon.tgg.mutationtest.util.CsvWriter;
import de.unikoblenz.emoflon.tgg.mutationtest.util.MutationTestConfiguration;

//TODO static instance, executeTests Parameters as constructor, static call to method to execute next iteration

public class MutationTestExecuter {

	public static MutationTestExecuter INSTANCE;

	// test setup variables

	private final IProject tggProject;

	private final ILaunchConfiguration launchConfigFile;

	private final Integer iterations;

	private final Integer timeout;

	// runtime helper variables
	
	private String projectName;

	private Integer iterationCount = 0;

	private Path tggFilePath = null;
	
	private Path projectPath;

	public MutationTestExecuter(MutationTestConfiguration mutationTestConfiguration) {
		this(mutationTestConfiguration.getProject(), mutationTestConfiguration.getLaunchConfig(),
				mutationTestConfiguration.getIterations(), mutationTestConfiguration.getTimeout());
	}

	public MutationTestExecuter(IProject tggProject, ILaunchConfiguration launchConfigFile, Integer iterations,
			Integer timeout) {
		this.tggProject = tggProject;
		this.launchConfigFile = launchConfigFile;
		this.iterations = iterations;
		this.timeout = timeout;
		this.projectPath = tggProject.getLocation().toFile().toPath();

		projectName = tggProject.getName();
		
		INSTANCE = this;
	}

	public void executeTests() {

		System.out.println("-----------------------");
		System.out.println("Wizard config:");
		System.out.println("project: " + tggProject);
		System.out.println("launch config: " + launchConfigFile);
		System.out.println("iterations: " + iterations);
		System.out.println("timeout: " + timeout);
		System.out.println("-----------------------");

		// TODO calculate possible mutation count so we don't keep on iterating at some
		// point while no more new mutations are possible

		TestResultCollector.INSTANCE.clearResultDataList();
	
		executeNextIteration();
	}

	void executeNextIteration() {
		restoreOriginalRuleFile();

		try {
			TGGRuleUtil tggRuleUtil = new TGGRuleUtil(tggProject);
			TripleGraphGrammarFile tggFile = null;

			tggFilePath = retrieveRandomTggFilePath(tggProject);
			System.out.println("Mutating file: " + tggFilePath.getFileName());

			tggFile = tggRuleUtil.loadRule(tggProject.getFile(tggFilePath.toString()));

			boolean isSuccess = tggRuleUtil.getMutantRule(tggFile);

			if (isSuccess) {
				createRuleFileBackup();
				
				// save new tgg data to the original tgg file
				System.out.println("Saving file");
				tggFile.eResource().save(Collections.emptyMap());

				// build the project with the new TGG file
				boolean buildSuccess = buildProject(tggProject);

				System.out.println("Starting tests..");
//				MutationTestRunListener.getInstance().setTestConfigName(launchConfigFile.getName());
				DebugUITools.launch(launchConfigFile, ILaunchManager.RUN_MODE);

				// TODO wait for the test launch to finish before retrieving

			} else {
				// TODO proper handling
				System.out.println("Unable to mutate any file");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean buildProject(IProject tggProject) throws InterruptedException {
		final Job job = new MoflonBuildJob(Arrays.asList(tggProject), IncrementalProjectBuilder.FULL_BUILD);
		job.setUser(true);
		job.schedule();
		job.join();

//		if (job.getResult().isOK()) {
//			return true;
//		} else {
		// re-try build if failed once
		Job retryJob = new MoflonBuildJob(Arrays.asList(tggProject), IncrementalProjectBuilder.FULL_BUILD);
		retryJob.schedule();
		retryJob.join();
		return retryJob.getResult().isOK();
//		}
	}

	private void createRuleFileBackup() {
		System.out.println("Creating backup");
		Path fileName = tggFilePath.getFileName();
		Path sourcePath = projectPath.resolve(tggFilePath);
		Path targetPath = sourcePath.resolveSibling(fileName + ".backup");
		System.out.println(sourcePath);
		try {
			Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO logger
			e.printStackTrace();
		}
	}

	void restoreOriginalRuleFile() {
		if (tggFilePath == null) {
			return;
		}
		System.out.println("--Restoring file");
		Path fileName = tggFilePath.getFileName();
		Path mutatedFilePath = projectPath.resolve(tggFilePath);
		Path backupFilePath = mutatedFilePath.resolveSibling(fileName + ".backup");

		if (backupFilePath.toFile().exists()) {
			try {
				Files.move(backupFilePath, mutatedFilePath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO logger
				e.printStackTrace();
			}
		} else {
			System.out.println("Info: Backup file does not exist.");
		}
	}

	private Path retrieveRandomTggFilePath(IProject testProject) throws CoreException {

		ExtensionFileVisitor tggFileVisitor = new ExtensionFileVisitor("tgg");
		Arrays.stream(JavaCore.create(testProject).getRawClasspath())
				.filter(classPathEntry -> classPathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
				.map(classPathEntry -> testProject.getFolder(classPathEntry.getPath().removeFirstSegments(1)))
				.forEach(projectFolder -> {
					try {
						projectFolder.accept(tggFileVisitor);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});

		int randomIndex = new Random().nextInt(tggFileVisitor.getFiles().size());
		Path tggFilePath = tggFileVisitor.getFiles().get(randomIndex);

		if (tggFilePath.isAbsolute()) {
			tggFilePath = testProject.getLocation().toFile().toPath().relativize(tggFilePath);
		}

		if (IbexTGGNature.SCHEMA_FILE.equals(tggFilePath.toString())) {
			return retrieveRandomTggFilePath(testProject);
		} else {
			return tggFilePath;
		}

	}
	

	public IProject getTggProject() {
		return tggProject;
	}

	public ILaunchConfiguration getLaunchConfigFile() {
		return launchConfigFile;
	}
	
	

}
