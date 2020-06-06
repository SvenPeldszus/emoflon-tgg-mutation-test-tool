package de.unikoblenz.emoflon.tgg.mutationtest.util.representation;

import org.eclipse.jdt.junit.model.ITestElement.Result;

public class MutationUnitTestResult {
	
	private Result testResult;
	
	private boolean differentFromInitial;

	public MutationUnitTestResult(Result testResult, Result initialTestResult) {
		super();
		this.testResult = testResult;
		this.differentFromInitial = !testResult.equals(initialTestResult) && testResult != Result.UNDEFINED;
	}

	public Result getTestResult() {
		return testResult;
	}

	public boolean isDifferentFromInitial() {
		return differentFromInitial;
	}
}
