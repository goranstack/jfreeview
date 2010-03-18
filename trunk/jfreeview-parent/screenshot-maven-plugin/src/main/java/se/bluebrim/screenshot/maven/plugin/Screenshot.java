package se.bluebrim.screenshot.maven.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
}
