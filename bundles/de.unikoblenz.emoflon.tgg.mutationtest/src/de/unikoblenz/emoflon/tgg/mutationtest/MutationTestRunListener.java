package de.unikoblenz.emoflon.tgg.mutationtest;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

import java.util.Arrays;

import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestRunSession;

public class MutationTestRunListener extends TestRunListener {
		
	@Override
	public void sessionLaunched(ITestRunSession session) {
		System.out.println("--------------------------------------My Tests started");
    }
	
	@Override
	public void sessionFinished(ITestRunSession session) {
		System.out.println("--------------------------------------My Tests finished");
    }

}
