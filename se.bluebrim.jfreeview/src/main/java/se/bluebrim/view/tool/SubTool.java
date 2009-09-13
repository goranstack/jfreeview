package se.bluebrim.view.tool;

import java.awt.event.MouseEvent;

import se.bluebrim.view.transaction.TransactionManager;

/**
 * A SubTool is a tool that is not visible in he tool bar. A SubTool
 * is used to control certain gestures on a main tool. For example:
 * When the starts a drag on a handle the sub tool ResizeTool will take 
 * over as current tool as long the drag is going on. When the drag is interrupted 
 * the ResizeTool return the control to the SelectionTool.
 * 
 * @author G Stack
 *
 */
public abstract class SubTool extends Tool
{	

	public SubTool(ToolDispatcher toolDispatcher)
	{
		super(toolDispatcher);
		toolDispatcher.pushTool(this);
	}
		
	/**
	 * Give the control back to main tool
	 */
	protected void exitTool(MouseEvent e)
	{
		super.exitTool(e);
		toolDispatcher.popTool();
		toolDispatcher.getCurrentTool().enterTool(e);
	}
		
}
