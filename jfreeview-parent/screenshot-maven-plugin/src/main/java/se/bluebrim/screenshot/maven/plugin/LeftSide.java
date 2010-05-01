package se.bluebrim.screenshot.maven.plugin;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

/**
 * Position the decorator specified offset from the left side of the component
 * 
 * @author Goran Stack
 *
 */
public class LeftSide extends DockLayout {

	
	public LeftSide() {
		super();
	}

	public LeftSide(Point offset) {
		super(offset);
	}

	protected Double getDockingPoint(Rectangle bounds) 
	{
		return new Point2D.Double(bounds.getX(),  bounds.getCenterY());
	}

}
