package de.unikoblenz.emoflon.tgg.mutationtest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.jdt.junit.model.ITestElement.Result;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.unikoblenz.emoflon.tgg.mutationtest.util.CsvWriter;
import de.unikoblenz.emoflon.tgg.mutationtest.util.FileHandler;
import de.unikoblenz.emoflon.tgg.mutationtest.util.MutantResult;
import de.unikoblenz.emoflon.tgg.mutationtest.util.representation.MutationTestData;
import de.unikoblenz.emoflon.tgg.mutationtest.util.representation.MutationTestResult;
import de.unikoblenz.emoflon.tgg.mutationtest.util.representation.MutationUnitTestResult;

public class TestResultCollector {

	private TestResultCollector() {
	}

	public static TestResultCollector INSTANCE = new TestResultCollector();

	private static final Logger LOGGER = Logger.getLogger(MutationTestExecuter.class);

	private CsvWriter csvWriter = new CsvWriter();

	private List<String[]> resultData = new ArrayList<>();

	private List<MutationTestData> mutationTestDataList = new ArrayList<>();

	private Map<String, Result> initialRunData = new HashMap<>();
	
	private String finishInformation = "";

	public void processTestResults(Map<String, Result> testResultData) {

		if (MutationTestExecuter.INSTANCE.getCreateCsvOutput()) {
			runCsvProcessing(testResultData);
		}

		if (MutationTestExecuter.INSTANCE.getMutantResult().isInitialRun()) {
			runInitialTestResultProcessing(testResultData);
		} else {
			if (MutationTestExecuter.INSTANCE.getSkipInitialTests() && initialRunData.isEmpty()) {
				mockInitialRunData(testResultData);
			}
			runMutationTestResultProcessing(testResultData);
		}

		if (MutationTestExecuter.INSTANCE.isFinished()) {
			System.out.println("All iterations done.");
			setFinishInformation("All iterations done.");
			finishProcessing();
		} else {
			MutationTestExecuter.INSTANCE.executeNextIteration();
		}
	}

	public void finishProcessing() {
		LOGGER.info("Finished tests. Reason: " + finishInformation);
		if (MutationTestExecuter.INSTANCE.getCreateCsvOutput()) {
			writeCsvFile();
		}
		openResultView();
	}

	private void mockInitialRunData(Map<String, Result> testResultData) {
		testResultData.entrySet().forEach(testResultEntry -> initialRunData.put(testResultEntry.getKey(), Result.OK));
	}

	private void runInitialTestResultProcessing(Map<String, Result> testResultData) {
		testResultData.entrySet()
				.forEach(testResultEntry -> initialRunData.put(testResultEntry.getKey(), testResultEntry.getValue()));
	}

	private void runMutationTestResultProcessing(Map<String, Result> testResultData) {
		MutantResult mutantResult = MutationTestExecuter.INSTANCE.getMutantResult();
		String ruleName = mutantResult.getMutantRule().getName();

		MutationTestData mutationTestData = findMutationTestData(ruleName);
		if (mutationTestData == null) {
			mutationTestData = new MutationTestData(ruleName);
			mutationTestDataList.add(mutationTestData);
		}

		MutationTestResult mutationTestResult = new MutationTestResult(mutantResult.getMutationName());

		for (Entry<String, Result> testResultEntry : testResultData.entrySet()) {
			String methodName = testResultEntry.getKey();
			MutationUnitTestResult unitTestResult = new MutationUnitTestResult(testResultEntry.getValue(),
					initialRunData.get(methodName));

			mutationTestResult.addUnitTestResult(methodName, unitTestResult);
		}
		
		FileHandler.INSTANCE.moveMutationFile(mutationTestResult.isMutationDetected());
		FileHandler.INSTANCE.restoreOriginalRuleFile();
				
		mutationTestData.addMutationTestResult(mutationTestResult);
	}
	
	private MutationTestData findMutationTestData(String ruleName) {
		return mutationTestDataList.stream().filter(d -> ruleName.equals(d.getMutatedRule())).findFirst().orElse(null);
	}

	private void runCsvProcessing(Map<String, Result> testResultData) {
		String mutationName = MutationTestExecuter.INSTANCE.getMutantResult().getMutationName();

		testResultData.entrySet().stream().map(testResultEntry -> createResultDataArray(mutationName,
				testResultEntry.getKey(), testResultEntry.getValue().toString())).forEach(resultData::add);
	}

	public void openResultView() {
		try {
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//					.showView("de.unikoblenz.emoflon.tgg.mutationtest.ui.MutationTestResultView");
			PlatformUI.getWorkbench().getWorkbenchWindows()[0].getPages()[0]
					.showView("de.unikoblenz.emoflon.tgg.mutationtest.ui.MutationTestResultView");
		} catch (PartInitException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void writeCsvFile() {
		try {
			csvWriter.writeCsvFile(MutationTestExecuter.INSTANCE.getTggProject().getName(), resultData);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private String[] createResultDataArray(String mutationName, String testMethod, String testResult) {
		return new String[] { mutationName, testMethod, testResult };
	}

	public void clearResultDataList() {
		resultData.clear();
		mutationTestDataList.clear();
		initialRunData.clear();
	}

	public List<String[]> getResultData() {
		return resultData;
	}

	public List<MutationTestData> getMutationTestDataList() {
		return mutationTestDataList;
	}

	public Map<String, Result> getInitialRunData() {
		return initialRunData;
	}

	public String getFinishInformation() {
		return finishInformation;
	}

	public void setFinishInformation(String finishInformation) {
		this.finishInformation = finishInformation;
	}

	
	
}
