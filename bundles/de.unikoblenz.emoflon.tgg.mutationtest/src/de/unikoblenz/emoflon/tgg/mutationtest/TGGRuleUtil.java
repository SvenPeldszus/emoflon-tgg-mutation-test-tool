package de.unikoblenz.emoflon.tgg.mutationtest;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.XtextResourceSet;
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

}
