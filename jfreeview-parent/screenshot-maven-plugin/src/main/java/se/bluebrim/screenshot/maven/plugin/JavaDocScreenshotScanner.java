package se.bluebrim.screenshot.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;


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


	/**	
	 * Use the Javadoc convention to name the screen shot file. See:
	 * <a href="http://java.sun.com/j2se/javadoc/writingdoccomments/#images">Including images in Javadoc</a>
	 */
	@Override
	protected void handleFoundMethod(Class candidateClass, Method method) {
		JComponent screenShotComponent = callScreenshotMethod(candidateClass, method);
		if (screenShotComponent != null)
		{
			Class javadocClass = getJavadocClass(method, screenShotComponent);
			File docFilesDirectory = new File(sourceDirectory, org.springframework.util.ClassUtils.classPackageAsResourcePath(javadocClass) + "/doc-files");
			docFilesDirectory.mkdirs();
			File file = new File(docFilesDirectory.getPath(), ClassUtils.getShortClassName(javadocClass) + ".png");
			File tempFile = createTempFile(ClassUtils.getShortClassName(javadocClass), ".png", docFilesDirectory);
			takeScreenShot(screenShotComponent, tempFile);
			try
			{
				if (!FileUtils.contentEquals(file, tempFile))
				{
					FileUtils.copyFile(tempFile, file);
					getLog().info("Saved screenshot to: " + file.getPath());
				}
			} catch (IOException e)
			{
				throw new RuntimeException("Unable to save screenshot: " + file.getPath(), e);
			} finally
			{
				tempFile.delete();
			}
		}
	}
	
	private File createTempFile(String prefix, String suffix,  File directory)
	{
		try
		{
			return File.createTempFile(prefix, suffix, directory);
		} catch (IOException e)
		{
			throw new RuntimeException("Unable to create temp file for storing screenshot: " + directory.getPath() + "/" + prefix + "." + suffix, e);
		}
	}
	
	@Override
	protected Log getLog() 
	{
		return mojo.getLog();
	}

}
