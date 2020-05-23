package de.unikoblenz.emoflon.tgg.mutationtest.ui.util.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MutationTestData {
	
	private String mutatedRule;
	
	private Map<String, TestResult> initialRunResults = new HashMap<>();
	
	private List<MutationTestResult> mutationTestResults = new ArrayList<>();

	public String getMutatedRule() {
		return mutatedRule;
	}	
	
	public MutationTestData(String mutatedRuleName, String mutationRepresentation, String testMethodName,
			TestResult testResult) {
		super();
		this.mutatedRule = mutatedRuleName;
	}
	
	public void addInitialTestResult(String testMethodName, TestResult testResult) {
		initialRunResults.put(testMethodName, testResult);
	}
	
	public void addMutationTestResult(MutationTestResult mutationTestResult) {
		mutationTestResults.add(mutationTestResult);
	}
}
