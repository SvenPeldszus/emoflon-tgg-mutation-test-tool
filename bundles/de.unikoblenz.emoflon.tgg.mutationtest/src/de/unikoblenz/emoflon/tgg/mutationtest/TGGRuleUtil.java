package de.unikoblenz.emoflon.tgg.mutationtest;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.moflon.tgg.mosl.tgg.Rule;
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
	private Resource schemaResource;

	/**
	 * Creates a new rule util for the given TGG project
	 * 
	 * @param project The Eclipse project
	 * @throws IOException   If the TGG Schema couldn't be loaded
	 * @throws CoreException If the TGG Schema couldn't be loaded
	 */
	public TGGRuleUtil(IProject project) throws IOException, CoreException {
		resourceSet = new XtextResourceSet();
		IFile schemaFile = project.getFile(SCHEMA_FILE);
		schemaResource = resourceSet
				.createResource(URI.createPlatformResourceURI(schemaFile.getFullPath().toString(), true));
		schemaResource.load(schemaFile.getContents(), Collections.emptyMap());
		EcoreUtil.resolveAll(resourceSet);
	}
	
	public void unloadResources() {
		resourceSet.getResources().forEach(Resource::unload);
	}

	/**
	 * Loads the TGG rule from the given files
	 * 
	 * @param file A file
	 * @return The loaded rule
	 * @throws IOException   If reading the serialized rule failed
	 * @throws CoreException if this method fails. The status code associated with
	 *                       exception reflects the cause of the failure.
	 */
	public List<Rule> loadRules(Collection<IFile> files) throws IOException, CoreException {
		List<Rule> rules = new LinkedList<>();
		for (IFile file : files) {
			Resource ruleResource = resourceSet
					.getResource(URI.createPlatformResourceURI(file.getFullPath().toString(), true), true);
			rules.addAll(((TripleGraphGrammarFile) ruleResource.getContents().get(0)).getRules());
		}
		EcoreUtil2.resolveLazyCrossReferences(schemaResource, () -> false);
		resourceSet.getResources().forEach(r -> EcoreUtil2.resolveLazyCrossReferences(r, () -> false));
		EcoreUtil.resolveAll(resourceSet);
		return rules;
	}

	/**
	 * Loads the TGG rule from the given file
	 * 
	 * @param file A file
	 * @return The loaded rule
	 * @throws IOException   If reading the serialized rule failed
	 * @throws CoreException if this method fails. The status code associated with
	 *                       exception reflects the cause of the failure.
	 */
	public List<Rule> loadRules(IFile file) throws IOException, CoreException {
		Resource ruleResource = resourceSet
				.getResource(URI.createPlatformResourceURI(file.getFullPath().toString(), true), true);
		EcoreUtil2.resolveLazyCrossReferences(schemaResource, () -> false);
		resourceSet.getResources().forEach(r -> EcoreUtil2.resolveLazyCrossReferences(r, () -> false));
		EcoreUtil.resolveAll(resourceSet);
		return ((TripleGraphGrammarFile) ruleResource.getContents().get(0)).getRules();
	}
}
