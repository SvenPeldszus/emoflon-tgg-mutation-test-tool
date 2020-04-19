package de.unikoblenz.emoflon.tgg.mutationtest;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

import de.unikoblenz.emoflon.tgg.mutationtest.util.CsvWriter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.internal.junit.model.TestRunSession;
import org.eclipse.jdt.internal.junit.model.TestSuiteElement;
import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestElement;
import org.eclipse.jdt.junit.model.ITestRunSession;
import org.eclipse.jdt.junit.model.ITestSuiteElement;

public class MutationTestRunListener extends TestRunListener {

	private CsvWriter csvWriter = new CsvWriter();

	@Override
	public void sessionLaunched(ITestRunSession session) {
//		if (testConfigName.equals(session.getTestRunName())) {
		System.out.println("--------------------------------------My Tests started");
		System.out.println(session.getTestRunName());
		System.out.println(session.getLaunchedProject().getProject().getName());
//		}
	}

	@Override
	public void sessionFinished(ITestRunSession session) {
		if (MutationTestExecuter.INSTANCE.getLaunchConfigFile().getName().equals(session.getTestRunName())) {

			System.out.println("--------------------------------------My Tests finished");
			System.out.println("Test run result: " + session.getTestResult(true));

			Map<String, String> testResultData = Arrays.stream(session.getChildren())
					.filter(iTestElement -> iTestElement instanceof TestSuiteElement)
					.map(iTestElement -> (TestSuiteElement) iTestElement)
					.flatMap(this::testSuiteElementChildrenToStream)
					.map(iTestElement -> (TestSuiteElement) iTestElement).map(this::printTestResult)
					.collect(Collectors.toMap(testSuiteElement -> testSuiteElement.getTestName(),
							testSuiteElement -> testSuiteElement.getTestResult(true).toString()));
			TestResultCollector.INSTANCE.processTestResults(testResultData);

		}
	}

	private Stream<ITestElement> testSuiteElementChildrenToStream(TestSuiteElement testSuiteElement) {
		return Arrays.stream(testSuiteElement.getChildren());
	}

	private TestSuiteElement printTestResult(TestSuiteElement testSuiteElement) {
		System.out.println("-- " + testSuiteElement.getTestName() + " Result: " + testSuiteElement.getTestResult(true));
		return testSuiteElement;
	}
}
