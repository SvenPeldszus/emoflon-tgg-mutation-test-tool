package de.unikoblenz.emoflon.tgg.mutationtest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.emoflon.ibex.tgg.ide.admin.IbexTGGNature;
import org.gravity.eclipse.io.ExtensionFileVisitor;
import org.junit.runner.JUnitCore;
import org.moflon.tgg.mosl.tgg.TripleGraphGrammarFile;

import de.unikoblenz.emoflon.tgg.mutationtest.util.MutationTestConfiguration;

//TODO ask: properly import instead of 'require bundle' in MANIFEST.MF

public class MutationTestExecuter {

//	private MutationTestRunListener mutationTestRunListener;

//	public MutationTestExecuter() {
//		mutationTestRunListener = new MutationTestRunListener();
//		JUnitCore core = new JUnitCore();
//		core.addListener(mutationTestRunListener);
//	}
	
	public void executeTests(MutationTestConfiguration mutationTestConfiguration) {
		executeTests(mutationTestConfiguration.getProject(), mutationTestConfiguration.getLaunchConfig(), mutationTestConfiguration.getIterations(), mutationTestConfiguration.getTimeout());
	}

	public void executeTests(IProject tggProject, ILaunchConfiguration launchConfigFile, Integer iterations,
			Integer timeout) {

		System.out.println("-----------------------");
		System.out.println("Wizard config:");
		System.out.println("project: " + tggProject);
		System.out.println("launch config: " + launchConfigFile);
		System.out.println("iterations: " + iterations);
		System.out.println("timeout: " + timeout);
		System.out.println("-----------------------");

		// TODO calculate possible mutation count so we don't keep on iterating at some
		// point while no more new mutations are possible
		TGGRuleUtil tggRuleUtil;
		try {
			tggRuleUtil = new TGGRuleUtil(tggProject);

			Path tggFilePath = null;
			TripleGraphGrammarFile tggFile = null;
			Path projectPath = null;
			boolean isSuccess = false;

			while (!isSuccess) {

				tggFilePath = retrieveRandomTggFilePath(tggProject);
				System.out.println("Mutating file: " + tggFilePath.getFileName());

				projectPath = tggProject.getLocation().toFile().toPath();
				
				tggFile = tggRuleUtil.loadRule(tggProject.getFile(tggFilePath.toString()));

				isSuccess = tggRuleUtil.getMutantRule(tggFile);
//				isSuccess = true;

				if (!isSuccess) {
					System.out.println("Unable to mutate. Trying different file");
				} else {
					createRuleFileBackup(projectPath, tggFilePath);
				}
			}

			if (isSuccess) {
				// TODO save new tgg data to the original tgg file
				System.out.println("Saving file");
				tggFile.eResource().save(Collections.emptyMap());
				
				// build the project with the new TGG file
				tggProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
//
//				// TODO execute launch configuration
				System.out.println("Starting tests..");
//				DebugUITools.buildAndLaunch(launchConfigFile, ILaunchManager.RUN_MODE, createBuildProgressMonitor());
				DebugUITools.launch(launchConfigFile, ILaunchManager.RUN_MODE);

				// TODO retrieve results

			} else {
				// TODO proper handling
				System.out.println("Unable to mutate any file");
			}

//			restoreOriginalRuleFile(projectPath, tggFilePath);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	private IProgressMonitor createBuildProgressMonitor() {
		return new IProgressMonitor() {
			
			@Override
			public void worked(int work) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void subTask(String name) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void setTaskName(String name) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void setCanceled(boolean value) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public boolean isCanceled() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void internalWorked(double work) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void done() {
				// TODO Auto-generated method stub
				System.out.println("build done");
			}
			
			@Override
			public void beginTask(String name, int totalWork) {
				// TODO Auto-generated method stub
			}
		};
	}

	private void createRuleFileBackup(Path projectPath, Path tggFilePath) throws IOException {
		System.out.println("Creating backup");
		Path fileName = tggFilePath.getFileName();
		Path sourcePath = projectPath.resolve(tggFilePath);
		Path targetPath = sourcePath.resolveSibling(fileName + ".backup");
		System.out.println(sourcePath.toFile().canRead());
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
		List<IClasspathEntry> classPathEntries = Arrays.asList(JavaCore.create(testProject).getRawClasspath()).stream()
				.filter(i -> i.getEntryKind() == IClasspathEntry.CPE_SOURCE)
				.collect(Collectors.toList());
		
		testProject.getFolder(classPathEntries.get(0).getPath()).accept(tggFileVisitor);
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
