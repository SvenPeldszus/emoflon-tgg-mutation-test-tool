package de.unikoblenz.emoflon.tgg.mutationtest.util.representation;

public enum TestResult {
	OK, FAILURE, ERROR;

	public static TestResult mapStringToTestResult(String s) {
		if (s.equalsIgnoreCase("ok")) {
			return OK;
		}
		if (s.equalsIgnoreCase("failure")) {
			return FAILURE;
		}
		if (s.equalsIgnoreCase("error")) {
			return ERROR;
		}
		throw new IllegalArgumentException("Invalid input");
	}
}
