package se.bluebrim.screenshot.maven.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang.ObjectUtils;

/**
 * Used to annotate method in test classes that returns a JComponent suitable
 * for screen shot ripping.
 * 
 * @author G Stack
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Screenshot {
	/**
	 * Specify the class who's Javadoc will include the screen shot. Can be omitted if the screen shot 
	 * method returns that class. 
	 */
	Class targetClass() default ObjectUtils.Null.class;
	
	/**
	 * Used to generate different file names for several screen shots of the same class 
	 * 
	 */
	String scene() default "";
}
