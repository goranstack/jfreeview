package se.bluebrim.view.tool;

import java.awt.Point;
import java.awt.event.MouseEvent;

import se.bluebrim.view.Handle;
import se.bluebrim.view.impl.ActionModifiers;

/**
 * ResizeTool is a sub tool that is created by SelectionTool when the user initialize
 * a view resize by starting a drag on a resize handle view. The ResizeTool will take over as current
 * tool as long the drag is going on. When the drag is interrupted the ResizeTool return the control
 * to the SelectionTool.
 * 
 * @author G Stack
 *
 */
public class ResizeTool extends HandleTool
{
	
	public ResizeTool(ToolDispatcher toolDispatcher, Handle handle, Point mousePressedPoint)
	{
		super(toolDispatcher, handle, mousePressedPoint);
	}
		
	
	public void mouseReleased(MouseEvent e)
	{
		super.mouseReleased(e);
		getViewPanel().setPaintableHints(null);
		resizeAction(new ActionModifiers(e.getModifiers()));
		if (!getTransactionManager().isDataChangeNotified())
			getViewPanel().invalidate();
		getViewPanel().repaint();		// Paint the unconfirmed changes state
	}

	/**
	 * Let the view update its model and execute the current transaction to make
	 * the changes permanent.
	 */
	private void resizeAction(ActionModifiers modifiers)
	{
		try
		{
			handle.stopMoving(modifiers);
			}
		catch (RuntimeException e)
		{
			System.out.println("Unable to perform resize action due to: " + e);
			getTransactionManager().abortCurrentTransaction();
			handle.getTargetView().updateViewFromModel();
			getViewPanel().repaint();
			return;
		}
		getViewPanel().updateCashedViewValues();
		getTransactionManager().executeCurrentTransaction();
	}


}
