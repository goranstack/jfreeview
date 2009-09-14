package se.bluebrim.view.tool;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

import se.bluebrim.view.Handle;
import se.bluebrim.view.Layoutable;
import se.bluebrim.view.impl.ActionModifiers;

/**
 * Abstract superclass to tools that manipulates a view through a handle
 * @author G Stack
 *
 */
public abstract class HandleTool extends SubTool
{
	protected Handle handle;
	private Layoutable layoutable;

	public HandleTool(ToolDispatcher toolDispatcher, Handle handle, Point mousePressedPoint)
	{
		super(toolDispatcher);
		this.mousePressedPoint = mousePressedPoint;
		this.handle = handle;
		layoutable = (Layoutable)handle.getTargetView();
		getViewPanel().setPaintableHints(handle.getPaintabeHints());
		handle.startMoving();
	}
	
	public void mouseDragged(MouseEvent e)
	{
		ActionModifiers actionModifiers = new ActionModifiers(e);
		Point2D newPoint = new Point2D.Float(e.getX(), e.getY());
		if (layoutable.lockVerticalMovement(actionModifiers))
			newPoint.setLocation(newPoint.getX(), mousePressedPoint.y);
		if (layoutable.lockHorizontalMovement(actionModifiers))
			newPoint.setLocation(mousePressedPoint.x, newPoint.getY());
		
		Rectangle2D dirtyRegionBeforeChange = handle.getRepaintRegion();
		handle.moveTo(newPoint);
		repaint(dirtyRegionBeforeChange.createUnion(handle.getRepaintRegion()));
		
		// Autoscroll
		Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
		((JComponent)e.getSource()).scrollRectToVisible(r);
		
		getViewPanel().invalidate();
	}


}
