package se.bluebrim.view.tool;

import javax.swing.*;

/**
 * Implemented by objects that has a Toolbar
 * 
 * @author G Stack
 */
public interface ToolBarOwner extends ToolDispatcher
{
	public ButtonGroup getToolGroup();
	public JToolBar getToolBar();

}
