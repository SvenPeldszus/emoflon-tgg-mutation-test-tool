package de.unikoblenz.emoflon.tgg.mutationtest.util.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MutationTestData {
	
	private String mutatedRule;
	
	private List<MutationTestResult> mutationTestResults = new ArrayList<>();

	public String getMutatedRule() {
		return mutatedRule;
	}	
	
	public MutationTestData(String mutatedRuleName) {
		super();
		this.mutatedRule = mutatedRuleName;
	}
	
	public void addMutationTestResult(MutationTestResult mutationTestResult) {
		mutationTestResults.add(mutationTestResult);
	}

	public List<MutationTestResult> getMutationTestResults() {
		return mutationTestResults;
	}
	
	
}
