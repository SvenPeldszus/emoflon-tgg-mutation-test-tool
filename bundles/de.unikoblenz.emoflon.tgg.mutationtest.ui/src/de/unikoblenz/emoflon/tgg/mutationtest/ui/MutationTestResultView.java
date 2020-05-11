package de.unikoblenz.emoflon.tgg.mutationtest.ui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

import de.unikoblenz.emoflon.tgg.mutationtest.TestResultCollector;

public class MutationTestResultView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		GridLayout grid = new GridLayout(1, false);
		parent.setLayout(grid);
		parent.setLayoutData(gridData);
		parent.setSize(500, 500);
		
		Tree tree = new Tree(parent, SWT.BORDER | SWT.FILL);
		tree.setSize(400, 400);

		
//		List<String[]> resultData = TestResultCollector.INSTANCE.getResultData();
		List<String[]> resultData = Arrays.asList(new String[] {"a"}, new String[] {"b"}, new String[] {"c"});
		
		
		TreeItem root = new TreeItem(tree, 0);
		
		for(int i = 0; i < resultData.size(); i++) {
			String[] result = resultData.get(i);
			TreeItem childItem = new TreeItem(root, i);
			childItem.setText("test");
		}
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
