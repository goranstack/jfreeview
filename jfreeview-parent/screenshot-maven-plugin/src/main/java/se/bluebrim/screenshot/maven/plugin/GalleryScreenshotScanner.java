package se.bluebrim.screenshot.maven.plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.lang.ClassUtils;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.reporting.AbstractMavenReport;

/**
 * Scans test classes for method annotated with the Screenshot annotation. Calls found methods and save a png-file with the same name as
 * return JComponent subclass. The png is saved in a doc-files directory alongside the source file for the JComponent subclass.
 * 
 * @author G Stack
 *
 */
public class GalleryScreenshotScanner extends ScreenshotScanner 
{
	private AbstractMavenReport reportMojo;
	private String outputDirectory;
	private Sink sink;

	public GalleryScreenshotScanner(AbstractMavenReport reportMojo, File testClassesDirectory, File classesDirectory, List<String> testClasspathElements, String outputDirectory) 
	{
		super(reportMojo, testClassesDirectory, classesDirectory, testClasspathElements);
		this.outputDirectory = outputDirectory;
		this.reportMojo = reportMojo;
		sink = reportMojo.getSink();
	}

	/**	
	 * See:
	 * <a href="http://docs.codehaus.org/display/MAVENUSER/Write+your+own+report+plugin">Write your own report plugin</a>
	 */
	protected void handleFoundMethod(Class candidateClass, Method method) {
		JComponent screenShotComponent = callScreenshotMethod(candidateClass, method);
		String imageFileName = ClassUtils.getShortClassName(screenShotComponent.getClass()) + "-1.png";
		File file = new File(outputDirectory, imageFileName);
		takeScreenShot(screenShotComponent, file);
		mojo.getLog().info("Saved screenshot to: " + file.getPath());
		sink.figure();
		sink.figureGraphics(imageFileName);
		sink.figure_();
	}
	
	public void close()
	{
		sink.flush();
	    sink.close();		
	}

}
