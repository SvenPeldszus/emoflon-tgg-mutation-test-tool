package de.unikoblenz.emoflon.tgg.mutationtest.util;

import java.util.List;

import org.moflon.tgg.mosl.tgg.Rule;

public class MutantResult {
	String mutationName;
	String description;
	boolean success;

	String nodeName;
	String nodeType;
	String nodeOperator;
	String nodeSourceName;
	String nodeTargetName;
	List<String> listLinkNames;
	List<String> listCorrNames;
	
	private boolean initialRun;

	String errorText;
	private Rule mutantRule;

	public MutantResult(Rule rule) {
		this.mutantRule = rule;
	}

	public String getMutationName() {
		return mutationName;
	}

	public void setMutationName(String mutationName) {
		this.mutationName = mutationName;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public String getNodeOperator() {
		return nodeOperator;
	}

	public void setNodeOperator(String nodeOperator) {
		this.nodeOperator = nodeOperator;
	}

	public String getNodeSourceName() {
		return nodeSourceName;
	}

	public void setNodeSourceName(String nodeSourceName) {
		this.nodeSourceName = nodeSourceName;
	}

	public String getNodeTargetName() {
		return nodeTargetName;
	}

	public void setNodeTargetName(String nodeTargetName) {
		this.nodeTargetName = nodeTargetName;
	}

	public List<String> getListLinkNames() {
		return listLinkNames;
	}

	public void setListLinkNames(List<String> listLinkNames) {
		this.listLinkNames = listLinkNames;
	}

	public List<String> getListCorrNames() {
		return listCorrNames;
	}

	public void setListCorrNames(List<String> listCorrNames) {
		this.listCorrNames = listCorrNames;
	}

	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}
	

	public boolean isInitialRun() {
		return initialRun;
	}

	public void setInitialRun(boolean initialRun) {
		this.initialRun = initialRun;
	}

	/**
	 * A getter for the mutated rule
	 *  
	 * @return the mutantRule The rule
	 */
	public Rule getMutantRule() {
		return mutantRule;
	}
	
	
}
