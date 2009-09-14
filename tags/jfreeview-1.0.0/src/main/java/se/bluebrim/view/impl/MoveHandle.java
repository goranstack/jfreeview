package se.bluebrim.view.impl;

import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.geom.Point2D;
import java.util.List;

import se.bluebrim.view.*;
import se.bluebrim.view.tool.Tool;

/**
 * A MoveHandle is used to 
 * move the target view in any direction. MoveHandles differ
 * from resize handles since they are not created until the
 * actual move starts in the SelectionTool. It's seems unnecessary
 * to create handles that has the exact same geometry as the target view.
 * Instead we detect move gestures on views and then create a MoveHandle 
 * on the fly. <br>
 * A MoveHandle uses the sides handles of the target view to create
 * the same kind of feedback as resize does but a MoveHandle produce
 * feedback for several sides at the same time
 * 
 * @author G Stack
 *
 */
public class MoveHandle extends AbstractHandle
{
	private Point2D anchorPoint;
	private ParentView dragSource;
	private ViewDropTarget dropTarget;
	private Tool tool;
	private TransferableView transferable;

	public MoveHandle(BasicView target, Point hitPoint, Tool tool)
	{
		super(target);
		this.dragSource = target.getParent();
		this.tool = tool;
		anchorPoint = target.componentToView(hitPoint);
		if (target instanceof TransferableView)
			transferable = (TransferableView)target;
//		System.out.println("MoveHandle anchorPoint: " + new Point((int)anchorPoint.getX(), (int)anchorPoint.getY()) + " hitPoint: " + hitPoint);
	}
	
	/**
	 * TODO: Delegate this to someone who knows about the ongoing
	 * drag and drop and can set drop accept reject cursor
	 */
	public Cursor getCursor()
	{
		return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
	}

	public void moveTo(Point2D point)
	{
		super.moveTo(point);
//		System.out.println("MoveHandle.moveTo " + point);
		if (transferable != null)
			highLightDropTarget(tool.findDropTargetUnderMouse(new Point((int)point.getX(), (int)point.getY())));		
//		setCursor();
	}
	
	/**
	 * TODO: Introduce min x and min y. Here is also the perfect place for
	 * snapping.
	 */
	public void moveTo(double x, double y)
	{
//		System.out.println("MoveHandle.moveTo x: " + (int)x + " y: " + (int)y);
		double constrainedY = Math.max(getMinY(), y);
		constrainedY = Math.min(getMaxY(), constrainedY);
		double constrainedX = Math.max(getMinX(), x);
		constrainedX = Math.min(getMaxX(), constrainedX);
		targetView.setLocation(new Point2D.Double(constrainedX - anchorPoint.getX(), constrainedY - anchorPoint.getY()));
	}

	/**
	 * Highlights drop target that accepts the drop and set the cursor
	 * @param newDropTarget
	 */
	private void highLightDropTarget(ViewDropTarget newDropTarget)
	{
//		System.out.println("MoveHandle.highLightDropTarget " + newDropTarget);
		if (dropTarget == newDropTarget)
			return;
		else
		{
			if (dropTarget != null)
				tool.highLight(dropTarget, false);
			if (newDropTarget != null)
				if (newDropTarget.acceptDrop(transferable))
				{
					tool.highLight(newDropTarget, true);
					dropTarget = newDropTarget;
					tool.getViewPanel().setCursor(DragSource.DefaultMoveDrop);
				}
				else
					if (newDropTarget != dragSource)
						tool.getViewPanel().setCursor(DragSource.DefaultMoveNoDrop);
					else
						tool.getViewPanel().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			else
			{
				tool.getViewPanel().setCursor(DragSource.DefaultMoveNoDrop);
				dropTarget = null;
			}
		}
	}

	
	/**
	 * MoveHandle is a short life handle that only exist during move
	 */
	public void stopMoving(ActionModifiers modifiers)
	{
		super.stopMoving(modifiers);
		if (dropTarget != null)
			tool.highLight(dropTarget, false);
		((BasicView)targetView).removeHandle(this);
	}

	public List getFeedbackViews()
	{
		return master.getMoveFeedbackViews();
	}

	public ParentView getDragSource()
	{
		return dragSource;
	}		

}
