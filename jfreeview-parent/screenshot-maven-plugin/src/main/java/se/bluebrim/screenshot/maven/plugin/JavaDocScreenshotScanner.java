package se.bluebrim.screenshot.maven.plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import javax.swing.JComponent;

import org.apache.maven.plugin.AbstractMojo;
import org.springframework.util.ClassUtils;

/**
 * Scans test classes for method annotated with the Screenshot annotation. Calls found methods and save a png-file with the same name as
 * return JComponent subclass. The png is saved in a doc-files directory alongside the source file for the JComponent subclass.
 * 
 * @author G Stack
 *
 */
public class JavaDocScreenshotScanner extends ScreenshotScanner 
{
	private File sourceDirectory;

	public JavaDocScreenshotScanner(AbstractMojo mojo, File testClassesDirectory, File classesDirectory, List<String> testClasspathElements, File sourceDirectory) 
	{
		super(mojo, testClassesDirectory, classesDirectory, testClasspathElements);
		this.sourceDirectory = sourceDirectory;
	}

	@Override
	/**	
	 * Use the Javadoc convention to name the screen shot file. See:
	 * <a href="http://java.sun.com/j2se/javadoc/writingdoccomments/#images">Including images in Javadoc</a>
	 */
	protected void handleFoundMethod(Class candidateClass, Method method) {
		JComponent screenShotComponent = callScreenshotMethod(candidateClass, method);
		File docFilesDirectory = new File(sourceDirectory, ClassUtils.classPackageAsResourcePath(candidateClass) + "/doc-files");
		docFilesDirectory.mkdirs();
		File file = new File(docFilesDirectory, ClassUtils.getShortName(screenShotComponent.getClass()) + "-1.png");
		takeScreenShot(screenShotComponent, file);
		mojo.getLog().info("Saved screenshot to: " + file.getPath());
	}

}
