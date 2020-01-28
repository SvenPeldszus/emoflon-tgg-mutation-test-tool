package de.unikoblenz.emoflon.tgg.mutationtest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.lang.Math;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.moflon.tgg.mosl.tgg.AttrCond;
import org.moflon.tgg.mosl.tgg.AttributeExpression;
import org.moflon.tgg.mosl.tgg.ContextObjectVariablePattern;
import org.moflon.tgg.mosl.tgg.CorrType;
import org.moflon.tgg.mosl.tgg.CorrVariablePattern;
import org.moflon.tgg.mosl.tgg.LinkVariablePattern;
import org.moflon.tgg.mosl.tgg.ObjectVariablePattern;
import org.moflon.tgg.mosl.tgg.Operator;
import org.moflon.tgg.mosl.tgg.Rule;
import org.moflon.tgg.mosl.tgg.Schema;
import org.moflon.tgg.mosl.tgg.TggFactory;
import org.moflon.tgg.mosl.tgg.TripleGraphGrammarFile;
import org.emoflon.ibex.tgg.ide.admin.IbexTGGNature;

/**
 * A utility class for loading and serializing TGG specifications
 * 
 * @author speldszus
 *
 */
public class TGGRuleUtil {

	private static final String SCHEMA_FILE = IbexTGGNature.SCHEMA_FILE;
	private XtextResourceSet resourceSet;
	private final String DEFAULT_OPERATOR = "++";


	/**
	 * Creates a new rule util for the given TGG project
	 * 
	 * @param project The Eclipse project
	 * @throws IOException If the TGG Schema couldn't be loaded
	 * @throws CoreException If the TGG Schema couldn't be loaded
	 */
	public TGGRuleUtil(IProject project) throws IOException, CoreException {
		resourceSet = new XtextResourceSet();
		IFile schemaFile = project.getFile(SCHEMA_FILE);
		Resource schemaResource = resourceSet
				.createResource(URI.createURI(schemaFile.getFullPath().toString(), false));
		schemaResource.load(schemaFile.getContents(), Collections.emptyMap());
		EcoreUtil.resolveAll(resourceSet);
	}

	/**
	 * Loads the TGG rule from the given file
	 * 
	 * @param file A file
	 * @return The loaded rule
	 * @throws IOException If reading the serialized rule failed
	 * @throws CoreException if this method fails. The status code associated with exception reflects the cause of the failure.
	 */
	public TripleGraphGrammarFile loadRule(IFile file) throws IOException, CoreException {
		Resource ruleResource = resourceSet.getResource(URI.createPlatformResourceURI(file.getFullPath().toString(), true), true);
		ruleResource.load(file.getContents(), Collections.emptyMap());
		EcoreUtil.resolveAll(resourceSet);
		return (TripleGraphGrammarFile) ruleResource.getContents().get(0);
	}
	
	public boolean getMutantRule(TripleGraphGrammarFile tggFile) {
		try {
			List<Rule> rules = tggFile.getRules();	

			if (rules == null || rules.size() == 0) {
				return false;
			}
			
			Rule rule = rules.get(0);
			//int randomMutantIndex = getRandomNumber(0, 4);
					
			int randomMutantIndex = 3;
		    // add emoflon validator 
			
			switch(randomMutantIndex) {
			  case 0:
				  	 return addMutant_DeleteSourcePattern(rule); 
			  case 1:
				  	 return addMutant_DeleteTargetPattern(rule);
			  case 2:
				     return addMutant_DeleteCorrespondencePattern(rule);
			  case 3:
					 return addAMutant_AddSourcePattern(rule);
			  case 4:
					 return addAMutant_AddTargetPattern(rule);
			  case 5:
					 return addAMutant_AddCorrespondence(rule);
			  default:
			    return false;
			}
		}
		catch(Exception e) {
			return false;
		}
	}
	
	/** Add a mutant into the TGG rule, which deletes the last source pattern
	 * 
	 * @param Rule The TripleGraphGrammarFile.Rule rule file
	 * @return boolean Indicate that a source node was deleted successfully
	 * @throws CoreException if this method fails. The status code associated with exception reflects the cause of the failure.
	 */
	public boolean addMutant_DeleteSourcePattern(Rule rule) throws CoreException {
		EList<ObjectVariablePattern> sourceObjects = rule.getSourcePatterns();
		
		if (sourceObjects == null || sourceObjects.size() == 0) {
			return false;
		}
		
		boolean isSuccess = deleteLastNode(rule, true);
		
		return isSuccess;
	}
	
	/** Add a mutant into the TGG rule, which deletes the last target pattern
	 * 
	 * @param Rule The TripleGraphGrammarFile.Rule rule file
	 * @return boolean Indicate that a source node was deleted successfully
	 * @throws CoreException if this method fails. The status code associated with exception reflects the cause of the failure.
	 */
	public boolean addMutant_DeleteTargetPattern(Rule rule) throws CoreException {
		EList<ObjectVariablePattern> targetObjects = rule.getTargetPatterns();
		
		if (targetObjects == null || targetObjects.size() == 0) {
			return false;
		}
		
		boolean isSuccess = deleteLastNode(rule, false);
		
		return isSuccess;
	}
	
	/** Add a mutant into the TGG rule, which deletes the last Correspondence pattern
	 * 
	 * @param Rule The TripleGraphGrammarFile.Rule rule file
	 * @return boolean Indicate that a source node was deleted successfully
	 * @throws CoreException if this method fails. The status code associated with exception reflects the cause of the failure.
	 */
	public boolean addMutant_DeleteCorrespondencePattern(Rule rule) throws CoreException {	
		try {
			EList<CorrVariablePattern> corrPatterns = rule.getCorrespondencePatterns();
			
			if (corrPatterns == null || corrPatterns.size() == 0) {
				return false;
			}
			
			int indexToDelete = corrPatterns.size() - 1;
			corrPatterns.remove(indexToDelete);	
			
			return true;
		}
		catch(Exception e) {
			return false;
		}	
	}
	
	/** Introduce the mutant into the TGG file, which adds one source pattern  
	 * 
	 * @param Rule The TripleGraphGrammarFile.Rule rule file
	 * @return boolean Indicate that a source node was added successfully
	 * @throws CoreException if this method fails. The status code associated with exception reflects the cause of the failure.
	 */
	public boolean addAMutant_AddSourcePattern(Rule rule) throws CoreException {
		List<ObjectVariablePattern> sourceObjects = rule.getSourcePatterns();
		
		if (sourceObjects == null) {
			return false;
		}
		
		boolean isSuccess = addNode(rule, true);
		if (!isSuccess)
			return false;

		try {
			if (sourceObjects.size() > 1) {
				ObjectVariablePattern sourceObject = sourceObjects.get(sourceObjects.size() - 2);
				ObjectVariablePattern targetObject = sourceObjects.get(sourceObjects.size() - 1);
				Operator op = TggFactory.eINSTANCE.createOperator();
				op.setValue(DEFAULT_OPERATOR);
				// add a link
				addLinkEdge(rule, sourceObject, targetObject, op);
			}
			
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	/** Introduce the mutant into the TGG file, which adds one target pattern  
	 * 
	 * @param Rule The TripleGraphGrammarFile.Rule rule file
	 * @return boolean Indicate that a source node was added successfully
	 * @throws CoreException if this method fails. The status code associated with exception reflects the cause of the failure.
	 */
	public boolean addAMutant_AddTargetPattern(Rule rule) throws CoreException {
		List<ObjectVariablePattern> targetObjects = rule.getTargetPatterns();
		
		if (targetObjects == null) {
			return false;
		}
		
		boolean isSuccess = addNode(rule, false);
		if (!isSuccess)
			return false;
		
		if (targetObjects.size() > 1) {
			ObjectVariablePattern sourceObject = targetObjects.get(targetObjects.size() - 2);
			ObjectVariablePattern targetObject = targetObjects.get(targetObjects.size() - 1);
			Operator op = TggFactory.eINSTANCE.createOperator();
			op.setValue(DEFAULT_OPERATOR);
			// add a link
			addLinkEdge(rule, sourceObject, targetObject, op);
		}
		
		return true;
	}
	
	/** Introduce the mutant into the TGG file, which adds one correspondence pattern  
	 * 
	 * @param Rule The TripleGraphGrammarFile.Rule rule file
	 * @return boolean Indicate that a source node was added successfully
	 * @throws CoreException if this method fails. The status code associated with exception reflects the cause of the failure.
	 */
	public boolean addAMutant_AddCorrespondence(Rule rule) throws CoreException {
		List<CorrVariablePattern> corrObjects = rule.getCorrespondencePatterns();
		
		if (corrObjects == null) {
			return false;
		}
		
		boolean isSuccess = addCorrespondenceNode(rule);
		if (!isSuccess)
			return false;
		
		return true;
	}
	
	public boolean deleteLastNode(Rule rule, boolean isSourceNode) {
		List<CorrVariablePattern> correspondenceList = null;
		List<ObjectVariablePattern> sourceObjects = null;
		List<ObjectVariablePattern> targetObjects = null;
		List<ObjectVariablePattern> nodes = null;
		List<AttrCond> attrConditions = null;
		
		try {
			correspondenceList = rule.getCorrespondencePatterns();
			attrConditions = rule.getAttrConditions();
			sourceObjects  = rule.getSourcePatterns();
			targetObjects  = rule.getTargetPatterns();
			
			if (correspondenceList == null || sourceObjects == null || targetObjects == null)
				return false;
			
			if (isSourceNode) {
				nodes = sourceObjects;
			} else {
				nodes = targetObjects;
			}
			
			int indexToDelete = nodes.size() - 1;		
			String nodeNameToDelete = nodes.get(indexToDelete).getName();
			ObjectVariablePattern nodeToDelete = nodes.get(indexToDelete);
			
			// Search for and delete dependent attribute conditions
			List<AttrCond> attrConditionSelection = attrConditions.stream()
					.filter(c -> c.getValues().stream().anyMatch(p -> p instanceof AttributeExpression
							&& getObjectVariableName(((AttributeExpression) p).getObjectVar()).equals(nodeNameToDelete)))
					.collect(Collectors.toList());
			if (attrConditionSelection.size() > 0) {
				attrConditions.removeAll(attrConditionSelection);
			}

			// Search for and delete dependent links
			for (ObjectVariablePattern node: nodes) {							
				EList<LinkVariablePattern> linkVPs = node.getLinkVariablePatterns();
				List<Integer> listLinkIndexes = getIndexesToDelete(linkVPs, nodeNameToDelete);
				for (int index: listLinkIndexes) {
					linkVPs.remove(index);
				}
			}		
			
			List<Integer> listCorrIndexes = getCorrIndexesToDelete(correspondenceList, isSourceNode, nodeToDelete);
			for (int index: listCorrIndexes) {
				correspondenceList.remove(index);
			}
			
			// Delete the last node
			if (isSourceNode) {
				sourceObjects.remove(indexToDelete);
			} else {
				targetObjects.remove(indexToDelete);
			}
			
			return true;
		}
		catch(Exception e) {
			return false;
		}		
	}
	
	public boolean addNode(Rule rule, boolean isSourceNode) {
		Schema schema;
		List<ObjectVariablePattern> sourceObjects;
		List<ObjectVariablePattern> targetObjects;
		
		try {
			schema        = rule.getSchema();
			sourceObjects = rule.getSourcePatterns();
			targetObjects = rule.getTargetPatterns();
			
			if (schema == null || sourceObjects == null || targetObjects == null)
				return false;
	
			// Get all possible node types
			Map<String, List<EClassifier>> classifiers;
			if (isSourceNode) {
				classifiers = getClassifiersInPackageList(schema.getSourceTypes());
			} else {
				classifiers = getClassifiersInPackageList(schema.getTargetTypes());
			}	
			List<EClassifier> outputList = combineObjectClassifierLists(classifiers);
	
			// Define a node name and type
			int classifierIndex = getRandomNumber(0, outputList.size() - 1);
			EClass type = (EClass)outputList.get(classifierIndex);
			String nodeName = "mutant" + System.currentTimeMillis();
	
			// Create a new node
			ObjectVariablePattern node = createNode(nodeName, type);
			if (node == null)
				return false;
	
			// Add the new node to the TGG rule
			if (isSourceNode)
				sourceObjects.add(node);
			else
				targetObjects.add(node);
			
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	public boolean addCorrespondenceNode(Rule rule) {
		Schema schema;
		List<CorrVariablePattern> corrList;
		List<ObjectVariablePattern> sourceObjects;
		List<ObjectVariablePattern> targetObjects;
		
		try {
			schema        = rule.getSchema();
			corrList      = rule.getCorrespondencePatterns();
			sourceObjects = rule.getSourcePatterns();
			targetObjects = rule.getTargetPatterns();
			
			if (schema == null || corrList == null || sourceObjects == null || targetObjects == null
					|| sourceObjects.size() == 0 || targetObjects.size() == 0)
				return false;
			
			// Get type
			List<CorrType> corrTypes = schema.getCorrespondenceTypes();
			int corrIndex = getRandomNumber(0, corrTypes.size() - 1);
			CorrType type = corrTypes.get(corrIndex);
			
			ObjectVariablePattern source = sourceObjects.get(0);
			ObjectVariablePattern target = targetObjects.get(0);
			String corrName = "mutant" + System.currentTimeMillis();
			
			CorrVariablePattern correspondence = TggFactory.eINSTANCE.createCorrVariablePattern();
			correspondence.setType(type);
			correspondence.setSource(source);
			correspondence.setTarget(target);
			correspondence.setName(corrName);

			// Set the default operator DEFAULT_OPERATOR for the new correspondence
			Operator op = TggFactory.eINSTANCE.createOperator();
			op.setValue(DEFAULT_OPERATOR);
			correspondence.setOp(op);

			// Add the new correspondence to the TGG rule
			corrList.add(correspondence);
			
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	/** Create ObjectVariablePattern object
	 * 
	 * @param String The ObjectVariablePattern name
	 * @param EClass The ObjectVariablePattern type
	 * @return ObjectVariablePattern The created node
	 */
	public ObjectVariablePattern createNode(String nodeName, EClass type)  {
		try {
			// Create a new node
			ObjectVariablePattern node = TggFactory.eINSTANCE.createObjectVariablePattern();
			node.setName(nodeName);
			node.setType(type);

			// Set the default operator DEFAULT_OPERATOR for the new correspondence
			Operator op = TggFactory.eINSTANCE.createOperator();
			op.setValue(DEFAULT_OPERATOR);
			node.setOp(op);
			
			return node;
		}
		catch(Exception e) {
			return null;
		}
	}
	
	private String getObjectVariableName(EObject objVar) {
		String objVarName = "";
		if (objVar instanceof ObjectVariablePattern) {
			objVarName = ((ObjectVariablePattern) objVar).getName();
		} else if (objVar instanceof ContextObjectVariablePattern) {
			objVarName = ((ContextObjectVariablePattern) objVar).getName();
		}

		return objVarName;
	}
	
	/** Get a list of classifiers inside a package
	 * 
	 * @param List<EPackage> The list of packages
	 * @return Map<String, List<EClassifier>> The list of the classifiers inside that package
	 */
	private Map<String, List<EClassifier>> getClassifiersInPackageList(List<EPackage> packages) {
		// Key: Package name, Value: List of the classifiers inside that package
		Map<String, List<EClassifier>> classifierNames = new HashMap<String, List<EClassifier>>();
		for (EPackage p : packages) {
			classifierNames.put(p.getName(), p.getEClassifiers());
		}

		return classifierNames;
	}
	
	/** Get a list of a types inside a list of classifiers
	 * 
	 * @param Map<String, List<EClassifier>> The list of classifiers
	 * @return List<EClassifier> The list of types
	 */
	private List<EClassifier> combineObjectClassifierLists(Map<String, List<EClassifier>> input) {
		Set<String> keys = input.keySet();
		List<EClassifier> outputList = new ArrayList<EClassifier>();
		for (String key : keys) {
			outputList.addAll(input.get(key));
		}

		return outputList;
	}
	
	
	public boolean addLinkEdge(Rule rule,
			ObjectVariablePattern sourceObject, 
			ObjectVariablePattern targetObject, 
			Operator op) {
		try {	
			EClass targetType = targetObject.getType();
			
			List<EReference> references = sourceObject.getType().getEAllReferences();			
			for (EReference reference : references) {
				EClass referenceType = reference.getEReferenceType();					
				
				if (referenceType == targetType) {
					LinkVariablePattern link = TggFactory.eINSTANCE.createLinkVariablePattern();
					link.setTarget(targetObject);
					link.setType(reference);
					link.setOp(op);
					sourceObject.getLinkVariablePatterns().add(link);
					
					return true;
				}
			};
			return false;							
		}
		catch(Exception e) {
			return false;
		}
	}
	
	
	/** Get a list of link (edge) indexes, which are related to the target pattern
	 * 
	 * @param linkVPs The list of LinkVariablePattern objects (edges)
	 * @param nameOriginal The name of pattern
	 * @return List<Integer> The list of link (edge) indexes
	 */
	public List<Integer> getIndexesToDelete(EList<LinkVariablePattern> linkVPs, String namePattern) {
		int linkIndex = 0;
		try {
			List<Integer> listVPIndexes = new ArrayList<Integer>();
			for (LinkVariablePattern linkVP: linkVPs) {
				ObjectVariablePattern linkTarget = linkVP.getTarget();
				String nameLink = linkTarget.getName();
				
				if (namePattern == nameLink) {
					listVPIndexes.add(linkIndex);
				}			
				linkIndex++; 
			}		
			
			return listVPIndexes;
		}
		catch(Exception e) {
			return new ArrayList<Integer>();
		}
	}
	
	public List<Integer> getCorrIndexesToDelete(List<CorrVariablePattern> correspondenceList, 
			boolean isSourceNode, ObjectVariablePattern nodeToDelete) {
		int corrIndex = 0;
		try {
			List<Integer> listCorrIndexes = new ArrayList<Integer>();
			for (CorrVariablePattern corr : correspondenceList) {
				if (isSourceNode && corr.getSource() == nodeToDelete 
				|| !isSourceNode && corr.getTarget() == nodeToDelete) {
					listCorrIndexes.add(corrIndex);
				}	
				corrIndex++;
			}
			
			return listCorrIndexes;
		}
		catch(Exception e) {
			return new ArrayList<Integer>();
		}
	}
	
	/** Generate a random number between min and max
	 * 
	 * @param max The max possible value
	 * @param min The min possible value
	 * @return int A random number between min and max
	 */
	public int getRandomNumber(int min, int max) {
		if (max == min)
			return min;
		
		int range = max - min + 1; 		
		int rand = (int)(Math.random() * range) + min;
		
		return rand;
	}
}
