package se.bluebrim.screenshot.maven.plugin;

import java.awt.geom.Point2D;

import javax.swing.JComponent;

/**
 * Implemented by objects that calculates the position of a decorator relative the root component. 
 * 
 * @author Goran Stack
 *
 */
public interface DecoratorLayout {

	public Point2D getPosition(JComponent component, JComponent rootComponent);
}
