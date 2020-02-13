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
import org.emoflon.ibex.tgg.ide.admin.IbexTGGNature;
import org.gravity.eclipse.io.ExtensionFileVisitor;
import org.moflon.tgg.mosl.tgg.TripleGraphGrammarFile;

public class MutationTestExecuter {
	
	public void executeTests(IProject testProject, IFile launchConfigFile, Integer iterations, Integer timeout) {
		
		//TODO calculate possible mutation count so we don't keep on iterating at some point while no more new mutations are possible
		TGGRuleUtil tggRuleUtil;
		try {
			tggRuleUtil = new TGGRuleUtil(testProject);
			
			
			//TODO select rule file
			
			ExtensionFileVisitor tggFileVisitor = new ExtensionFileVisitor("tgg");
			testProject.accept(tggFileVisitor);
			Path projectPath = testProject.getLocation().toFile().toPath();
			
			int randomIndex = new Random().nextInt(tggFileVisitor.getFiles().size());
			
			
			
			Path tggFilePath = retrieveRandomTggFilePath(tggFileVisitor, projectPath, randomIndex);
			
			createRuleFileBackup(projectPath, tggFilePath);
			
		
			TripleGraphGrammarFile tggFile = tggRuleUtil.loadRule(testProject.getFile(tggFilePath.toString()));
			
			boolean isSuccess = tggRuleUtil.getMutantRule(tggFile);
			
			//TODO store mutant rule file? idk how
			
			if(isSuccess) {
				//build the project with the new TGG file
				testProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
				
				//TODO execute launch config
				
				//TODO retrieve results
				
				
			} else {
				//TODO proper handling
				System.out.println("Unable to mutate any file");
			}
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
		
		
		System.out.println(testProject);
		System.out.println(launchConfigFile);
		System.out.println(iterations);
		System.out.println(timeout);
	}

	private void createRuleFileBackup(Path projectPath, Path tggFilePath) throws IOException {
		Path fileName = tggFilePath.getFileName();	
		Path sourcePath = projectPath.resolve(tggFilePath);
		Path targetPath = sourcePath.resolveSibling(fileName + ".backup");	
		Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
	}

	private Path retrieveRandomTggFilePath(ExtensionFileVisitor tggFileVisitor, Path projectPath, int randomIndex) {
		Path tggFilePath = tggFileVisitor.getFiles().get(randomIndex);
		if (tggFilePath.isAbsolute()) {
			tggFilePath = projectPath.relativize(tggFilePath);
		}
		
		if (IbexTGGNature.SCHEMA_FILE.equals(tggFilePath.toString())) {
			return retrieveRandomTggFilePath(tggFileVisitor, projectPath, randomIndex);
		} else {
			return tggFilePath;
		}
		
	}

}
