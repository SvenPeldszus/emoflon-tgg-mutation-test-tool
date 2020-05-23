package de.unikoblenz.emoflon.tgg.mutationtest.ui.util.representation;

public class MutationUnitTestResult {
	
	private TestResult testResult;
	
	private boolean differentFromInitial;

	public MutationUnitTestResult(TestResult testResult, TestResult initialTestResult) {
		super();
		this.testResult = testResult;
		this.differentFromInitial = !testResult.equals(initialTestResult);
	}

	public TestResult getTestResult() {
		return testResult;
	}

	public boolean isDifferentFromInitial() {
		return differentFromInitial;
	}
}
