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
		
		TreeColumn column1 = new TreeColumn(tree, SWT.CENTER);
		column1.setText("Mutation description");
		column1.setWidth(200);
		
		TreeColumn column2 = new TreeColumn(tree, SWT.CENTER);
		column2.setText("Test name");
		column1.setWidth(500);
		
		TreeColumn column3 = new TreeColumn(tree, SWT.CENTER);
		column3.setText("Test result");
		column3.setWidth(200);

//		List<String[]> resultData = TestResultCollector.INSTANCE.getResultData();
		List<String[]> resultData = Arrays.asList(
				new String[] { "AddourcePattern", "[41: Forward Transformation From Src: InnerClassInConstructor]",
						"Error" },
				new String[] { "AddourcePattern", "[22: Forward Transformation From Src: FieldInAnonClass]", "Error" },
				new String[] { "myMutation", "[39: Forward Transformation From Src: InnerClassInAnonymousClass]",
						"OK" },
				new String[] { "myMutation", "[43: Forward Transformation From Src: InterfaceExtendsInterface]",
						"OK" });

		Map<String, List<String[]>> resultAggregation = new HashMap<>();
		for (String[] data : resultData) {
			resultAggregation.compute(data[0], (key, value) -> {
				if (value == null) {
					value = new ArrayList<>();
				}
				value.add(data);
				return value;
			});
		}

		TreeItem root = new TreeItem(tree, SWT.NONE);

		resultDataMap.clear();		
		
		for(Entry<String, List<String[]>> dataEntry : resultAggregation.entrySet()) {
			TreeItem iterationItem = new TreeItem(root, SWT.NONE);
			iterationItem.setText(new String[] { dataEntry.getKey(), "", ""});
			
			for(String[] testResultData : dataEntry.getValue()) {
			TreeItem childItem = new TreeItem(iterationItem, SWT.NONE);
			childItem.setText(new String[] { "", testResultData[1], testResultData[2] });
			resultDataMap.put(childItem, testResultData);
			}
		}

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
