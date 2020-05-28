package de.unikoblenz.emoflon.tgg.mutationtest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.unikoblenz.emoflon.tgg.mutationtest.util.CsvWriter;
import de.unikoblenz.emoflon.tgg.mutationtest.util.MutantResult;
import de.unikoblenz.emoflon.tgg.mutationtest.util.representation.MutationTestData;
import de.unikoblenz.emoflon.tgg.mutationtest.util.representation.MutationTestResult;
import de.unikoblenz.emoflon.tgg.mutationtest.util.representation.MutationUnitTestResult;
import de.unikoblenz.emoflon.tgg.mutationtest.util.representation.TestResult;

public class TestResultCollector {

	private TestResultCollector() {
	}

	public static TestResultCollector INSTANCE = new TestResultCollector();

	private static final Logger LOGGER = Logger.getLogger(MutationTestExecuter.class);

	private CsvWriter csvWriter = new CsvWriter();

	private List<String[]> resultData = new ArrayList<>();

	private List<MutationTestData> mutationTestDataList = new ArrayList<>();

	private Map<String, TestResult> initialRunData = new HashMap<>();

	public void processTestResults(Map<String, String> testResultData) {

		MutationTestExecuter.INSTANCE.restoreOriginalRuleFile();
		String mutationName = MutationTestExecuter.INSTANCE.getMutantResult().getMutationName();

		runCsvProcessing(testResultData);

		if (MutationTestExecuter.INSTANCE.getMutantResult().isInitialRun()) {
			runInitialTestResultProcessing(testResultData);
		} else {
			runMutationTestResultProcessing(testResultData);
		}

		if (MutationTestExecuter.INSTANCE.isFinished()) {
			writeCsvFile();
			openResultView();
		} else {
			MutationTestExecuter.INSTANCE.executeNextIteration();
		}
	}

	private void runInitialTestResultProcessing(Map<String, String> testResultData) {
		testResultData.entrySet().forEach(
				(entry) -> initialRunData.put(entry.getKey(), TestResult.mapStringToTestResult(entry.getValue())));
	}

	private void runMutationTestResultProcessing(Map<String, String> testResultData) {
		MutantResult mutantResult = MutationTestExecuter.INSTANCE.getMutantResult();
		String ruleName = mutantResult.getMutantRule().getName();

		MutationTestData mutationTestData = findMutationTestData(ruleName);
		if(mutationTestData == null) {
			mutationTestData = new MutationTestData(ruleName);
			mutationTestDataList.add(mutationTestData);
		}

		MutationTestResult mutationTestResult = new MutationTestResult(mutantResult.getMutationName());

		for (Entry<String, String> testResultEntry : testResultData.entrySet()) {
			String methodName = testResultEntry.getKey();
			TestResult testResult = TestResult.mapStringToTestResult(testResultEntry.getValue());
			MutationUnitTestResult unitTestResult = new MutationUnitTestResult(testResult,
					initialRunData.get(methodName));

			mutationTestResult.addUnitTestResult(methodName, unitTestResult);
		}
		mutationTestData.addMutationTestResult(mutationTestResult);
	}

	private MutationTestData findMutationTestData(String ruleName) {
		return mutationTestDataList.stream().filter(d -> ruleName.equals(d.getMutatedRule())).findFirst().orElse(null);
	}

	private void runCsvProcessing(Map<String, String> testResultData) {
		String mutationName = MutationTestExecuter.INSTANCE.getMutantResult().getMutationName();

		testResultData.entrySet().stream().map(testResultEntry -> createResultDataArray(mutationName,
				testResultEntry.getKey(), testResultEntry.getValue())).forEach(resultData::add);
	}

	private void openResultView() {
		try {
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//					.showView("de.unikoblenz.emoflon.tgg.mutationtest.ui.MutationTestResultView");
			PlatformUI.getWorkbench().getWorkbenchWindows()[0].getPages()[0].showView("de.unikoblenz.emoflon.tgg.mutationtest.ui.MutationTestResultView");
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

	public Map<String, TestResult> getInitialRunData() {
		return initialRunData;
	}

}
