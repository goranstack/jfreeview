package se.bluebrim.screenshot.maven.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;



/**
 * Mojo that generates a screen shots gallery report.
 * <p> @see <a href="http://teleal.org/weblog/Howto%20write%20a%20Maven%20report%20plugin.html">How to write a Maven report plugin 2010-03-05T12:30:00CET</a>
 * 
 * @author G Stack
 * @goal gallery
 * @phase site
 * @requiresDependencyResolution test
 */
public class GalleryMojo extends AbstractMavenReport
{
	/**
     * Screenshot scale factor. Must be > 0.0 and =< 1.0
     *
     * @parameter default-value="1"
     */
    private float imageScale = 1f;

	/**
	 * The directory containing generated test classes of the project.
	 * 
	 * @parameter expression="${project.build.testOutputDirectory}"
	 */
	protected File testClassesDirectory;
	
	/**
	 * The directory containing generated classes of the project.
	 * 
	 * @parameter expression="${project.build.outputDirectory}"
	 */
	protected File classesDirectory;
	
    /**
     * The classpath elements of the project being tested.
     *
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */

	private ArrayList<String> testClasspathElements;
    /**
     * Directory where reports will go.
     *
     * @parameter expression="${project.reporting.outputDirectory}"
     * @required
     * @readonly
     */
    private String outputDirectory;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * @parameter expression="${rectorProjects}"
     * @readonly
     */
    private ArrayList reactorProjects;

    /**
     * @component
     * @required
     * @readonly
     */
    private Renderer siteRenderer;

	@Override
	protected void executeReport(Locale locale) throws MavenReportException {
		getLog().info("Screenshot gallery executed. The report directory is: " + outputDirectory, null);
		GalleryScreenshotScanner screenshotScanner = new GalleryScreenshotScanner(this, project, testClassesDirectory, classesDirectory, testClasspathElements, outputDirectory);
		screenshotScanner.setProject(project);
		screenshotScanner.setReactorProjects(reactorProjects);
		try {
			screenshotScanner.annotationScan();
		} catch (NoClassDefFoundError e) {
			getLog().error("Unable to find class: " + e.getMessage() + " in the class path of: " + project.getArtifactId());
		}
		screenshotScanner.close();		
	}

	protected MavenProject getProject()
	{
	    return project;
	}

	protected String getOutputDirectory()
	{
	    return outputDirectory;
	}

	protected Renderer getSiteRenderer()
	{
	    return siteRenderer;
	}

	public String getDescription( Locale locale )
	{
	    return getBundle( locale ).getString( "report.gallery.description" );
	}

	public String getName( Locale locale )
	{
	    return getBundle( locale ).getString( "report.gallery.name" );
	}

	public String getOutputName()
	{
	    return "screenshot-gallery";
	}

	private ResourceBundle getBundle( Locale locale )
	{
	    return ResourceBundle.getBundle( "screenshot-gallery", locale, this.getClass().getClassLoader() );
	}

	
}
