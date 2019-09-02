/**
 * 
 */
package de.unikoblenz.emoflon.tgg.mutationtest.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.unikoblenz.emoflon.tgg.mutationtest.TGGRuleUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.emoflon.ibex.tgg.ide.admin.IbexTGGNature;
import org.gravity.eclipse.io.ExtensionFileVisitor;
import org.gravity.eclipse.io.GitCloneException;
import org.gravity.eclipse.io.GitTools;
import org.gravity.eclipse.util.EclipseProjectUtil;
import org.junit.Before;
import org.junit.Test;
import org.moflon.tgg.mosl.TGGStandaloneSetup;
import org.moflon.tgg.mosl.tgg.Rule;
import org.moflon.tgg.mosl.tgg.Schema;
import org.moflon.tgg.mosl.tgg.TggFactory;
import org.moflon.tgg.mosl.tgg.TripleGraphGrammarFile;

/**
 * Tests for the interaction with the TGG specifications
 * 
 * @author speldszus
 *
 */
public class TGGRuleUtilTest {

	/**
	 * The logger of this class
	 */
	private static final Logger LOGGER = Logger.getLogger(TGGRuleUtilTest.class);

	/**
	 * A test for loading all tgg rules from the eMoflon handbook example
	 * "socialNetworkSynchronisation"
	 * 
	 * @throws IOException
	 * @throws GitCloneException
	 * @throws CoreException
	 */
	@Test
	public void loadTGGRuleTest() throws IOException, GitCloneException, CoreException {
		IProject project = checkoutAndGetTGGProject("https://github.com/eMoflon/emoflon-ibex-examples.git",
				"socialNetworkSynchronisation/version1/");
		Path projectPath = project.getLocation().toFile().toPath();
		TGGRuleUtil util = new TGGRuleUtil(project);
		ExtensionFileVisitor visitor = new ExtensionFileVisitor("tgg");
		project.accept(visitor);
		for (Path ruleFile : visitor.getFiles()) {
			if (ruleFile.isAbsolute()) {
				ruleFile = projectPath.relativize(ruleFile);
			}
			if (IbexTGGNature.SCHEMA_FILE.equals(ruleFile.toString())) {
				continue;
			}
			TripleGraphGrammarFile rule = util.loadRule(project.getFile(ruleFile.toString()));
			assertNotNull(rule);
		}
	}

	/**
	 * Tests the serialization of a dummy rule
	 * 
	 * @throws IOException If the rule cannot be serialized
	 */
	@Test
	public void serializeTGGRuleTest() throws IOException {
		File tmpFile = Files.createTempFile("xtext", ".tgg").toFile();
		String schemaName = "testSchema";
		String ruleName = "testRule";
		TripleGraphGrammarFile ruleFile = createEmptyRuleAndSchema(schemaName, ruleName);

		ruleFile.eResource().save(new FileOutputStream(tmpFile), Collections.emptyMap());

		assertTrue(tmpFile.exists());

		List<String> lines = Files.readAllLines(tmpFile.toPath());
		assertNotNull(lines);
		assertFalse(lines.isEmpty());
		assertEquals("#rule " + ruleName + " #with " + schemaName,
				lines.parallelStream().collect(Collectors.joining()).trim());
	}

	/**
	 * Initializes xtext
	 */
	@Before
	public void initXtext() {
		new TGGStandaloneSetup().createInjectorAndDoEMFRegistration();
	}

	/**
	 * Creates an new empty rule and with the given names
	 * 
	 * @param schemaName The name of the schema
	 * @param ruleName   The name of the rule
	 * 
	 * @return the created rule
	 */
	private TripleGraphGrammarFile createEmptyRuleAndSchema(String schemaName, String ruleName) {
		XtextResourceSet resourceSet = new XtextResourceSet();
		Resource ruleResource = resourceSet.createResource(URI.createFileURI(ruleName + ".tgg"));
		Resource schemaResource = resourceSet.createResource(URI.createFileURI("Schema.tgg"));
		TggFactory factory = TggFactory.eINSTANCE;

		TripleGraphGrammarFile ruleFile = factory.createTripleGraphGrammarFile();

		Schema schema = factory.createSchema();
		schema.setName(schemaName);
		schemaResource.getContents().add(schema);

		Rule rule = factory.createRule();
		rule.setName(ruleName);
		rule.setSchema(schema);
		ruleFile.getRules().add(rule);

		ruleResource.getContents().add(ruleFile);
		return ruleFile;
	}

	/**
	 * Clones the git repository, imports all projects into the workspace and
	 * returns the tgg project
	 * 
	 * @param url    The url of the git repository
	 * @param folder A folder within the repository containing the projects to
	 *               import
	 * @return The TGG project
	 * @throws IOException
	 * @throws CoreException
	 * @throws GitCloneException
	 */
	private IProject checkoutAndGetTGGProject(String url, String folder)
			throws IOException, CoreException, GitCloneException {
		File tmp = Files.createTempDirectory("eMoflonExamples").toFile();
		try (GitTools git = new GitTools(url, tmp)) {
			List<IProject> projects = EclipseProjectUtil.importProjects(new File(tmp.listFiles()[0], folder),
					new NullProgressMonitor());
			return getAnyTGGProject(projects);
		}
	}

	/**
	 * Search if there is any TGG project contained in the list of projects and
	 * return it
	 * 
	 * @param projects A list of eclipse project
	 * @return A eMoflon TGG project if present in the list
	 * @throws IllegalStateException if no TGG project is contained in the list
	 */
	private IProject getAnyTGGProject(List<IProject> projects) throws IllegalStateException {
		Optional<IProject> result = projects.parallelStream().filter(p -> {
			try {
				return p.getNature(IbexTGGNature.IBEX_TGG_NATURE_ID) != null;
			} catch (CoreException e) {
				LOGGER.error(e.getMessage(), e);
				return false;
			}
		}).findAny();
		if (!result.isPresent()) {
			throw new IllegalStateException("Couldn't load the TGG project!");
		}
		return result.get();
	}
}
