package de.unikoblenz.emoflon.tgg.mutationtest;

import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

public class MutationTestRunListener extends RunListener {
	
	@Override
	public void testRunFinished(Result result) throws Exception {
		System.out.println("--------------------------------------My Test finished");
    }

}
