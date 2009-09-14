package se.bluebrim.view.tool;

import javax.swing.*;

import se.bluebrim.view.swing.ViewPanel;
/**
 * 
 * @author G Stack
 *
 */
public class MultipleToolDispatcher extends AbstractToolDispatcher implements ToolBarOwner
{
	private ButtonGroup toolGroup;
	private JToolBar toolBar;

	public MultipleToolDispatcher(ViewPanel viewPanel, boolean readOnly, JToolBar toolBar)
	{
		super(viewPanel, readOnly);
		this.toolBar = toolBar;
		toolGroup = new ButtonGroup();
	}
	
	public ButtonGroup getToolGroup()
	{
		return toolGroup;
	}
	
	public JToolBar getToolBar()
	{
		return toolBar;
	}
	
	public SelectionTool addSelectionTool()
	{
		SelectionTool selectionTool = new SelectionTool(this, false);
		selectionTool.setTransactionManager(viewPanel.getViewContext());
		selectionTool.select();
		setCurrentTool(selectionTool);
		return selectionTool;
	}



}
