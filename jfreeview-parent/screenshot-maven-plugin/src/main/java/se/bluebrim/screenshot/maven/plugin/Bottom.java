package se.bluebrim.screenshot.maven.plugin;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

/**
 * @author Goran Stack
 *
 */
public class Bottom extends DockLayout {

	
	public Bottom() {
		super();
	}

	public Bottom(Point offset) {
		super(offset);
	}

	protected Double getDockingPoint(Rectangle bounds) 
	{
		return new Point2D.Double(bounds.getCenterX(),  bounds.getY() + bounds.getHeight());
	}

}
