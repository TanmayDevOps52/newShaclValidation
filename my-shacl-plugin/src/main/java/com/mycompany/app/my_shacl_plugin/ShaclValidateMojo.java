package com.mycompany.app.my_shacl_plugin;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.validation.ValidationReport;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;
import java.io.StringWriter;
@Mojo(name = "validate", defaultPhase = LifecyclePhase.TEST)
public class ShaclValidateMojo extends AbstractMojo {
	@Parameter(property = "shapeFile", required = true)
	private String shapeFile;

	@Parameter(property = "dataFile", required = true)
	private String dataFile;

	public void execute() throws MojoExecutionException {
		getLog().info("Validating JSON-LD data with SHACL shapes...");
		
		try (InputStream shapesInput = new FileInputStream(shapeFile);
				InputStream dataInput = new FileInputStream(dataFile)) {

			Model shapesModel = JenaUtil.createMemoryModel();
			RDFDataMgr.read(shapesModel, shapesInput, Lang.TTL);

			Model dataModel = JenaUtil.createMemoryModel();
			RDFDataMgr.read(dataModel, dataInput, Lang.JSONLD);

			 Resource reportResource = ValidationUtil.validateModel(dataModel, shapesModel, false);
		        boolean conforms = reportResource.getProperty(SH.conforms).getBoolean();

		        if (!conforms) {
		            StringWriter writer = new StringWriter();
		            reportResource.getModel().write(writer, "TURTLE");
		            String report = writer.toString();
		            throw new MojoExecutionException("Validation failed: \n" + report);
		        }
		} catch (IOException e) {
			throw new MojoExecutionException("Error during SHACL validation", e);
		}
	}
}
