package se.bluebrim.view.tool;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import se.bluebrim.view.*;
import se.bluebrim.view.impl.*;
import se.bluebrim.view.swing.ViewPanel;
/**
 * MoveTool is a sub tool that is created by SelectionTool when the user initialize
 * a view move by starting a drag on a transferable view. The MoveTool will take over as current
 * tool as long the drag is going on. When the drag is interrupted the MoveTool return the control
 * to the SelectionTool. The view that is moved by the handle is called the target view<br>
 * A MoveTool keeps track of drag source and drop target to determine if there is a drag and drop
 * action going on. The drag source is the original parent of the target view and the drop target is
 * the top most view at the mouse position that return true on the isDropTarget method
 * 
 * @author G Stack
 *
 */

public class MoveTool extends HandleTool implements GlobalTool
{
	private ParentView dragSource;
	private TransferableView targetView;
	private List transferableViews;
	private Point2D originalLocation;
	
	public MoveTool(ToolDispatcher toolDispatcher, MoveHandle handle, Point mousePressedPoint)
	{
		super(toolDispatcher, handle, mousePressedPoint);
		dragSource = handle.getDragSource();
		targetView = (TransferableView)handle.getTargetView();
		transferableViews = new ArrayList();
		transferableViews.add(targetView);
		originalLocation = targetView.getLocation();
		handle.moveTo(mousePressedPoint);
		getMouseEventDispatcher().setGlobalTool(this);
	}
	
	public void mouseReleased(MouseEvent e)
	{
		super.mouseReleased(e);
		getMouseEventDispatcher().setGlobalTool(null);
		getViewPanel().setPaintableHints(null);
		moveAction(e);
		if (!getTransactionManager().isDataChangeNotified())
			getViewPanel().invalidate();
		getViewPanel().repaint();		// Paint the unconfirmed changes state
	}
	
	/**
	 * Let the view update its model and excute the current transaction to make
	 * the changes permanent.
	 */
	private void moveAction(MouseEvent e)
	{
		try
		{
			handle.stopMoving(new ActionModifiers(e.getModifiers()));
		}
		catch (RuntimeException e1)
		{
			System.out.println("Unable to perform move action due to: " + e1);
			abortMove();
			return;
		}
		ViewDropTarget dropTarget = findDropTargetUnderMouse(e.getPoint());
		if (dropTarget == null)
			abortMove();
		else if (dropTarget == dragSource)
		{
			getTransactionManager().executeCurrentTransaction();
			// Moved views are pulled up to root so we have to restore the original parent 
			targetView.changeParent((ParentView)dropTarget);		
		}
		else if (dropTarget.acceptDrop(targetView))
		{
			targetView.changeParent((ParentView)dropTarget);
			getTransactionManager().executeCurrentTransaction();
		}
		else
			abortMove();
		getViewPanel().updateCashedViewValues();
	}

	public List getTransferableViews()
	{
		return transferableViews;
	}

	
	private void abortMove()
	{
		getTransactionManager().abortCurrentTransaction();
		targetView.setLocation(originalLocation);
		targetView.changeParent(dragSource);
//		targetView.updateViewFromModel();		// TODO: Restore original parent
		getViewPanel().updateCashedViewValues();
		getViewPanel().repaint();		
	}


	public void changeToolDispatcher(ToolDispatcher newToolDispatcher)
	{
		ViewPanel oldViewPanel = toolDispatcher.getViewPanel();
		Rectangle dirty = targetView.getDirtyRegion().getBounds();
		System.out.println("MoveTool.changeToolDispather");
		toolDispatcher.popTool();
		toolDispatcher = newToolDispatcher;
		toolDispatcher.pushTool(this);
		targetView.changeParent(getViewPanel().getRootView());
		oldViewPanel.repaint(dirty);
	}
	
}
