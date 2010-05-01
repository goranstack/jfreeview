package se.bluebrim.screenshot.maven.plugin;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

/**
 * 
 * @author Goran Stack
 *
 */
public class TopLeftCorner extends DockLayout {

	
	public TopLeftCorner() {
		super();
	}

	public TopLeftCorner(Point offset) {
		super(offset);
	}

	protected Double getDockingPoint(Rectangle bounds) 
	{
		return new Point2D.Double(bounds.getX(),  bounds.getY() + bounds.getHeight());
	}

}
