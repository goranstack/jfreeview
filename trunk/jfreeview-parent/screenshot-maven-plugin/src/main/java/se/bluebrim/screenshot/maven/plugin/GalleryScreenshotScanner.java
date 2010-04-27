package se.bluebrim.screenshot.maven.plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.lang.ClassUtils;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
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
	private File outputDirectory;
	private Sink sink;
	private MavenProject project;

	public GalleryScreenshotScanner(AbstractMavenReport reportMojo, MavenProject project, File testClassesDirectory, File classesDirectory, List<String> testClasspathElements, String outputDirectory) 
	{
		super(reportMojo, testClassesDirectory, classesDirectory, testClasspathElements);
		this.reportMojo = reportMojo;
		this.project = project;
		this.outputDirectory = new File(outputDirectory);
		this.outputDirectory.mkdirs();
		sink = reportMojo.getSink();
		
	}

	/**	
	 * See:
	 * <a href="http://docs.codehaus.org/display/MAVENUSER/Write+your+own+report+plugin">Write your own report plugin</a>
	 * The target class for the javadoc image file can be specified as annotation parameter. If Object.class is specified
	 * the class returned from the screen shot method is used as target class.
	 */
	protected void handleFoundMethod(Class candidateClass, Method method) 
	{
		JComponent screenShotComponent = callScreenshotMethod(candidateClass, method);
		if (screenShotComponent != null)
		{
			Class screenshotClass = getTargetClass(method, screenShotComponent);
			File file = createScreenshotFile(screenShotComponent, screenshotClass, outputDirectory, method);
			sink.paragraph();
			sink.figure();
			sink.figureGraphics(file.getName());
			sink.figure_();
			sink.lineBreak();
			sink.link(getScmPath() + getSourceDirectory() + "/" + org.springframework.util.ClassUtils.convertClassNameToResourcePath(screenshotClass.getName()) + ".java");
			sink.text(screenshotClass.getName());
			sink.link_();
			sink.paragraph_();
		}
	}
	
	private String getScmPath()
	{
		return project.getScm().getUrl();
	}

	/**
	 * TODO: Retrieve from Maven project
	 *
	 */
	private String getSourceDirectory()
	{
		return "/src/main/java";
	}
			
	protected Log getLog() 
	{
		return mojo.getLog();
	}
		
	public void close()
	{
		sink.flush();
	    sink.close();		
	}

}
