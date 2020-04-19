package de.unikoblenz.emoflon.tgg.mutationtest.util;

import java.util.List;

public class MutantResult {
	String mutationName;
	boolean success;

	String nodeName;
	String nodeType;
	String nodeOperator;
	String nodeSourceName;
	String nodeTargetName;
	List<String> listLinkNames;
	List<String> listCorrNames;

	String errorText;

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
	
	
}
