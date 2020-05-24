package de.unikoblenz.emoflon.tgg.mutationtest.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

import de.unikoblenz.emoflon.tgg.mutationtest.TestResultCollector;
import de.unikoblenz.emoflon.tgg.mutationtest.util.representation.MutationTestData;
import de.unikoblenz.emoflon.tgg.mutationtest.util.representation.MutationTestResult;
import de.unikoblenz.emoflon.tgg.mutationtest.util.representation.MutationUnitTestResult;
import de.unikoblenz.emoflon.tgg.mutationtest.util.representation.TestResult;

public class MutationTestResultView extends ViewPart {

	// map for keeping references for selection events
	Map<TreeItem, String[]> resultDataMap = new HashMap<>();

	@Override
	public void createPartControl(Composite parent) {
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		FillLayout fillLayout = new FillLayout();
		GridLayout grid = new GridLayout(1, false);
//		parent.setLayout(grid);
//		parent.setLayoutData(gridData);
//		parent.setSize(500, 500);

		Tree tree = new Tree(parent, SWT.BORDER | SWT.FILL);
		tree.setLayoutData(gridData);
		tree.setHeaderVisible(true);

		TreeColumn ruleNameColumn = new TreeColumn(tree, SWT.CENTER);
		ruleNameColumn.setText("Rule name");
		ruleNameColumn.setWidth(200);

		TreeColumn mutationDescriptionColumn = new TreeColumn(tree, SWT.CENTER);
		mutationDescriptionColumn.setText("Mutation description");
		mutationDescriptionColumn.setWidth(200);

		TreeColumn aggregatedResultColumn = new TreeColumn(tree, SWT.CENTER);
		aggregatedResultColumn.setText("detected?");
		aggregatedResultColumn.setWidth(75);

		TreeColumn testMethodNameColumn = new TreeColumn(tree, SWT.CENTER);
		testMethodNameColumn.setText("Test method name");
		testMethodNameColumn.setWidth(400);

		TreeColumn testResultColumn = new TreeColumn(tree, SWT.CENTER);
		testResultColumn.setText("Test result");
		testResultColumn.setWidth(75);

		TreeColumn differentFromInitialColumn = new TreeColumn(tree, SWT.CENTER);
		differentFromInitialColumn.setText("differs from inital?");
		differentFromInitialColumn.setWidth(100);
		
		setupTestData();

		Map<String, TestResult> initialRunData = TestResultCollector.INSTANCE.getInitialRunData();
		TreeItem initialRunRootItem = new TreeItem(tree, SWT.NONE);
		initialRunRootItem.setText(new String[] { "initial run without mutation" });
		for (Entry<String, TestResult> initialRunResult : initialRunData.entrySet()) {
			TreeItem childItem = new TreeItem(initialRunRootItem, SWT.NONE);
			childItem.setText(new String[] { "", "", "", initialRunResult.getKey(),
					mapTestResultToString(initialRunResult.getValue()), "" });
		}

		List<MutationTestData> mutationTestDataList = TestResultCollector.INSTANCE.getMutationTestDataList();
		for (MutationTestData mutationTestData : mutationTestDataList) {
			TreeItem mutationTestRunItem = new TreeItem(tree, SWT.NONE);
			mutationTestRunItem.setText(new String[] { mutationTestData.getMutatedRule() });

			for (MutationTestResult mutationTestResult : mutationTestData.getMutationTestResults()) {
				TreeItem mutationTestResultItem = new TreeItem(mutationTestRunItem, SWT.NONE);
				mutationTestResultItem
						.setText(new String[] { "", mutationTestResult.getMutationName(), "", "", "", "" });

				for (Entry<String, MutationUnitTestResult> testResultEntry : mutationTestResult.getUnitTestResults()
						.entrySet()) {
					TreeItem unitTestResultItem = new TreeItem(mutationTestResultItem, SWT.NONE);
					unitTestResultItem.setText(new String[] { "", "", "", testResultEntry.getKey(),
							mapTestResultToString(testResultEntry.getValue().getTestResult()),
							String.valueOf(testResultEntry.getValue().isDifferentFromInitial()) });
				}
			}
		}
	}

	private String mapTestResultToString(TestResult testResult) {
		switch (testResult) {
		case OK:
			return "OK";
		case FAILURE:
			return "Failure";
		case ERROR:
			return "Error";
		default:
			return "unknown result";
		}
	}
	
	private void setupTestData() {
		TestResultCollector.INSTANCE.clearResultDataList();
		TestResultCollector.INSTANCE.getInitialRunData().put("method1", TestResult.OK);
		TestResultCollector.INSTANCE.getInitialRunData().put("method2", TestResult.OK);
		TestResultCollector.INSTANCE.getInitialRunData().put("method3", TestResult.OK);
		
		MutationTestData mutationTestData1 = new MutationTestData("rule1");
		
		MutationTestResult mutationTestResult1 = new MutationTestResult("mutation1");
		mutationTestResult1.addUnitTestResult("method1", new MutationUnitTestResult(TestResult.FAILURE, TestResult.OK));
		mutationTestResult1.addUnitTestResult("method2", new MutationUnitTestResult(TestResult.OK, TestResult.OK));
		mutationTestResult1.addUnitTestResult("method3", new MutationUnitTestResult(TestResult.ERROR, TestResult.OK));
		mutationTestData1.addMutationTestResult(mutationTestResult1);
		
		MutationTestResult mutationTestResult2 = new MutationTestResult("mutation2");
		mutationTestResult2.addUnitTestResult("method1", new MutationUnitTestResult(TestResult.OK, TestResult.OK));
		mutationTestResult2.addUnitTestResult("method2", new MutationUnitTestResult(TestResult.OK, TestResult.OK));
		mutationTestResult2.addUnitTestResult("method3", new MutationUnitTestResult(TestResult.FAILURE, TestResult.OK));
		mutationTestData1.addMutationTestResult(mutationTestResult2);
		
		MutationTestData mutationTestData2 = new MutationTestData("rule2");
		
		MutationTestResult mutationTestResult3 = new MutationTestResult("mutation1");
		mutationTestResult3.addUnitTestResult("method1", new MutationUnitTestResult(TestResult.OK, TestResult.OK));
		mutationTestResult3.addUnitTestResult("method2", new MutationUnitTestResult(TestResult.OK, TestResult.OK));
		mutationTestResult3.addUnitTestResult("method3", new MutationUnitTestResult(TestResult.ERROR, TestResult.OK));
		mutationTestData2.addMutationTestResult(mutationTestResult3);
		
		MutationTestResult mutationTestResult4 = new MutationTestResult("mutation2");
		mutationTestResult4.addUnitTestResult("method1", new MutationUnitTestResult(TestResult.OK, TestResult.OK));
		mutationTestResult4.addUnitTestResult("method2", new MutationUnitTestResult(TestResult.OK, TestResult.OK));
		mutationTestResult4.addUnitTestResult("method3", new MutationUnitTestResult(TestResult.OK, TestResult.OK));
		mutationTestData2.addMutationTestResult(mutationTestResult4);
		
		TestResultCollector.INSTANCE.getMutationTestDataList().add(mutationTestData1);
		TestResultCollector.INSTANCE.getMutationTestDataList().add(mutationTestData2);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
