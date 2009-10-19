package se.bluebrim.view.tool;

import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Stack;

import se.bluebrim.view.dnd.MouseEventDispatcher;
import se.bluebrim.view.swing.ViewPanel;

/**
 * Abstract super class to objects that recieve user events and delegate 
 * the handling of each event to the current tool. <br>
 * This class also contains a new aproach to tool management that will
 * fit better with the experimental MoveTool class. Since the same MoveTool
 * instance is used when dragging between ViewPanels the present solution where
 * the tool it self keep a reference to the tool that will take over when the
 * MoveTool is terminated.

 * 
 * @author G Stack
 *
 */
public abstract class AbstractToolDispatcher implements ToolDispatcher
{
	private MouseEventDispatcher mouseEventDispatcher;
	private Stack toolStack;
//	private Tool previousTool;			// Temp fix for MoveTool. When MoveTool is allways used rework this
//	protected Tool currentTool;
	protected ViewPanel viewPanel;
	protected boolean readOnly;		// Used to turn off mouse dragged

	public AbstractToolDispatcher(ViewPanel viewPanel, boolean readOnly)
	{
		this.viewPanel = viewPanel;
		this.readOnly = readOnly;
		toolStack = new Stack();
	}

	public void setCurrentTool(Tool newCurrentTool)
	{
		Tool currentTool = getCurrentTool();
		if (currentTool != null)
			currentTool.dispose();
		toolStack.clear();
		toolStack.push(newCurrentTool);
	}

	public Tool getCurrentTool()
	{
		return toolStack.empty() ? null :(Tool)toolStack.peek();
	}

	public ViewPanel getViewPanel()
	{
		return viewPanel;
	}

	public void mouseDragged(MouseEvent e)
	{
		if (!readOnly)
			getCurrentTool().mouseDragged(e);
	}

	public void mouseMoved(MouseEvent e)
	{
		getCurrentTool().mouseMoved(e);

	}

	public void mouseClicked(MouseEvent e)
	{
		getCurrentTool().mouseClickedOneOrMoreTimes(e);

	}

	public void mouseEntered(MouseEvent e)
	{
		getCurrentTool().mouseEntered(e);
	}

	public void mouseExited(MouseEvent e)
	{
		getCurrentTool().mouseExited(e);
	}

	public void mousePressed(MouseEvent e)
	{
		getCurrentTool().mousePressed(e);
	}

	public void mouseReleased(MouseEvent e)
	{
		getCurrentTool().mouseReleased(e);
	}

	public void dropEntered(MouseEvent event, List transferableViews)
	{
		getCurrentTool().dropEntered(event, transferableViews);		
	}

	public void dropReleased(MouseEvent event, List transferableViews, ViewPanel dragSource)
	{
		getCurrentTool().dropReleased(event, transferableViews, dragSource);		
	}

	public List getTransferableViews()
	{
		return getCurrentTool().getTransferableViews();
	}
	
	public void dropExited(MouseEvent event)
	{
		getCurrentTool().dropExited(event);		
	}

	public void abortCurrentTool()
	{
		getCurrentTool().abort();		
	}
	
	public void repairDamage()
	{
		getCurrentTool().repairDamage();		
	}

	public boolean isReadOnly()
	{
		return readOnly;
	}
	
	public void pushTool(Tool tool)
	{
		toolStack.push(tool);		
	}
	
	public void popTool()
	{
		toolStack.pop();		
	}

	public void setMouseEventDispatcher(MouseEventDispatcher mouseEventDispatcher)
	{
		this.mouseEventDispatcher = mouseEventDispatcher;
	}

	public MouseEventDispatcher getMouseEventDispatcher()
	{
		return mouseEventDispatcher;
	}
	
}
