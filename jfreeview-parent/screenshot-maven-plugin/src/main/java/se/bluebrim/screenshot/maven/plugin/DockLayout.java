package se.bluebrim.screenshot.maven.plugin;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Abstract superclass to objects that calculates the position of a decorator 
 * as a specified offset from one of the edges of the component.
 * 
 * @author Goran Stack
 *
 */
public abstract class DockLayout implements DecoratorLayout {

	private Point offset;

	public DockLayout() {
		this(new Point());
	}
		
	public DockLayout(Point offset) {
		super();
		this.offset = offset;
	}


	@Override
	public Point2D getPosition(JComponent component, JComponent rootComponent) {
		Rectangle bounds = SwingUtilities.convertRectangle(component.getParent(), component.getBounds(), rootComponent);
		Point2D anchor = getDockingPoint(bounds);
		return new Point2D.Double(anchor.getX() + offset.x, anchor.getY() + offset.y);
	}

	protected abstract Double getDockingPoint(Rectangle bounds);

}
