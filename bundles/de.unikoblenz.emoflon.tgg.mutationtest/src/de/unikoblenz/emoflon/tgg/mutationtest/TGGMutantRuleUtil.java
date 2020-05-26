package de.unikoblenz.emoflon.tgg.mutationtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
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

import de.unikoblenz.emoflon.tgg.mutationtest.util.MutantResult;


public class TGGMutantRuleUtil {
	
	private final String DEFAULT_OPERATOR = "++";
	
	private HashMap<String, Set<Integer>> appliedMutantsAndIndexesHash;
			
	/**
	 * Creates a new mutant rule util 
	 */
	public TGGMutantRuleUtil() throws  CoreException {
		appliedMutantsAndIndexesHash = new HashMap<String, Set<Integer>>(); 
	}
	
	public MutantResult getMutantRule(List<Rule> rules) {
		Rule rule = null;
		MutantResult mutantResult = null;
		String ruleName = "temp rule"; 				
		
		try {			
			if (rules == null || rules.isEmpty()) {
				return null;
			}			
			
			Set<Integer> appliedIndexes = new HashSet<Integer>();
			Set<Integer> mutantIndexes = new HashSet<Integer>(); 
			mutantIndexes.addAll(Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5})); 
			
			Random rand = new Random();
			Set<Integer> randIndexes = new HashSet<Integer>(); 
			int size = rules.size();
			int randIndex;
						
			for (int i = 0; i < size; i++) {								
				do {
					// Obtain a random number between [0 - (size - 1)]
					randIndex = rand.nextInt(size);				
					if (!randIndexes.contains(randIndex)) {
						randIndexes.add(randIndex);
						break;
					}					
				}
				while(true);
				
				Rule tempRule = rules.get(randIndex);
				
				// Should be an item HashMap key
				ruleName = tempRule.getName();			
				appliedIndexes = appliedMutantsAndIndexesHash.get(ruleName);
				
				// Check if there is already a list for the current key 				
				if(appliedIndexes == null) { 	
					// If not, continue working with this rule
					rule = tempRule;
					appliedIndexes = new HashSet<Integer>();
					// break;
				}
				else {
					// Check if all mutants for the current rule has been already applied
					if (appliedIndexes.size() == mutantIndexes.size()) {
						// If so, try another rule
						continue;						
					}
					else {
						// Delete applied indexes from mutantIndexes
						mutantIndexes.removeAll(appliedIndexes);
						
						// And continue working with this rule
						rule = tempRule;
						// break;
					}
				}								
								
				// Perform mutations
				List<Integer> mutantIndexesList = new ArrayList<>(mutantIndexes);
				Collections.shuffle(mutantIndexesList);
				mutantResult = null;

				// Loop possible mutants for a single rule
				for (Integer mutantIndex : mutantIndexesList) {
					// Add index, which will be applied to the list
					appliedIndexes.add(mutantIndex);
					
					switch (mutantIndex) {
					case 0:
						// Delete source pattern node
						mutantResult = addMutant_DeletePattern(rule, true);						
						break;
					case 1:
						// Delete target pattern node
						mutantResult = addMutant_DeletePattern(rule, false);
						break;
					case 2:
						// Delete correspondence node
						mutantResult = addMutant_DeleteCorrespondencePattern(rule);
						break;
					case 3:
						// Add source pattern node
						mutantResult = addAMutant_AddPattern(rule, true);
						break;
					case 4:
						// Add target pattern node
						mutantResult = addAMutant_AddPattern(rule, false);
						break;
					case 5:
						// Add correspondence node
						mutantResult = addAMutant_AddCorrespondence(rule);
						break;
					default:
						mutantResult = null;
					}
					if (mutantResult.isSuccess()) {
						// Save appliedIndexes to HashMap
						appliedMutantsAndIndexesHash.put(ruleName, appliedIndexes);			
						
						return mutantResult;
					}
				}
				// Save appliedIndexes to HashMap
				appliedMutantsAndIndexesHash.put(ruleName, appliedIndexes);			
			}
			mutantResult = new MutantResult(null);
			mutantResult.setSuccess(false);
			mutantResult.setErrorText("All possible mutants for all rules in a file have been already checked ");
			return mutantResult;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// ========================== Add nodes mutants
	// ================================//

	/**
	 * Introduce the mutant into the TGG file, which adds one source or target
	 * pattern
	 * 
	 * @param Rule The TripleGraphGrammarFile.Rule rule file
	 * @return boolean Indicate that a source node was added successfully
	 * @throws CoreException if this method fails. The status code associated with
	 *                       exception reflects the cause of the failure.
	 */
	public MutantResult addAMutant_AddPattern(Rule rule, boolean isSourceNode) throws CoreException {
		EList<ObjectVariablePattern> nodes;
		ObjectVariablePattern newNode;
		Schema schema;

		MutantResult mutantResult = new MutantResult(rule);
		mutantResult.setMutationName(isSourceNode ? "AddSourcePattern" : "AddTargetPattern");

		try {
			schema = rule.getSchema();
			if (schema == null) {
				mutantResult.setErrorText("schema is null");
				return mutantResult;
			}

			nodes = isSourceNode ? rule.getSourcePatterns() : rule.getTargetPatterns();
			if (nodes == null || nodes.size() == 0) {
				mutantResult.setErrorText("SourcePatterns or TargetPatterns are null");
				return mutantResult;
			}

			newNode = createNode(schema, nodes, true, mutantResult);
			if (newNode == null) {
				return mutantResult;
			}

			if (nodes.size() > 1) {
				ObjectVariablePattern sourceObject = nodes.get(nodes.size() - 1);
				ObjectVariablePattern targetObject = newNode;
				Operator op = TggFactory.eINSTANCE.createOperator();
				op.setValue(DEFAULT_OPERATOR);
				// Add a link
				LinkVariablePattern link = createLinkEdge(rule, sourceObject, targetObject, op, mutantResult);

				if (link == null) {
					mutantResult.setErrorText("there is no valid link");
					return mutantResult;
				}

				// Add the new node to the TGG rule
				nodes.add(newNode);
				sourceObject.getLinkVariablePatterns().add(link);

				List<String> listLinkNames = new ArrayList<String>();
				listLinkNames.add(link.getType().getName());
				
				mutantResult.setSuccess(true);
				fillMutantDeleteResult(mutantResult, newNode, listLinkNames, null, targetObject.getName());
			}
			
			return mutantResult;
		} catch (Exception e) {
			e.printStackTrace();
			mutantResult.setErrorText("addAMutant_AddPattern: " + e.getMessage());
			return mutantResult;
		}
	}

	public ObjectVariablePattern createNode(Schema schema, List<ObjectVariablePattern> nodes, boolean isSourceNode,
			MutantResult mutantResult) {
		Map<String, List<EClassifier>> classifiers;
		try {
			// Get all possible node types
			classifiers = isSourceNode ? getClassifiersInPackageList(schema.getSourceTypes())
					: getClassifiersInPackageList(schema.getTargetTypes());
			if (classifiers == null || classifiers.size() == 0) {
				mutantResult.setErrorText("it is not possible to get classifiers");
				return null;
			}
			List<EClassifier> outputList = combineObjectClassifierLists(classifiers);

			// Define a node name and type
			int classifierIndex = getRandomNumber(0, outputList.size() - 1);

			EClass type = (EClass) outputList.get(classifierIndex);
			String nodeName = "mutant" + System.currentTimeMillis();

			// Create a new node
			ObjectVariablePattern node = TggFactory.eINSTANCE.createObjectVariablePattern();
			node.setName(nodeName);
			node.setType(type);

			// Set the default operator DEFAULT_OPERATOR for the new correspondence
			Operator op = TggFactory.eINSTANCE.createOperator();
			op.setValue(DEFAULT_OPERATOR);
			node.setOp(op);

			return node;
		} catch (Exception e) {
			e.printStackTrace();
			mutantResult.setErrorText("createNode: " + e.getMessage());
			return null;
		}
	}

	public LinkVariablePattern createLinkEdge(Rule rule, ObjectVariablePattern sourceObject,
			ObjectVariablePattern targetObject, Operator op, MutantResult mutantResult) {
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

					return link;
				}
			}
			;
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			mutantResult.setErrorText("createLinkEdge: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Introduce the mutant into the TGG file, which adds one correspondence pattern
	 * 
	 * @param Rule The TripleGraphGrammarFile.Rule rule file
	 * @return boolean Indicate that a source node was added successfully
	 * @throws CoreException if this method fails. The status code associated with
	 *                       exception reflects the cause of the failure.
	 */
	public MutantResult addAMutant_AddCorrespondence(Rule rule) throws CoreException {
		List<CorrVariablePattern> corrObjects = rule.getCorrespondencePatterns();
		MutantResult mutantResult = new MutantResult(rule);
		mutantResult.setMutationName("AddCorrespondence");

		if (corrObjects == null) {
			mutantResult.setErrorText("CorrespondencePatterns are null");
			return mutantResult;
		}

		CorrVariablePattern correspondence = createCorrespondenceNode(rule, mutantResult);
		if (correspondence == null) {
			return mutantResult;
		}

		// Add the new correspondence to the TGG rule
		corrObjects.add(correspondence);

		mutantResult.setSuccess(true);
		return mutantResult;
	}

	public CorrVariablePattern createCorrespondenceNode(Rule rule, MutantResult mutantResult) {
		Schema schema;
		List<CorrVariablePattern> corrList;
		List<ObjectVariablePattern> sourceObjects;
		List<ObjectVariablePattern> targetObjects;

		try {
			schema = rule.getSchema();
			EcoreUtil.resolve(schema, rule.eResource().getResourceSet());
			corrList = rule.getCorrespondencePatterns();
			sourceObjects = rule.getSourcePatterns();
			targetObjects = rule.getTargetPatterns();

			if (schema == null || corrList == null || sourceObjects == null || targetObjects == null
					|| sourceObjects.size() == 0 || targetObjects.size() == 0) {
				mutantResult.setErrorText("schema, CorrespondencePatterns, SourcePatterns or TargetPatterns is null");
				return null;
			}
				

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

			fillCorrMutantResult(mutantResult, correspondence);

			return correspondence;
		} catch (Exception e) {
			e.printStackTrace();
			mutantResult.setErrorText("createCorrespondenceNode: " + e.getMessage());
			return null;
		}
	}

	// ========================== Delete node mutants
	// ================================//
	/**
	 * Add a mutant into the TGG rule, which deletes the last Correspondence pattern
	 * 
	 * @param Rule The TripleGraphGrammarFile.Rule rule file
	 * @return boolean Indicate that a source node was deleted successfully
	 * @throws CoreException if this method fails. The status code associated with
	 *                       exception reflects the cause of the failure.
	 */
	public MutantResult addMutant_DeleteCorrespondencePattern(Rule rule) throws CoreException {
		MutantResult mutantResult = new MutantResult(rule);
		mutantResult.setMutationName("DeleteCorrespondencePattern");

		try {
			EList<CorrVariablePattern> corrPatterns = rule.getCorrespondencePatterns();

			if (corrPatterns == null || corrPatterns.size() == 0) {
				mutantResult.setErrorText("SourcePatterns or TargetPatterns is null");
				return mutantResult;
			}

			int indexToDelete = corrPatterns.size() - 1;
			fillCorrMutantResult(mutantResult, corrPatterns.get(indexToDelete));

			corrPatterns.remove(indexToDelete);

			mutantResult.setSuccess(true);
			return mutantResult;
		} catch (Exception e) {
			e.printStackTrace();
			mutantResult.setErrorText(e.getMessage());
			return mutantResult;
		}
	}

	/**
	 * Add a mutant into the TGG rule, which deletes the source or target pattern
	 * node
	 * 
	 * @param Rule The TripleGraphGrammarFile.Rule rule file
	 * @return boolean Indicate that a source node was deleted successfully
	 * @throws CoreException if this method fails. The status code associated with
	 *                       exception reflects the cause of the failure.
	 */
	public MutantResult addMutant_DeletePattern(Rule rule, boolean isSourceNode) {
		EList<ObjectVariablePattern> nodes;
		List<CorrVariablePattern> correspondenceList;
		List<AttrCond> attrConditions;

		MutantResult mutantResult = new MutantResult(rule);
		mutantResult.setMutationName(isSourceNode ? "DeleteSourcePattern" : "DeleteTargetPattern");

		try {
			nodes = isSourceNode ? rule.getSourcePatterns() : rule.getTargetPatterns();
			correspondenceList = rule.getCorrespondencePatterns();
			attrConditions = rule.getAttrConditions();

			if (nodes == null || nodes.size() == 0 || correspondenceList == null) {
				mutantResult.setErrorText("nodes or correspondenceList is null");
				return mutantResult;
			}
			if (nodes.size() < 4) {
				mutantResult.setErrorText("mutated model size is less than 3");
				return mutantResult;
			}

			mutantResult.setSuccess(deleteNode(nodes, correspondenceList, attrConditions, isSourceNode, mutantResult));
			return mutantResult;
		} catch (Exception e) {
			e.printStackTrace();
			mutantResult.setErrorText("addMutant_DeleteCorrespondencePattern: " + e.getMessage());
			return mutantResult;
		}
	}

	public boolean deleteNode(List<ObjectVariablePattern> nodes, List<CorrVariablePattern> correspondenceList,
			List<AttrCond> attrConditions, boolean isSourceNode, MutantResult mutantResult) {
		String nodeNameToDelete;
		try {
			// Get a node, which does not make a model invalid if deleted
			ObjectVariablePattern nodeToDelete = getNodeToDelete(nodes, mutantResult);
			if (nodeToDelete == null)
				return false;

			nodeNameToDelete = nodeToDelete.getName();

			// Search for and delete related attribute conditions
			List<AttrCond> attrConditionSelection = attrConditions.stream()
					.filter(c -> c.getValues().stream()
							.anyMatch(p -> p instanceof AttributeExpression
									&& getObjectVariableName(((AttributeExpression) p).getObjectVar())
											.equals(nodeNameToDelete)))
					.collect(Collectors.toList());
			if (attrConditionSelection.size() > 0) {
				attrConditions.removeAll(attrConditionSelection);
			}

			List<String> listLinkNames = new ArrayList<String>();
			// Search for and delete related links
			for (ObjectVariablePattern node : nodes) {
				EList<LinkVariablePattern> linkVPs = node.getLinkVariablePatterns();
				List<Integer> listLinkIndexes = getIndexesToDelete(linkVPs, nodeNameToDelete);
				for (int index : listLinkIndexes) {
					listLinkNames.add(linkVPs.get(index).getType().getName());
					linkVPs.remove(index);
				}
			}

			List<String> listCorrNames = new ArrayList<String>();
			// Search for and delete correspondence links
			List<Integer> listCorrIndexes = getCorrIndexesToDelete(correspondenceList, isSourceNode, nodeToDelete);
			for (int index : listCorrIndexes) {
				listCorrNames.add(correspondenceList.get(index).getName());
				correspondenceList.remove(index);
			}

			fillMutantDeleteResult(mutantResult, nodeToDelete, listLinkNames, listCorrNames, null);
			// Delete the node
			nodes.remove(nodeToDelete);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			mutantResult.setErrorText("deleteNode: " + e.getMessage());
			return false;
		}
	}

	public ObjectVariablePattern getNodeToDelete(List<ObjectVariablePattern> nodes, MutantResult mutantResult) {
		ObjectVariablePattern nodeToDelete;
		int newModelSize;
		try {
			newModelSize = nodes.size() - 1;
			for (int i = newModelSize; i > 2; i--) {
				// Get a potential node to delete
				nodeToDelete = nodes.get(i);

				// Check if a model is connected
				boolean isModelValid = checkIfModelValid(nodes, nodeToDelete, mutantResult);
				if (isModelValid) {
					return nodeToDelete;
				}
			}
			mutantResult.setErrorText("It is not possible to delete a single node so that a model is valid");
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			mutantResult.setErrorText("getNodeToDelete: " + e.getMessage());
			return null;
		}
	}

	class Graph {
		// Create a class that represent a graph of the given model.
		// The graph is an array of adjacency lists.
		// The number of vertices in the graph is the size of the array
		// A graph vertex is a model node
		// A graph edge is a model link

		int num_Vertices;
		LinkedList<Integer>[] adjListArray;

		// Constructor
		@SuppressWarnings("unchecked")
		Graph(int num_Vertices) {
			this.num_Vertices = num_Vertices;

			// Create a list of vertices
			this.adjListArray = new LinkedList[num_Vertices];

			// Create a list for each vertex so that adjacent nodes can be stored
			for (int i = 0; i < num_Vertices; i++) {
				adjListArray[i] = new LinkedList<>();
			}

		}

		// Adds an edge to an undirected graph
		void addEdge(int sourceVertice, int targetVertice) {
			// Add an edge from source to target
			adjListArray[sourceVertice].add(targetVertice);

			// Add an edge from target to source
			// since a graph is undirected,
			adjListArray[targetVertice].add(sourceVertice);
		}

		void DFS(int vertice, boolean[] isVisitedVertices) {
			// Mark the current node as visited
			isVisitedVertices[vertice] = true;

			// Make recursion for all the vertices adjacent to this vertex
			for (int neighbor : adjListArray[vertice]) {
				if (!isVisitedVertices[neighbor]) {
					DFS(neighbor, isVisitedVertices);
				}
			}
		}

		boolean isConnected() {
			// Create an array of visited vertices
			boolean[] isVisitedVertices = new boolean[num_Vertices];

			// Start the DFS from vertex 0
			DFS(0, isVisitedVertices);

			// Check if all the vertices are visited,
			// if yes then the graph is connected
			int count = 0;
			for (int i = 0; i < isVisitedVertices.length; i++) {
				if (isVisitedVertices[i]) {
					count++;
				}
			}

			return (num_Vertices == count);
		}
	}

	private boolean checkIfModelValid(List<ObjectVariablePattern> nodes, ObjectVariablePattern nodeToDelete,
			MutantResult mutantResult) {
		try {
			// Map names to numbers
			LinkedList<String> vertices = new LinkedList<>();

			// === Create a list of vertices === //
			String name;
			for (ObjectVariablePattern node : nodes) {
				if (node == nodeToDelete) {
					continue;
				}
				name = node.getName();
				if (!vertices.contains(name)) {
					vertices.add(name);
				}
			}

			Graph graph = new Graph(vertices.size());

			// === Create a list of edges === //
			addEdgesToGraphMappedFromLinks(graph, nodes, vertices, nodeToDelete);
			boolean isConnected = graph.isConnected();
			return isConnected;
		} catch (Exception e) {
			mutantResult.setErrorText("checkIfModelValid: " + e.getMessage());
			return false;
		}
	}

	private void addEdgesToGraphMappedFromLinks(Graph graph, List<ObjectVariablePattern> nodes,
			LinkedList<String> vertices, ObjectVariablePattern nodeToDelete) {
		EList<LinkVariablePattern> linkVPs;
		for (ObjectVariablePattern node : nodes) {
			if (node == nodeToDelete) {
				continue;
			}

			linkVPs = node.getLinkVariablePatterns();
			for (LinkVariablePattern link : linkVPs) {
				if (link.getTarget() != nodeToDelete) {
					int source = vertices.indexOf(node.getName());
					int target = vertices.indexOf(link.getTarget().getName());
					graph.addEdge(source, target);

				}
			}
		}

	}

	/**
	 * Get a list of link (edge) indexes, which are related to the target pattern
	 * 
	 * @param linkVPs      The list of LinkVariablePattern objects (edges)
	 * @param nameOriginal The name of pattern
	 * @return List<Integer> The list of link (edge) indexes
	 */
	public List<Integer> getIndexesToDelete(EList<LinkVariablePattern> linkVPs, String namePattern) {
		int linkIndex = 0;
		try {
			List<Integer> listVPIndexes = new ArrayList<Integer>();
			for (LinkVariablePattern linkVP : linkVPs) {
				ObjectVariablePattern linkTarget = linkVP.getTarget();
				String nameLink = linkTarget.getName();

				if (namePattern == nameLink) {
					listVPIndexes.add(linkIndex);
				}
				linkIndex++;
			}

			return listVPIndexes;
		} catch (Exception e) {
			return new ArrayList<Integer>();
		}
	}

	public List<Integer> getCorrIndexesToDelete(List<CorrVariablePattern> correspondenceList, boolean isSourceNode,
			ObjectVariablePattern nodeToDelete) {
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
		} catch (Exception e) {
			return new ArrayList<Integer>();
		}
	}

	// ========================= Common methods
	// ============================================//

	/**
	 * Generate a random number between min and max
	 * 
	 * @param max The max possible value
	 * @param min The min possible value
	 * @return int A random number between min and max
	 */
	public int getRandomNumber(int min, int max) {
		if (max == min)
			return min;

		int range = max - min + 1;
		int rand = (int) (Math.random() * range) + min;

		return rand;
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

	/**
	 * Get a list of classifiers inside a package
	 * 
	 * @param List<EPackage> The list of packages
	 * @return Map<String, List<EClassifier>> The list of the classifiers inside
	 *         that package
	 */
	private Map<String, List<EClassifier>> getClassifiersInPackageList(List<EPackage> packages) {
		// Key: Package name, Value: List of the classifiers inside that package
		Map<String, List<EClassifier>> classifierNames = new HashMap<String, List<EClassifier>>();
		for (EPackage p : packages) {
			classifierNames.put(p.getName(), p.getEClassifiers());
		}

		return classifierNames;
	}

	/**
	 * Get a list of a types inside a list of classifiers
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

	private void fillCorrMutantResult(MutantResult mutantResult, CorrVariablePattern corrMutantNode) {
		mutantResult.setNodeName(corrMutantNode.getName());
		mutantResult.setNodeType(corrMutantNode.getType().getName());
		mutantResult.setNodeOperator(corrMutantNode.getOp().getValue());
		mutantResult.setNodeSourceName(corrMutantNode.getSource().getName());
		mutantResult.setNodeTargetName(corrMutantNode.getTarget().getName());
	}

	private void fillMutantDeleteResult(MutantResult mutantResult, ObjectVariablePattern mutantNode,
			List<String> listLinkNames, List<String> listCorrNames, String targetName) {
		mutantResult.setNodeName(mutantNode.getName());
		mutantResult.setNodeType(mutantNode.getType().getName());
		mutantResult.setNodeOperator(mutantNode.getOp().getValue());

		mutantResult.setListLinkNames(listLinkNames);
		mutantResult.setListCorrNames(listCorrNames);
		mutantResult.setNodeTargetName(targetName);
	}

}