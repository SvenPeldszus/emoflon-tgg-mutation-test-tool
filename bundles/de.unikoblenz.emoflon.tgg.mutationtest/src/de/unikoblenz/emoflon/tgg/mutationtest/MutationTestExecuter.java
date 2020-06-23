package de.unikoblenz.emoflon.tgg.mutationtest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
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
import org.gravity.eclipse.io.ExtensionFileVisitor;
import org.moflon.core.build.MoflonBuildJob;
import org.moflon.tgg.mosl.tgg.Rule;

import de.unikoblenz.emoflon.tgg.mutationtest.util.FileHandler;
import de.unikoblenz.emoflon.tgg.mutationtest.util.MutantResult;
import de.unikoblenz.emoflon.tgg.mutationtest.util.MutationTestConfiguration;

public class MutationTestExecuter {

	public static MutationTestExecuter INSTANCE;

	private static final Logger LOGGER = LogManager.getLogger(MutationTestExecuter.class);

	// test setup variables

	private final IProject tggProject;

	private final ILaunchConfiguration launchConfigFile;

	private final Integer testIterations;

	private final Integer timeout;

	private final Boolean skipInitialTests;

	private final Boolean createCsvOutput;

	// runtime helper variables

	private List<IFile> tggRuleFiles;

	private Integer iterationCount = 0;

	private MutantResult mutantResult;

	private TGGMutantRuleUtil mutantRuleUtil;

	private TGGRuleUtil tggRuleUtil;

	private long startTime;

	public MutationTestExecuter(MutationTestConfiguration mutationTestConfiguration) {
		this(mutationTestConfiguration.getProject(), mutationTestConfiguration.getLaunchConfig(),
				mutationTestConfiguration.getIterations(), mutationTestConfiguration.getTimeout(),
				mutationTestConfiguration.getSkipInitialTests(), mutationTestConfiguration.getCreateCsvOutput());
	}

	public MutationTestExecuter(IProject tggProject, ILaunchConfiguration launchConfigFile, Integer iterations,
			Integer timeout, Boolean skipInitialTests, Boolean createCsvOutput) {
		this.tggProject = tggProject;
		this.launchConfigFile = launchConfigFile;
		this.testIterations = iterations;
		this.timeout = timeout;
		this.skipInitialTests = skipInitialTests;
		this.createCsvOutput = createCsvOutput;

		INSTANCE = this;

		startTime = System.currentTimeMillis();
	}

	public void executeTests() {
		LOGGER.info("-----------------------");
		LOGGER.info("Wizard config:");
		LOGGER.info("project: " + tggProject);
		LOGGER.info("launch config: " + launchConfigFile);
		LOGGER.info("iterations: " + testIterations);
		LOGGER.info("timeout: " + timeout);
		LOGGER.info("-----------------------");

		TestResultCollector.INSTANCE.clearResultDataList();
		FileHandler.INSTANCE.prepareForNewRun();
		prepareTggRuleFileList();

		try {
			mutantRuleUtil = new TGGMutantRuleUtil();
		} catch (CoreException e) {
			LOGGER.error(e.getMessage(), e);
		}

		if (skipInitialTests) {
			LOGGER.info("Skipping initial test run");
			executeNextIteration();
		} else {
			LOGGER.info("Starting initial test run");
			runInitialTests();
		}
	}

	private void runInitialTests() {

		mutantResult = new MutantResult(null);
		mutantResult.setInitialRun(true);

		// TODO debug
		LOGGER.info("Starting build");
		boolean buildSuccess = buildProject();
		System.out.println("Build successful: " + buildSuccess);
		if (!buildSuccess) {
			LOGGER.info("Unable to build project. Aborting tests");
			return;
		}

		// TODO debug
		LOGGER.info("Starting initial tests.");
		DebugUITools.launch(launchConfigFile, ILaunchManager.RUN_MODE);
	}

	void executeNextIteration() {
		if (isTimeoutReached()) {
			TestResultCollector.INSTANCE.setFinishInformation("Timeout reached.");
			TestResultCollector.INSTANCE.finishProcessing();
		}
		LOGGER.info("Starting iteration: " + iterationCount);

		try {
			unloadTggRuleUtilResources();
			tggRuleUtil = new TGGRuleUtil(tggProject);

			List<Rule> rules = tggRuleUtil.loadRules(tggRuleFiles);

			mutantResult = mutantRuleUtil.getMutantRule(rules);

			if (mutantResult.isSuccess()) {
				FileHandler.INSTANCE.createRuleFileBackup();

				// save new tgg data to the original tgg file
				mutantResult.getMutantRule().eResource().save(Collections.emptyMap());

				// build the project with the new TGG file
				// TODO debug
				LOGGER.info("Starting build");
				boolean buildSuccess = buildProject();
				System.out.println(buildSuccess);
				if (!buildSuccess) {
					LOGGER.info("Unable to build project. Restoring file and retrying iteration.");
					FileHandler.INSTANCE.storeMutationFileForDebug();
					FileHandler.INSTANCE.restoreOriginalRuleFile();
					executeNextIteration();
				} else {
					// TODO debug
					LOGGER.info("Build successful");
					// TODO debug
					LOGGER.info("Starting tests..");
					iterationCount++;
					DebugUITools.launch(launchConfigFile, ILaunchManager.RUN_MODE);
				}
			} else {
				TestResultCollector.INSTANCE.setFinishInformation("No more mutations possible.");
				TestResultCollector.INSTANCE.finishProcessing();
			}

		} catch (IOException | CoreException e) {
			LOGGER.error(e.getMessage(), e);
			FileHandler.INSTANCE.restoreOriginalRuleFile();
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

//			if (job.getResult().isOK()) {
//				return true;
//			} else {
			// re-try build if failed once
			Job retryJob = new MoflonBuildJob(Arrays.asList(tggProject), IncrementalProjectBuilder.FULL_BUILD);
			retryJob.schedule();
			retryJob.join();
			return retryJob.getResult().isOK();
//			}
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
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

		tggRuleFiles = tggRuleFilePaths.stream().map(this::relativizeFilePath)
				.map(path -> tggProject.getFile(path.toString())).collect(Collectors.toList());
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
		return testIterations != 0 && iterationCount >= testIterations;
	}

	private boolean isTimeoutReached() {
		if (timeout == 0) {
			return false;
		}

		long currentTime = System.currentTimeMillis();

		long timeoutMillis = timeout * 60 * 1000;

		return timeoutMillis > currentTime - startTime;
	}

	public Boolean getSkipInitialTests() {
		return skipInitialTests;
	}

	public Boolean getCreateCsvOutput() {
		return createCsvOutput;
	}

}
