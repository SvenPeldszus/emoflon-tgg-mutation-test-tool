package de.unikoblenz.emoflon.tgg.mutationtest.util.representation;

import java.util.HashMap;
import java.util.Map;

public class MutationTestResult {

	private String mutationName;

	private Map<String, MutationUnitTestResult> unitTestResults = new HashMap<>();

	public MutationTestResult(String mutationName) {
		this.mutationName = mutationName;
	}

	public void addUnitTestResult(String testMethodName, MutationUnitTestResult mutationUnitTestResult) {
		unitTestResults.put(testMethodName, mutationUnitTestResult);
	}

	public boolean isMutationDetected() {
		return unitTestResults.values().stream().anyMatch(MutationUnitTestResult::isDifferentFromInitial);
	}

	public String getMutationName() {
		return mutationName;
	}

	public Map<String, MutationUnitTestResult> getUnitTestResults() {
		return unitTestResults;
	}
}
