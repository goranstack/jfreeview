package se.bluebrim.view.tool;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import se.bluebrim.view.*;
import se.bluebrim.view.impl.*;

/**
 * A MainTool is tool that is visible in the tool bar.
 * 
 * @author G Stack
 *
 */
public abstract class MainTool extends Tool
{

	public MainTool(ToolDispatcher toolDispatcher)
	{
		this(toolDispatcher, false);
	}

	public MainTool(ToolDispatcher toolDispatcher, boolean addAsFirstTool)
	{
		super(toolDispatcher);
		if (toolDispatcher instanceof ToolBarOwner)		// TODO: This has to be done in a proper way
			installToolBarButton(addAsFirstTool);
	}

	
	/**
	 * Subclasses oveeride this to provide an icon for the tool button
	 * in the tool bar.
	 */
	protected abstract Icon getIcon();
	
	/**
	 * Creates the button for the tool and add the button to the tool bar.
	 * Register a listener to the button that selects the tool when buton is pressed. <br>
	 * addAsFirstTool = true place the button as the first button in the tool bar.
	 * A case where this is useful is when you want to add additional tools to
	 * the tools in the <code>ZoomController</code>
	 */
	private final void installToolBarButton(boolean addAsFirstTool)
	{
		toolBarButton = new JButton();
		toolBarButton.setContentAreaFilled(true);
		toolBarButton.setMargin(new Insets(0, 0, 0, 0));
		toolBarButton.setIcon(getIcon());
		if (addAsFirstTool)
			((ToolBarOwner)toolDispatcher).getToolBar().add(toolBarButton, 0);
		else
			((ToolBarOwner)toolDispatcher).getToolBar().add(toolBarButton);
		((ToolBarOwner)toolDispatcher).getToolGroup().add(toolBarButton);
		toolBarButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				select();
			}});
	}
	
	/**
	 * This method handle the case where a move has started in an other
	 * view panel and entered in the view panel of this tool.
	 */
	public void dropEntered(MouseEvent event, List transferableViews)
	{
		if (DragAndDropTool.USE_MOVE_HANDLE.value)
		{
//			BasicView transferableView = (BasicView)transferableViews.get(0);
//			transferableView.setParent(getViewPanel().getRootView());
//			MoveHandle moveHandle = new MoveHandle(transferableView, event.getPoint(), this);
//			transferableView.addHandle(moveHandle);
//			toolDispatcher.setCurrentTool(new MoveTool(toolDispatcher, moveHandle, event.getPoint()));
		} else
			new DragAndDropTool(toolDispatcher, copyViews(event.getPoint(), transferableViews), event.getPoint());				
	}

	private List copyViews(Point startPoint, List transferableViews)
	{
		List result = new ArrayList();
		for (Iterator iter = transferableViews.iterator(); iter.hasNext();)
		{
			TransferableView view = (TransferableView)iter.next();
			TransferableView transferableCopy;
			if (DragAndDropTool.USE_MOVE_HANDLE.value)
			{
				transferableCopy = view;		// They are already copied
				((Layoutable)transferableCopy).setX(startPoint.x - transferableCopy.getWidth()/2f);
				((Layoutable)transferableCopy).setY(startPoint.y - transferableCopy.getHeight()/2f);
			}
			else
			{
				transferableCopy = view.getTransferableCopy(getViewPanel().getViewContext());
				transferableCopy.setPositionDevice(startPoint.x - transferableCopy.getWidth()/2f, startPoint.y - transferableCopy.getHeight()/2f);
			}
			// Center view under mouse
			result.add(transferableCopy);
		}
		return result;
	}


}
