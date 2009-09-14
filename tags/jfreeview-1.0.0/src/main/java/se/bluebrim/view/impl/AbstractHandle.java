package se.bluebrim.view.impl;

import java.awt.*;
import java.awt.geom.*;

import se.bluebrim.view.*;
import se.bluebrim.view.paint.*;


/**
 * Abstract superclass to handles
 * 
 * @author Görans Stäck
 * 
 */
public abstract class AbstractHandle extends SlaveView implements Handle
{
	protected Layoutable targetView;

	public AbstractHandle(AbstractView master) 
	{
		super(master);
		targetView = (Layoutable)master;
	}

	/**
	 * 
	 * @return The view that should be repainted after each move. If more than one view is affected
	 * you can override this method and return the parent view to affected views. That's for example how
	 * Phase views is handled.
	 */
	public View getTargetView()
	{
		return master;
	}

	/**
	 * Give the view a opportunity to take some action when the handle manipulation starts
	 */
	public void startMoving() 
	{
		master.startHandleManipulation(this);
	}
	
	public void moveTo(Point2D point)
	{
		Point2D dest = new Point2D.Float();
		try
		{
			targetView.getTransform().inverseTransform(point, dest);
		}
		catch (NoninvertibleTransformException e)
		{
			System.out.println("ResizeHandle " + e);
			return;
		}
		moveTo(dest.getX(), dest.getY());		
	}

	public abstract void moveTo(double x, double y);

	/**
	 * Give the view a opportunity to take some action when the handle manipulation stops
	 */
	public void stopMoving(ActionModifiers modifiers) 
	{
		master.stopHandleManipulation(modifiers);
	}
	
	public Rectangle2D getRepaintRegion()
	{
		return master.getDirtyRegion().getBounds2D();
	}
	
	public PaintableHints getPaintabeHints()
	{
		return null;
	}
	
	/**
	 * Handles are normaly not visible but the feedback is always painted
	 * because that method check if the handle is active
	 */
	public void paint(Paintable g)
	{
		if (ResizeHandle.PAINT_HANDLES.value)
		{
			g.setStroke(new BasicStroke(1));
			g.setColor(Color.GREEN);
			g.setStroke(new BasicStroke());
			g.draw(getBounds());
		}
	}

	public void paintFeedback(Paintable g)
	{
		
	}
	
	protected void deactivateDragLayer()
	{
		targetView.removeFromLayer(ViewLayer.DRAG_LAYER);
	}
	
	protected void activateDragLayer()
	{
		targetView.addToLayer(ViewLayer.DRAG_LAYER);
	}
	
	public void putOnTop()
	{
		master.putOnTop();
	}

	public float getMaxWidth()
	{
		return targetView.getMaxWidth();
	}

	public float getMinWidth()
	{
		return targetView.getMinWidth();
	}
	
	public float getMaxHeight()
	{
		return targetView.getMaxHeight();
	}

	public float getMinHeight()
	{
		return targetView.getMinHeight();
	}
	
	public float getMaxX()
	{
		return targetView.getMaxX();
	}

	public float getMinX()
	{
		return targetView.getMinX();
	}
	
	public float getMaxY()
	{
		return targetView.getMaxY();
	}

	public float getMinY()
	{
		return targetView.getMinY();
	}

	
}
