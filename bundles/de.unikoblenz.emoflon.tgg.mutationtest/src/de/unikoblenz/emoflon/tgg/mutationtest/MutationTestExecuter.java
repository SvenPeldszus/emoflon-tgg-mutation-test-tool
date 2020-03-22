package de.unikoblenz.emoflon.tgg.mutationtest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Random;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.emoflon.ibex.tgg.ide.admin.IbexTGGNature;
import org.gravity.eclipse.io.ExtensionFileVisitor;
import org.junit.runner.JUnitCore;
import org.moflon.tgg.mosl.tgg.TripleGraphGrammarFile;

import de.unikoblenz.emoflon.tgg.mutationtest.util.MutationTestConfiguration;

//TODO ask: properly import instead of 'require bundle' in MANIFEST.MF

public class MutationTestExecuter {

	private MutationTestRunListener mutationTestRunListener;

	public MutationTestExecuter() {
		mutationTestRunListener = new MutationTestRunListener();
		JUnitCore core = new JUnitCore();
		core.addListener(mutationTestRunListener);
	}
	
	public void executeTests(MutationTestConfiguration mutationTestConfiguration) {
		executeTests(mutationTestConfiguration.getProject(), mutationTestConfiguration.getLaunchConfig(), mutationTestConfiguration.getIterations(), mutationTestConfiguration.getTimeout());
	}

	public void executeTests(IProject testProject, ILaunchConfiguration launchConfigFile, Integer iterations,
			Integer timeout) {

		System.out.println("-----------------------");
		System.out.println("Wizard config:");
		System.out.println("project: " + testProject);
		System.out.println("launch config: " + launchConfigFile);
		System.out.println("iterations: " + iterations);
		System.out.println("timeout: " + timeout);
		System.out.println("-----------------------");

		// TODO calculate possible mutation count so we don't keep on iterating at some
		// point while no more new mutations are possible
		TGGRuleUtil tggRuleUtil;
		try {
			tggRuleUtil = new TGGRuleUtil(testProject);

			Path tggFilePath = null;
			TripleGraphGrammarFile tggFile = null;
			Path projectPath = null;
			boolean isSuccess = false;

			while (!isSuccess) {

				tggFilePath = retrieveRandomTggFilePath(testProject);
				System.out.println("Mutating file: " + tggFilePath.getFileName());

				projectPath = testProject.getLocation().toFile().toPath();
				createRuleFileBackup(projectPath, tggFilePath);

				tggFile = tggRuleUtil.loadRule(testProject.getFile(tggFilePath.toString()));

//				isSuccess = tggRuleUtil.getMutantRule(tggFile);
				isSuccess = true;

				if (!isSuccess) {
					System.out.println("Unable to mutate. Trying different file");
					restoreOriginalRuleFile(projectPath, tggFilePath);
				}
			}

			if (isSuccess) {
				// TODO save new tgg data to the original tgg file
				System.out.println("Saving file");
				tggFile.eResource().save(Collections.emptyMap());

				// build the project with the new TGG file
				testProject.build(IncrementalProjectBuilder.FULL_BUILD, null);

				// TODO execute launch configuration
				System.out.println("Starting tests..");
				DebugUITools.launch(launchConfigFile, ILaunchManager.RUN_MODE);

				// TODO retrieve results

			} else {
				// TODO proper handling
				System.out.println("Unable to mutate any file");
			}

			restoreOriginalRuleFile(projectPath, tggFilePath);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	private void createRuleFileBackup(Path projectPath, Path tggFilePath) throws IOException {
		Path fileName = tggFilePath.getFileName();
		Path sourcePath = projectPath.resolve(tggFilePath);
		Path targetPath = sourcePath.resolveSibling(fileName + ".backup");
		Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
	}

	private void restoreOriginalRuleFile(Path projectPath, Path tggFilePath) throws IOException {
		System.out.println("--Restoring file");
		Path fileName = tggFilePath.getFileName();
		Path mutatedFilePath = projectPath.resolve(tggFilePath);
		Path backupFilePath = mutatedFilePath.resolveSibling(fileName + ".backup");

		if (backupFilePath.toFile().exists()) {
			Files.move(backupFilePath, mutatedFilePath, StandardCopyOption.REPLACE_EXISTING);
		} else {
			System.out.println("Info: Backup file does not exist.");
		}
	}

	private Path retrieveRandomTggFilePath(IProject testProject) throws CoreException {

		ExtensionFileVisitor tggFileVisitor = new ExtensionFileVisitor("tgg");
		testProject.accept(tggFileVisitor);
		Path projectPath = testProject.getLocation().toFile().toPath();

		int randomIndex = new Random().nextInt(tggFileVisitor.getFiles().size());
		Path tggFilePath = tggFileVisitor.getFiles().get(randomIndex);

		if (tggFilePath.isAbsolute()) {
			tggFilePath = projectPath.relativize(tggFilePath);
		}

		if (IbexTGGNature.SCHEMA_FILE.equals(tggFilePath.toString())) {
			return retrieveRandomTggFilePath(testProject);
		} else {
			return tggFilePath;
		}

	}

}
