package de.unikoblenz.emoflon.tgg.mutationtest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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
		try (InputStream fileContent = file.getContents()) {
			ruleResource.load(fileContent, Collections.emptyMap());
			EcoreUtil.resolveAll(resourceSet);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return (TripleGraphGrammarFile) ruleResource.getContents().get(0);
	}
	
	public boolean getMutantRule(TripleGraphGrammarFile tggFile) {
		try {
			List<Rule> rules = tggFile.getRules();	

			if (rules == null || rules.size() == 0) {
				return false;
			}
			
			
			boolean isSuccess = false;
			Rule rule = rules.get(0);

			
			Integer[] indexes = new Integer[] {0, 1, 2, 3, 4, 5};
			List<Integer> randomIndexes = Arrays.asList(indexes);
			Collections.shuffle(randomIndexes);
			
			for (Integer index : randomIndexes) {					
				switch(index) {
				  case 0:
					  // Delete source pattern node
					  isSuccess =  addMutant_DeletePattern(rule, true); 
					  break;
				  case 1:
					  // Delete target pattern node
					  isSuccess =  addMutant_DeletePattern(rule, false);
					  break;
				  case 2:
					  // Delete correspondence node
					  isSuccess =  addMutant_DeleteCorrespondencePattern(rule);
					  break;
				  case 3:
					  // Add source pattern node
					  isSuccess =  addAMutant_AddPattern(rule, true);
					  break;
				  case 4:
					  // Add target pattern node
					  isSuccess =  addAMutant_AddPattern(rule, false);
					  break;
				  case 5:
					  // Add correspondence node
					  isSuccess =  addAMutant_AddCorrespondence(rule);
					  break;
				  default:
					  isSuccess =  false;
				}
				if (isSuccess)
					return true;
			}
			return false;
		}
		catch(Exception e) {
			System.out.println(e);
			return false;
		}
	}
	
	// ========================== Add nodes mutants ================================//
	
	/** Introduce the mutant into the TGG file, which adds one source or target pattern  
	 * 
	 * @param Rule The TripleGraphGrammarFile.Rule rule file
	 * @return boolean Indicate that a source node was added successfully
	 * @throws CoreException if this method fails. 
	 * The status code associated with exception reflects the cause of the failure.
	 */
	public boolean addAMutant_AddPattern(Rule rule, boolean isSourceNode) 
			throws CoreException 
	{
		EList<ObjectVariablePattern> nodes;	
		ObjectVariablePattern newNode;
		Schema schema;
		try {
			schema = rule.getSchema();
			if (schema == null)
				return false;
			
			nodes = isSourceNode ? rule.getSourcePatterns() : rule.getTargetPatterns();					
			if (nodes == null || nodes.size() == 0) {
				return false;
			}
			
			newNode = createNode(schema, nodes, true);
			if (newNode == null)
				return false;
		
			if (nodes.size() > 1) {
				ObjectVariablePattern sourceObject = nodes.get(nodes.size() - 1);
				ObjectVariablePattern targetObject = newNode;
				Operator op = TggFactory.eINSTANCE.createOperator();
				op.setValue(DEFAULT_OPERATOR);
				// Add a link
				LinkVariablePattern link = createLinkEdge(rule, sourceObject, targetObject, op);
				
				if (link == null)
					return false;
				// Add the new node to the TGG rule
				nodes.add(newNode);
				sourceObject.getLinkVariablePatterns().add(link);
			}
			
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
			
	public ObjectVariablePattern createNode(
			Schema schema, 
			List<ObjectVariablePattern> nodes, 
			boolean isSourceNode) 
	{
		Map<String, List<EClassifier>> classifiers;
		try {	
			// Get all possible node types
			classifiers = isSourceNode 
					? getClassifiersInPackageList(schema.getSourceTypes()) 
					: getClassifiersInPackageList(schema.getTargetTypes());				
			if (classifiers == null || classifiers.size() == 0) {
				return null;
			}
			List<EClassifier> outputList = combineObjectClassifierLists(classifiers);
	
			// Define a node name and type
			int classifierIndex = getRandomNumber(0, outputList.size() - 1);
			
			EClass type = (EClass)outputList.get(classifierIndex);
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
		}
		catch(Exception e) {
			return null;
		}
	}
	
	public LinkVariablePattern createLinkEdge(Rule rule,
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
					
					return link;
				}
			};
			return null;							
		}
		catch(Exception e) {
			return null;
		}
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
		
		CorrVariablePattern correspondence = createCorrespondenceNode(rule);
		if (correspondence == null)
			return false;
		
		// Add the new correspondence to the TGG rule
		corrObjects.add(correspondence);
		
		return true;
	}

	public CorrVariablePattern createCorrespondenceNode(Rule rule) {
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
				return null;
			
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
			
			return correspondence;
		}
		catch(Exception e) {
			return null;
		}
	}
	
	// ========================== Delete node mutants ================================//
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
	
	/** Add a mutant into the TGG rule, which deletes the source or target pattern node
	 * 
	 * @param Rule The TripleGraphGrammarFile.Rule rule file
	 * @return boolean Indicate that a source node was deleted successfully
	 * @throws CoreException if this method fails. 
	 * The status code associated with exception reflects the cause of the failure.
	 */
	public boolean addMutant_DeletePattern(Rule rule, boolean isSourceNode) 
	{
		EList<ObjectVariablePattern> nodes;
		List<CorrVariablePattern> correspondenceList;
		List<AttrCond> attrConditions;
		try {		
			nodes = isSourceNode ? rule.getSourcePatterns() : rule.getTargetPatterns();
			correspondenceList = rule.getCorrespondencePatterns();
			attrConditions = rule.getAttrConditions();
			
			if (nodes == null || nodes.size() == 0 || correspondenceList == null) {
				return false;
			}
			
			return deleteNode(nodes, correspondenceList, attrConditions, isSourceNode);	
		}
		catch(Exception e) {
			return false;
		}
	}
	
	public boolean deleteNode(
			List<ObjectVariablePattern> nodes, 
			List<CorrVariablePattern> correspondenceList,
			List<AttrCond> attrConditions,
			boolean isSourceNode) 
	{
		String nodeNameToDelete;
		try {			
			// Get a node, which does not make a model invalid if deleted
			ObjectVariablePattern nodeToDelete = getNodeToDelete(nodes);			
			if (nodeToDelete == null)
				return false;
			
			nodeNameToDelete = nodeToDelete.getName();

			// Search for and delete related attribute conditions
			List<AttrCond> attrConditionSelection = attrConditions.stream()
					.filter(c -> c.getValues().stream().anyMatch
							(p -> p instanceof AttributeExpression
							&& getObjectVariableName(((AttributeExpression) p).getObjectVar())
							.equals(nodeNameToDelete)))
					.collect(Collectors.toList());
			if (attrConditionSelection.size() > 0) {
				attrConditions.removeAll(attrConditionSelection);
			}

			// Search for and delete related links
			for (ObjectVariablePattern node: nodes) {							
				EList<LinkVariablePattern> linkVPs = node.getLinkVariablePatterns();
				List<Integer> listLinkIndexes = getIndexesToDelete(linkVPs, nodeNameToDelete);
				for (int index: listLinkIndexes) {
					linkVPs.remove(index);
				}
			}		
			
			// Search for and delete correspondence links
			List<Integer> listCorrIndexes = getCorrIndexesToDelete(correspondenceList, isSourceNode, nodeToDelete);
			for (int index: listCorrIndexes) {
				correspondenceList.remove(index);
			}
			
			// Delete the node
			nodes.remove(nodeToDelete);
			
			return true;
		}
		catch(Exception e) {
			return false;
		}		
	}
	
	public ObjectVariablePattern getNodeToDelete(List<ObjectVariablePattern> nodes) 
	{
		ObjectVariablePattern nodeToDelete;
		int newModelSize;
		try {						
			
			newModelSize = nodes.size() - 1;
			if (newModelSize < 3) {
				return null;
			}			
	
			for (int i = newModelSize; i > 2; i--) {				
				// Get a potential node to delete 
				nodeToDelete = nodes.get(i);
				
				// Check if a model is connected
				boolean isModelValid = checkIfModelValid(nodes,  nodeToDelete);
				if (isModelValid) {
					return nodeToDelete;
				}
			}
			return null;
		}
		catch(Exception e) {
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
	        for(int i = 0; i < num_Vertices ; i++){ 
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
	            if(!isVisitedVertices[neighbor]) {
	            	DFS(neighbor,isVisitedVertices); 
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
	    	for (int i = 0; i < isVisitedVertices.length ; i++) {
	    		if(isVisitedVertices[i]) {
	    			count++;
	    		}
	    	}
	    	
	    	return (num_Vertices == count);
	    } 	    	          
	}
	
	private boolean checkIfModelValid(
			List<ObjectVariablePattern> nodes,
			ObjectVariablePattern nodeToDelete) 
	{	
		try 
		{
			// Map names to numbers
			LinkedList<String> vertices = new LinkedList<>();
			
			// === Create a list of vertices  === //
			 String name;
			 for (ObjectVariablePattern node: nodes) {       	
				 if (node == nodeToDelete) {
					 continue;
				 }
				 name = node.getName();
				 if (!vertices.contains(name)) {
					 vertices.add(name);
				 }				 		 
			 }
			
			Graph graph = new Graph(vertices.size());
			
	        // === Create a list of edges  === //
	        addEdgesToGraphMappedFromLinks(graph, nodes, vertices, nodeToDelete);
	        boolean isConnected = graph.isConnected();	
	        return isConnected;
		}
		catch(Exception e) {
			return false;
		}		
	}

	private void addEdgesToGraphMappedFromLinks(
			Graph graph, 
			List<ObjectVariablePattern> nodes, 
			LinkedList<String> vertices,
			ObjectVariablePattern nodeToDelete) 
	{
		EList<LinkVariablePattern> linkVPs;		
		for (ObjectVariablePattern node: nodes) {       	
			if (node == nodeToDelete) {
				continue;
			}
			
			linkVPs = node.getLinkVariablePatterns();
			for (LinkVariablePattern link: linkVPs) {
				if (link.getTarget() != nodeToDelete) {
					int source = vertices.indexOf(node.getName());
	        		int target = vertices.indexOf(link.getTarget().getName());
	        		graph.addEdge(source,target);
	        	  
				}
			}	
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
	
	public List<Integer> getCorrIndexesToDelete(
			List<CorrVariablePattern> correspondenceList, 
			boolean isSourceNode, 
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
		}
		catch(Exception e) {
			return new ArrayList<Integer>();
		}
	}
	
	//========================= Common methods ============================================//
	
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
	
}
