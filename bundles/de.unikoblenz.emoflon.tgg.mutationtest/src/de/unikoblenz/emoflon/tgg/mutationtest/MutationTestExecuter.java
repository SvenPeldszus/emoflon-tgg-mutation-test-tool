package de.unikoblenz.emoflon.tgg.mutationtest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.emoflon.ibex.tgg.ide.admin.IbexTGGNature;
import org.gravity.eclipse.io.ExtensionFileVisitor;
import org.moflon.core.build.MoflonBuildJob;
import org.moflon.tgg.mosl.tgg.Rule;
import de.unikoblenz.emoflon.tgg.mutationtest.util.MutantResult;
import de.unikoblenz.emoflon.tgg.mutationtest.util.MutationTestConfiguration;

public class MutationTestExecuter {

	public static MutationTestExecuter INSTANCE;

	private static final Logger LOGGER = Logger.getLogger(MutationTestExecuter.class);

	// test setup variables

	private final IProject tggProject;

	private final ILaunchConfiguration launchConfigFile;

	private final Integer testIterations;

	private final Integer timeout;

	// runtime helper variables

	private String projectName;

	private List<IFile> tggRuleFiles;

	private Integer iterationCount = 0;

	private Path projectPath;

	private MutantResult mutantResult;

	private TGGRuleUtil tggRuleUtil;

	public MutationTestExecuter(MutationTestConfiguration mutationTestConfiguration) {
		this(mutationTestConfiguration.getProject(), mutationTestConfiguration.getLaunchConfig(),
				mutationTestConfiguration.getIterations(), mutationTestConfiguration.getTimeout());
	}

	public MutationTestExecuter(IProject tggProject, ILaunchConfiguration launchConfigFile, Integer iterations,
			Integer timeout) {
		this.tggProject = tggProject;
		this.launchConfigFile = launchConfigFile;
		this.testIterations = iterations;
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
		System.out.println("iterations: " + testIterations);
		System.out.println("timeout: " + timeout);
		System.out.println("-----------------------");

		TestResultCollector.INSTANCE.clearResultDataList();

		prepareTggRuleFileList();

		runInitialTests();
	}

	private void runInitialTests() {
		mutantResult = new MutantResult(null);
		mutantResult.setInitialRun(true);

		System.out.println("Starting build");
		boolean buildSuccess = buildProject();
		System.out.println("build success: " + buildSuccess);
		if (!buildSuccess) {
			System.out.println("Unable to build project. Aborting tests");
			return;
		}

		System.out.println("Starting tests..");
		DebugUITools.launch(launchConfigFile, ILaunchManager.RUN_MODE);
	}

	void executeNextIteration() {
		iterationCount++;
		restoreOriginalRuleFile();

		try {
			unloadTggRuleUtilResources();
			tggRuleUtil = new TGGRuleUtil(tggProject);

			List<Rule> rules = tggRuleUtil.loadRules(tggRuleFiles);

			mutantResult = tggRuleUtil.getMutantRule(rules);

			if (mutantResult.isSuccess()) {

				// TODO check if all mutations have been done (mutantresult) --> remove from
				// list

				createRuleFileBackup();

				// save new tgg data to the original tgg file
				System.out.println("Saving file");
				mutantResult.getMutantRule().eResource().save(Collections.emptyMap());

				// build the project with the new TGG file
				System.out.println("Starting build");
				boolean buildSuccess = buildProject();
				System.out.println(buildSuccess);
				if (!buildSuccess) {
					System.out.println("Unable to build project. ");
				}

				System.out.println("Starting tests..");
				DebugUITools.launch(launchConfigFile, ILaunchManager.RUN_MODE);

			} else {
				// TODO proper handling
				System.out.println("Unable to mutate any rule in file");
			}

		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (CoreException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void unloadTggRuleUtilResources() {
		if (tggRuleUtil != null) {
			tggRuleUtil.unloadResources();
		}
	}

	private boolean buildProject() {
		try {
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
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
	}

	private void createRuleFileBackup() {
		System.out.println("Creating backup");
		Path filePath = Paths.get(mutantResult.getMutantRule().eResource().getURI().path());
		Path sourcePath = projectPath.resolve(filePath);
		Path fileName = filePath.getFileName();
		Path targetPath = sourcePath.resolveSibling(fileName + ".backup");
		System.out.println(sourcePath);
		try {
			Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	void restoreOriginalRuleFile() {
		if (mutantResult == null || isInitialTestRun()) {
			return;
		}
		System.out.println("--Restoring file");

		Path filePath = Paths.get(mutantResult.getMutantRule().eResource().getURI().path());
		Path fileName = filePath.getFileName();
		Path mutatedFilePath = projectPath.resolve(filePath);
		Path backupFilePath = mutatedFilePath.resolveSibling(fileName + ".backup");

		if (backupFilePath.toFile().exists()) {
			try {
				Files.move(backupFilePath, mutatedFilePath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		} else {
			System.out.println("Info: Backup file does not exist.");
		}
	}

	private boolean isInitialTestRun() {
		return "initialRunWithoutMutation".equals(mutantResult.getMutationName());
	}

	private void prepareTggRuleFileList() {
		List<Path> tggRuleFilePaths = new ArrayList<>();
		try {
			ExtensionFileVisitor tggFileVisitor = new ExtensionFileVisitor("tgg");
			Arrays.stream(JavaCore.create(tggProject).getRawClasspath())
					.filter(classPathEntry -> classPathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
					.map(classPathEntry -> tggProject.getFolder(classPathEntry.getPath().removeFirstSegments(1)))
					.forEach(projectFolder -> {
						try {
							projectFolder.accept(tggFileVisitor);
						} catch (CoreException e) {
							LOGGER.error(e.getMessage(), e);
						}
					});

			tggRuleFilePaths = tggFileVisitor.getFiles();
		} catch (CoreException e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		tggRuleFiles = tggRuleFilePaths.stream()
		.map(this::relativizeFilePath)
		.map(path -> tggProject.getFile(path.toString()))
		.collect(Collectors.toList());
	}

	private Path relativizeFilePath(Path tggFilePath) {
		if (tggFilePath.isAbsolute()) {
			tggFilePath = tggProject.getLocation().toFile().toPath().relativize(tggFilePath);
		}
		return tggFilePath;
	}

	public IProject getTggProject() {
		return tggProject;
	}

	public ILaunchConfiguration getLaunchConfigFile() {
		return launchConfigFile;
	}

	public MutantResult getMutantResult() {
		return mutantResult;
	}

	public boolean isFinished() {
		return iterationCount >= testIterations;
	}

}
