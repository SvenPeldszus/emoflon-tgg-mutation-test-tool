package de.unikoblenz.emoflon.tgg.mutationtest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import de.unikoblenz.emoflon.tgg.mutationtest.util.CsvWriter;

public class TestResultCollector {

	private TestResultCollector() {
	}

	public static TestResultCollector INSTANCE = new TestResultCollector();
	
	private static final Logger LOGGER = Logger.getLogger(MutationTestExecuter.class);

	private CsvWriter csvWriter = new CsvWriter();

	private List<String[]> resultData = new ArrayList<>();

	public void processTestResults(Map<String, String> testResultData) {

		MutationTestExecuter.INSTANCE.restoreOriginalRuleFile();
		String mutationName = MutationTestExecuter.INSTANCE.getMutantResult().getMutationName();

		testResultData.entrySet().stream().map(testResultEntry -> createResultDataArray(mutationName,
				testResultEntry.getKey(), testResultEntry.getValue())).forEach(resultData::add);

		if (MutationTestExecuter.INSTANCE.isFinished()) {
			writeCsvFile();
			openResultView();
		} else {
			MutationTestExecuter.INSTANCE.executeNextIteration();
		}
	}

	private void openResultView() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("de.unikoblenz.emoflon.tgg.mutationtest.ui.MutationTestResultView");
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
		// TODO Auto-generated method stub
		return new String[] { mutationName, testMethod, testResult };
	}

	public void clearResultDataList() {
		resultData.clear();
	}

}
