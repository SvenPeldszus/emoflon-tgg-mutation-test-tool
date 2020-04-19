package de.unikoblenz.emoflon.tgg.mutationtest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.unikoblenz.emoflon.tgg.mutationtest.util.CsvWriter;

public class TestResultCollector {
	
	private TestResultCollector() {
	}
	
	public static TestResultCollector INSTANCE = new TestResultCollector();
	
	private CsvWriter csvWriter = new CsvWriter();
	
	private List<String[]> resultData = new ArrayList<>();
	
	public void processTestResults(Map<String, String> testResultData) {
		
		MutationTestExecuter.INSTANCE.restoreOriginalRuleFile();
		String mutationName = MutationTestExecuter.INSTANCE.getMutantResult().getMutationName();

		List<String[]> resultData = testResultData.entrySet().stream()
				.map(testResultEntry -> createResultDataArray(mutationName, testResultEntry.getKey(), testResultEntry.getValue()))
				.collect(Collectors.toList());
		try {
			csvWriter.writeCsvFile(MutationTestExecuter.INSTANCE.getTggProject().getName(), resultData);
		} catch (IOException e) {
			// TODO logger
			e.printStackTrace();
		}
		
		//TODO if finished create result csv | else start next iteration
	}
	
	private String[] createResultDataArray(String mutationName, String testMethod, String testResult) {
		// TODO Auto-generated method stub
		return new String[] {mutationName, testMethod, testResult};
	}
	
	public void clearResultDataList() {
		resultData.clear();
	}
	
}
