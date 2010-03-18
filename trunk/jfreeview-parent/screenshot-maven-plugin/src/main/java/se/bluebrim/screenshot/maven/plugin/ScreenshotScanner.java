package se.bluebrim.screenshot.maven.plugin;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import org.apache.maven.plugin.AbstractMojo;
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

	protected AbstractMojo mojo;
	private File testClassesDirectory;
	private File classesDirectory;
	private List<String> testClasspathElements;	
	private ClassLoader classLoader;
	private float scaleFactor = 1f;
	
		
	public ScreenshotScanner(AbstractMojo mojo, File testClassesDirectory, File classesDirectory, List<String> testClasspathElements) 
	{
		super();
		this.mojo = mojo;
		this.testClassesDirectory = testClassesDirectory;
		this.classesDirectory = classesDirectory;
		this.testClasspathElements = testClasspathElements;
		classLoader = createClassLoader();
	}

	public void setScaleFactor(float scaleFactor) 
	{
		this.scaleFactor = scaleFactor;
	}


	protected abstract void handleFoundMethod(Class candidateClass, Method method);

	private ClassLoader createClassLoader()
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
		
		return new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());
	}

	
	/**
	 * Make sure that the test class and the annotation class are loaded with the same class loader otherwise
	 * the Method.isAnnotationPresent won't work.
	 */
	public void annotationScan()
	{
		Class screenshotAnnotation = loadClass(Screenshot.class.getName());
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.setResourceLoader(new DefaultResourceLoader(createAnnotationScanClassLoader()));
		scanner.addIncludeFilter(new AnnotationTypeFilter(Screenshot.class)); 			 
		for (BeanDefinition bd : scanner.findCandidateComponents(""))
		{
			mojo.getLog().debug("Found screenshot annotaded class: " + bd.getBeanClassName());			
			Class candidateClass = loadClass(bd.getBeanClassName());
			
			for (Method method : candidateClass.getMethods()) 
			{
				mojo.getLog().debug("Checking method: \"" + method.getName() + "\" for screenshot annotation");
				if (method.isAnnotationPresent(screenshotAnnotation)) 
			    {
					handleFoundMethod(candidateClass, method);
			    } 
			}						
		}			  
	}
	
	/**
	 * Use a named base comparison since we can't be sure that the annotation and the method
	 * are loaded with the same class loader.
	 */
	private boolean isAnnotationPresent(Method method, String annotationName)
	{
		Annotation[] annotations = method.getAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation.getClass().getName().equals(annotationName))
				return true;
		}
		return false;
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
			return new URLClassLoader(new URL[]{testClassesDirectory.toURI().toURL()}, ClassLoader.getSystemClassLoader());
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
			throw new RuntimeException(e);
		}		
	}

	protected void takeScreenShot(JComponent component, File file)
	{
		writeScreenshot(ripSwingComponent(component), file);
	}
	
	
	protected BufferedImage ripSwingComponent(JComponent component)
	{
		component.setSize(component.getPreferredSize());
		propagateDoLayout(component);
		BufferedImage image = new BufferedImage(Math.round(component.getWidth() * scaleFactor), Math.round(component.getHeight() * scaleFactor), BufferedImage.TYPE_INT_RGB);
		Graphics2D g =  image.createGraphics();
		g.scale(scaleFactor, scaleFactor);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		component.setDoubleBuffered(false);
		component.print(g);
		g.dispose();
		return image;
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
			ImageIO.write(screenshot, "png", file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to write screen shot to: " + file.getPath());
		}		
	}

	
}
