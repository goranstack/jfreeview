package se.bluebrim.view;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

import se.bluebrim.view.model.PropertyChangeNotifier;

/**
 * Implemented by views that can be connected to each other with connection lines
 * 
 * @author GStack
 *
 */
public interface ConnectableView extends PropertyChangeNotifier
{
	public Shape getConnectionShape();
	public Point2D getCenter();
	public AffineTransform getTransformFor(ParentView ancestor);
	public List<ParentView> getParentChain();
}