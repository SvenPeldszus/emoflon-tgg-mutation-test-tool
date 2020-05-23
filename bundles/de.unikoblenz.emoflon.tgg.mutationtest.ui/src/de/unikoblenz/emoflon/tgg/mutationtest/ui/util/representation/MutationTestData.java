package de.unikoblenz.emoflon.tgg.mutationtest.ui.util.representation;

public class MutationTestData {
	
	private String mutatedRule;
	
	private String mutationRepresentation;
	
	private String testMethodName; 
	
	private TestResult testResult;

	public String getMutatedRule() {
		return mutatedRule;
	}

	public String getMutationRepresentation() {
		return mutationRepresentation;
	}

	public String getTestName() {
		return testMethodName;
	}

	public TestResult getTestResult() {
		return testResult;
	}
	
	
	
	public MutationTestData(String mutatedRule, String mutationRepresentation, String testMethodName,
			TestResult testResult) {
		super();
		this.mutatedRule = mutatedRule;
		this.mutationRepresentation = mutationRepresentation;
		this.testMethodName = testMethodName;
		this.testResult = testResult;
	}

	/**
	 * Expected CSV Format: 
	 * mutated rule name, mutation representation, test method name, JUNIT test result (OK/FAILURE/ERROR)
	 * 
	 * @param csvData 
	 * @return
	 */
	public static MutationTestData createTestDataFromCSV(String[] csvData) {
		TestResult testResult = TestResult.mapStringToTestResult(csvData[3]);
		return new MutationTestData(csvData[0], csvData[1], csvData[2], testResult);
	}
}
