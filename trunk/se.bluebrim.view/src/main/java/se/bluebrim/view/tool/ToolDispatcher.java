package se.bluebrim.view.tool;

import java.awt.event.MouseEvent;
import java.util.List;

import se.bluebrim.view.dnd.MouseEventDispatcher;
import se.bluebrim.view.swing.ViewPanel;

/**
 * A <code>ToolDispatcher</code> recieve user events and delegate the handling of
 * each event to the current tool <br>
 * 
 * @author G Stack
 *
 */
public interface ToolDispatcher
{
	public void setCurrentTool(Tool currentTool);
	public void setMouseEventDispatcher(MouseEventDispatcher mouseEventDispatcher);
	public Tool getCurrentTool();
	public void pushTool(Tool tool);
	public void popTool();
	
	/**
	 * Return the component that is the target for the tools. The cursor
	 * will shift to the tool cursor when entering the component. The
	 * gesture geometry is relative to the component.
	 */
	public ViewPanel getViewPanel();
	
	public void mouseDragged(MouseEvent e);

	public void mouseMoved(MouseEvent e);

	public void mouseClicked(MouseEvent e);
	
	public void mouseEntered(MouseEvent e);

	public void mouseExited(MouseEvent e);

	public void mousePressed(MouseEvent e);

	public void mouseReleased(MouseEvent e);
	
	public List getTransferableViews();
	
	public void dropReleased(MouseEvent event, List transferableViews, ViewPanel dragSource);
	
	public void dropEntered(MouseEvent event, List transferableViews);
	
	public void dropExited(MouseEvent event);
	
	public void abortCurrentTool();
	
	public void repairDamage();
	
	public boolean isReadOnly();

	public MouseEventDispatcher getMouseEventDispatcher();
	
	/**
	 * Temp fix for MoveTool
	 */
//	public Tool getPreviousTool();
//	public void setPreviousTool(Tool previousTool);

	
}
