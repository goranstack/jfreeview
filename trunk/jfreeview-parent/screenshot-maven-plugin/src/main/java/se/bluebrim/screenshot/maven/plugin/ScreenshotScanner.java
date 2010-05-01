package se.bluebrim.screenshot.maven.plugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.jdesktop.swingx.graphics.GraphicsUtilities;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Abstract super class to objects that scans test classes for methods annotated with Screenshot annotation.
 * Since our two mojo's has different superclass we can't use inheritance to provide the features in this class
 * to our mojo's.
 * <p>
 * <a href="http://www.mail-archive.com/user@mojo.codehaus.org/msg01547.html">Adding project dependencies to a plugin</a>
 * 
 * @author G Stack
 *
 */
public abstract class ScreenshotScanner {

	private static final String FORMAT_PNG = "png";
	protected MavenProject project;
	protected List reactorProjects;
	protected AbstractMojo mojo;
	private File testClassesDirectory;
	private File classesDirectory;
	private List<String> testClasspathElements;	
	private float scaleFactor = 1f;
	private ClassLoader classLoader;
//	private Class<Screenshot> screenshotAnnotationClass;
	
		
	public ScreenshotScanner(AbstractMojo mojo, File testClassesDirectory, File classesDirectory, List<String> testClasspathElements) 
	{
		super();
		this.mojo = mojo;
		this.testClassesDirectory = testClassesDirectory;
		this.classesDirectory = classesDirectory;
		this.testClasspathElements = testClasspathElements;
		classLoader = createClassLoader();
//		screenshotAnnotationClass = loadAnnotationClass(Screenshot.class.getName());
	}
	
	public void setProject(MavenProject project) 
	{
		this.project = project;
	}

	public void setReactorProjects(List reactorProjects) 
	{
		this.reactorProjects = reactorProjects;
	}

	public void setScaleFactor(float scaleFactor) 
	{
		this.scaleFactor = scaleFactor;
	}

	protected abstract void handleFoundMethod(Class candidateClass, Method method);
	
	protected File createScreenshotFile(JComponent screenShotComponent, Class screenshotClass, File dir, Method method) 
	{
		String screenshotName = screenshotClass.getSimpleName() + getSceneName(method);
		File file = new File(dir.getPath(), screenshotName + "." + FORMAT_PNG);
		File tempFile = createTempFile(screenshotName, "." + FORMAT_PNG, dir);
		takeScreenShot(screenShotComponent, tempFile);
		overwriteIfChanged(file, tempFile);
		return file;
	}

	
	private void overwriteIfChanged(File originalFile, File tempFile) {
		try
		{
			if (!FileUtils.contentEquals(originalFile, tempFile))
			{
				FileUtils.copyFile(tempFile, originalFile);
				getLog().info("Saved screenshot to: " + originalFile.getPath());
			}
		} catch (IOException e)
		{
			throw new RuntimeException("Unable to save screenshot: " + originalFile.getPath(), e);
		} finally
		{
			tempFile.delete();
		}
	}
	
	private File createTempFile(String prefix, String suffix,  File directory)
	{
		try
		{
			File tempFile = File.createTempFile(prefix, suffix, directory);
			tempFile.deleteOnExit();
			return tempFile;
		} catch (IOException e)
		{
			throw new RuntimeException("Unable to create temp file for storing screenshot: " + directory.getPath() + "/" + prefix + "." + suffix, e);
		}
	}

	/**
	 * 
	 * @return The class that should be associated with the screenshot. There are cases where the screenShotComponent
	 * is a generic panel class containing the specific screenshot class.
	 */
	protected Class getTargetClass(Method method, JComponent screenShotComponent)
	{
		Screenshot annotation = method.getAnnotation(Screenshot.class);
		Class targetClass = annotation.targetClass();
		getLog().debug("Screenshot annotation targetClass: " + targetClass);
		return ObjectUtils.Null.class.equals(targetClass)  ? screenShotComponent.getClass()  : targetClass;		
	}
	

	private String getSceneName(Method method)
	{
		Screenshot annotation = method.getAnnotation(Screenshot.class);
		String scene = annotation.scene();
		getLog().debug("Screenshot annotation scene: " + scene);
		return (StringUtils.isEmpty(scene))  ? ""  : "-" + scene;		
	}

	
	protected abstract Log getLog(); 

	
	private List<URL> collectURLs()
	{
		List<URL> urls = new ArrayList<URL>();
		try
		{
			urls.add(testClassesDirectory.toURI().toURL());
			urls.add(classesDirectory.toURI().toURL());
			
			for (String classpathElement : testClasspathElements)
			{
				File pathelem = new File(classpathElement);
				// we need to use 3 slashes to prevent Windows from interpreting
				// 'file://D:/path' as server 'D'
				// we also have to add a trailing slash after directory paths
				URL url = new URL("file:///" + pathelem.getPath() + (pathelem.isDirectory() ? "/" : ""));
				urls.add(url);
				
			}
		} catch (MalformedURLException e)
		{
			throw new RuntimeException(e);
		}
		return urls;
	}
	

	
	/**
	 * Make sure that the test class and the annotation class are loaded with
	 * the same class loader otherwise the Method.isAnnotationPresent won't
	 * work.
	 */
	public void annotationScan()
	{
//		if (screenshotAnnotationClass == null) // Skip modules missing
//												// dependency to Screenshot
//												// annotation
//			return;
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.setResourceLoader(new DefaultResourceLoader(createAnnotationScanClassLoader()));
		scanner.addIncludeFilter(new AnnotationTypeFilter(Screenshot.class));

		ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(classLoader);
			processCandidateClasses(scanner);
		} catch (Exception e)
		{
			getLog().error(e);
		} finally
		{
			Thread.currentThread().setContextClassLoader(oldContextClassLoader);
		}

	}

	/**
	 * Process classes with one ore more screenshot annotated method
	 */
	private void processCandidateClasses(ClassPathScanningCandidateComponentProvider scanner)
	{
		for (BeanDefinition bd : scanner.findCandidateComponents(""))
		{
			getLog().debug("Found screenshot annotaded class: " + bd.getBeanClassName());
			Class candidateClass = loadClass(bd.getBeanClassName());

			for (Method method : candidateClass.getMethods())
			{
				getLog().debug("Checking method: \"" + method.getName() + "\" for screenshot annotation");
				if (method.isAnnotationPresent(Screenshot.class))
				{
					handleFoundMethod(candidateClass, method);
				}
			}
		}
	}

	private Class loadClass(String testClassName)
	{
		try
		{
			return classLoader.loadClass(testClassName);
		} catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a class loader that can be used in the resource loader injected in to the ClassPathScanningCandidateComponentProvider
	 */
	private ClassLoader createAnnotationScanClassLoader()
	{
		try
		{
			return new URLClassLoader(new URL[]{testClassesDirectory.toURI().toURL()});
		} catch (MalformedURLException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected JComponent callScreenshotMethod(Class targetClass, Method screenshotMethod)
	{
		try
		{
			Object instance = targetClass.newInstance();
			return (JComponent) screenshotMethod.invoke(instance);
		} catch (InstantiationException e)
		{
			throw new RuntimeException(e);
		} catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		} catch (SecurityException e)
		{
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e)
		{
			throw new RuntimeException(e);
		} catch (InvocationTargetException e)
		{
			handleExceptionInCalledMethod(targetClass, screenshotMethod, e);
			return null;
		}catch (Exception e)
		{
			handleExceptionInCalledMethod(targetClass, screenshotMethod, e);
			return null;
		}			
	}

	private void handleExceptionInCalledMethod(Class targetClass, Method screenshotMethod, Exception e)
	{
		getLog().info("Unable to create screenshot by calling: " + targetClass.getName() + "." + screenshotMethod.getName(), e);
	}

	protected void takeScreenShot(JComponent component, File file)
	{
		writeScreenshot(ripSwingComponent(component), file);
	}
	
	
	/**
	 * Scaling the image by setting the scale factor of the Graphics2D gives a pore result.
	 * Use the scaling method described in "Filthy Rich Clients" implemented in Swingx GraphicsUtilities.
	 */
	protected BufferedImage ripSwingComponent(JComponent component)
	{
		component.setSize(component.getPreferredSize());
		propagateDoLayout(component);
		BufferedImage image = new BufferedImage(component.getWidth(),component.getHeight(), BufferedImage.TYPE_INT_ARGB);			
		Graphics2D g =  image.createGraphics();
		if (component.isOpaque())
		{
			g.setColor(new Color(0xE5EDF5));
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		component.setDoubleBuffered(false);
		component.print(g);
		decorateScreenshot(component, g);
		g.dispose();

		return (scaleFactor < 1)  ? GraphicsUtilities.createThumbnailFast(image, (int)(image.getWidth() * scaleFactor), (int)(image.getHeight() * scaleFactor)) : image;

	}
	
	/**
	 * The method is public static to enable use from test classes to show decorated panels in 
	 * a JFrame.
	 */
	public static void decorateScreenshot(final JComponent rootComponent, final Graphics2D g2d)
	{
		DecoratorUtils.eachComponent(rootComponent, new DecoratorUtils.ComponentVisitor(){

			@Override
			public void visit(JComponent component) {
				Object clientProperty = component.getClientProperty(ScreenshotDecorator.CLIENT_PROPERTY_KEY);
				if (clientProperty instanceof ScreenshotDecorator)
					((ScreenshotDecorator)clientProperty).paint(g2d, component, rootComponent);				
			}});
	}
	
	/**
	 * Found at: <a href="http://forums.sun.com/thread.jspa?messageID=10852895#10852895"> Turning a component into a BufferedImage</a>
	 */
	private static void propagateDoLayout(Component c) {
	    synchronized (c.getTreeLock()) {
	        c.doLayout();
	 
	        if (c instanceof Container) {
	            for (Component subComp : ((Container) c).getComponents()) {
	                propagateDoLayout(subComp);
	            }
	        }
	    }
	}


	private void writeScreenshot(BufferedImage screenshot, File file)
	{
		try {
			ImageIO.write(screenshot, FORMAT_PNG, file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to write screen shot to: " + file.getPath());
		}		
	}

	private ClassLoader createClassLoader()
	{
		List<URL> urls = collectURLs();
		return new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
	}

	private Class<Screenshot> loadAnnotationClass(String className)
	{
		try
		{
			return (Class<Screenshot>) classLoader.loadClass(className);
		} catch (ClassNotFoundException e)
		{
			getLog().debug("No screenshot annotation class found in: " + project.getArtifactId());
			return null;
		}
	}
	
}
