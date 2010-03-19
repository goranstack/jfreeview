package se.bluebrim.screenshot.maven.plugin;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;



/**
 * Mojo that generates screen shots.
 
 * 
 * @author G Stack
 * @goal gallery
 * @phase site
 * @requiresDependencyResolution test
 * 
 */
public class GalleryMojo extends AbstractMavenReport
{
	/**
     * Screenshot scale factor. Must be > 0.0 and =< 1.0
     *
     * @parameter
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

	private List<String> testClasspathElements;
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
     * @component
     * @required
     * @readonly
     */
    private Renderer siteRenderer;

	@Override
	protected void executeReport(Locale locale) throws MavenReportException {
		getLog().info("Screenshot gallery executed. The report directory is: " + outputDirectory, null);
		GalleryScreenshotScanner screenshotScanner = new GalleryScreenshotScanner(this, testClassesDirectory, classesDirectory, testClasspathElements, outputDirectory);
		if (imageScale > 0 && imageScale <= 1)
			screenshotScanner.setScaleFactor(imageScale);
		else
			getLog().error("The \"imageScale\" parameter must be > 0 and <= 1");
		screenshotScanner.annotationScan();
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
