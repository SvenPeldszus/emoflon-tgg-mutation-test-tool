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

//		List<String[]> resultData = TestResultCollector.INSTANCE.getResultData();
		List<String[]> resultData = Arrays.asList(
				new String[] { "rule1", "AddourcePattern",
						"[41: Forward Transformation From Src: InnerClassInConstructor]", "Error" },
				new String[] { "rule1", "AddourcePattern", "[22: Forward Transformation From Src: FieldInAnonClass]",
						"Error" },
				new String[] { "rule1", "myMutation",
						"[39: Forward Transformation From Src: InnerClassInAnonymousClass]", "OK" },
				new String[] { "rule2", "myMutation",
						"[43: Forward Transformation From Src: InterfaceExtendsInterface]", "OK" });

		// TODO proper data structure => data type for result (OK,FAILURE,ERROR)
		Map<String, Map<String, List<String[]>>> resultAggregation = new HashMap<>();

		for (String[] data : resultData) {
			resultAggregation.compute(data[0], (key, value) -> {
				if (value == null) {
					value = new HashMap<>();
				}
				value.compute(data[1], (key2, value2) -> {
					if (value2 == null) {
						value2 = new ArrayList<>();
					}
					value2.add(data);
					return value2;
				});
				return value;
			});
		}

//		TreeItem root = new TreeItem(tree, SWT.NONE);

//		resultDataMap.clear();

		// convert data aggregation into tree
		for (Entry<String, Map<String, List<String[]>>> ruleData : resultAggregation.entrySet()) {
			TreeItem ruleRootItem = new TreeItem(tree, SWT.NONE);
			ruleRootItem.setText(new String[] { ruleData.getKey() });

			for (Entry<String, List<String[]>> mutationData : ruleData.getValue().entrySet()) {
				TreeItem childItem = new TreeItem(ruleRootItem, SWT.NONE);
				childItem.setText(new String[] { "", mutationData.getKey() });

				for (String[] testResultData : mutationData.getValue()) {
					TreeItem testResultTreeItem = new TreeItem(childItem, SWT.NONE);
					testResultTreeItem.setText(new String[] { "", "", "", testResultData[2], testResultData[3] });
//					resultDataMap.put(childItem, testResultData);
				}
			}
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
