package de.unikoblenz.emoflon.tgg.mutationtest.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import de.unikoblenz.emoflon.tgg.mutationtest.MutationTestExecuter;

public class FileHandler {

	public static FileHandler INSTANCE = new FileHandler();

	private static final Logger LOGGER = Logger.getLogger(FileHandler.class);

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy--HH-mm");

	private static final Path UNDETECTED_DIR_NAME = Paths.get("undetected");
	private static final Path DETECTED_DIR_NAME = Paths.get("detected");

	private static final Path DEBUG_HOME_DIR = Paths.get(System.getProperty("user.home"), "emoflon", "debug");

	private Path projectPath;
	private Path detectedDirPath;
	private Path undetectedDirPath;

	private String startTimeString;

	public void prepareForNewRun() {
		startTimeString = DATE_FORMAT.format(new Date());
		Path testRunDirectoryName = Paths.get("mutations", startTimeString);

		projectPath = MutationTestExecuter.INSTANCE.getTggProject().getLocation().toFile().toPath();
		Path testRunDirPath = projectPath.resolve(testRunDirectoryName);

		detectedDirPath = testRunDirPath.resolve(DETECTED_DIR_NAME);
		undetectedDirPath = detectedDirPath.resolveSibling(UNDETECTED_DIR_NAME);

		detectedDirPath.toFile().mkdirs();
		undetectedDirPath.toFile().mkdirs();
	}

	public void moveMutationFile(boolean mutationDetected) {
		MutantResult mutantResult = MutationTestExecuter.INSTANCE.getMutantResult();
		Path filePath = Paths.get(mutantResult.getMutantRule().eResource().getURI().toPlatformString(true));
		Path fileName = filePath.getFileName();
		Path mutatedFilePath = projectPath.resolve(filePath.subpath(1, filePath.getNameCount()));

		Path ruleAndMutationPath = Paths.get(mutantResult.getMutantRule().getName(), mutantResult.getMutationName());

		Path destinationDirectoryPath;
		if (mutationDetected) {
			destinationDirectoryPath = detectedDirPath.resolve(ruleAndMutationPath);
		} else {
			destinationDirectoryPath = undetectedDirPath.resolve(ruleAndMutationPath);
		}

		destinationDirectoryPath.toFile().mkdirs();
		Path destinationPath = destinationDirectoryPath.resolve(fileName);

		// TODO debug
		LOGGER.info("Saving " + (mutationDetected ? "detected" : "undetected") + " rule mutation: "
				+ mutantResult.getMutantRule());
		// TODO debug
		LOGGER.info("Rule file: " + fileName);
		try {
			Files.copy(mutatedFilePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public void createRuleFileBackup() {
		Path filePath = Paths.get(MutationTestExecuter.INSTANCE.getMutantResult().getMutantRule().eResource().getURI()
				.toPlatformString(true));
		Path sourcePath = projectPath.resolve(filePath.subpath(1, filePath.getNameCount()));
		Path fileName = filePath.getFileName();
		Path targetPath = sourcePath.resolveSibling(fileName + ".backup");
		// TODO debug
		LOGGER.info("Creating file backup: " + filePath);
		try {
			Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public void restoreOriginalRuleFile() {
		MutantResult mutantResult = MutationTestExecuter.INSTANCE.getMutantResult();
		if (mutantResult == null || mutantResult.isInitialRun() || mutantResult.getMutantRule() == null) {
			return;
		}

		Path filePath = Paths.get(mutantResult.getMutantRule().eResource().getURI().toPlatformString(true));
		Path fileName = filePath.getFileName();
		Path mutatedFilePath = projectPath.resolve(filePath.subpath(1, filePath.getNameCount()));
		Path backupFilePath = mutatedFilePath.resolveSibling(fileName + ".backup");
		// TODO debug
		LOGGER.info("Restoring file: " + filePath);

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

	public void storeMutationFileForDebug() {
		if (!DEBUG_HOME_DIR.toFile().exists()) {
			DEBUG_HOME_DIR.toFile().mkdirs();
		}

		MutantResult mutantResult = MutationTestExecuter.INSTANCE.getMutantResult();
		Path filePath = Paths.get(mutantResult.getMutantRule().eResource().getURI().toPlatformString(true));
		Path fileName = filePath.getFileName();
		Path mutatedFilePath = projectPath.resolve(filePath.subpath(1, filePath.getNameCount()));

		Path testRunDebugDir = DEBUG_HOME_DIR.resolve(startTimeString);
		testRunDebugDir.toFile().mkdirs();

		Path destinationPath = testRunDebugDir.resolve(fileName);

		// TODO debug
		LOGGER.info("Saving erroneous rule mutation: " + mutantResult.getMutantRule());
		// TODO debug
		LOGGER.info("Rule file: " + destinationPath);
		try {
			Files.copy(mutatedFilePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

}
