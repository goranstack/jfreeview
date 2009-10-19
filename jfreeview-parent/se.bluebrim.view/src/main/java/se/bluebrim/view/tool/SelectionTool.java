package se.bluebrim.view.tool;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import se.bluebrim.view.Handle;
import se.bluebrim.view.ParentView;
import se.bluebrim.view.PopupMenuProvider;
import se.bluebrim.view.TransferableView;
import se.bluebrim.view.View;
import se.bluebrim.view.impl.BasicView;
import se.bluebrim.view.impl.MoveHandle;
import se.bluebrim.view.impl.SlaveView;
import se.bluebrim.view.swing.ViewPanel;

/**
 * The selection tool is represented by an arrow icon on the tool button
 * in the tool bar. It's used for selecting views by clicking or rubber banding.
 * A drag on a transferable view starts a drag operation by delegating the operation
 * to the DragAndDropTool sub tool.
 * 
 * @author G Stack
 *
 */
public class SelectionTool extends RubberBandingTool
{
	private List viewsUnderMouse;
	private Point lastMousePoint;
	
	public SelectionTool(ToolDispatcher toolDispatcher, boolean addAsFirstTool)
	{
		super(toolDispatcher, addAsFirstTool);
		viewsUnderMouse = new ArrayList();
	}

	/**
	 * Call back from our listener listen for rootView swap in the viewPanel.
	 * When working with a server it's common that the whole view hierarchy is
	 * replaced as a result of a server update. We must throw our views and get new ones.
	 */
	protected void allViewsAbsolete()
	{
		super.allViewsAbsolete();
		// It should have a value but the effect of skipping these lines are less than a NPE
		if (lastMousePoint != null)
		{
			viewsUnderMouse = findViewsUnderMouse(lastMousePoint);
		} else
		{
			viewsUnderMouse = new ArrayList();
		}
	}
	
	protected Icon getIcon()
	{
		return new ImageIcon(getClass().getResource("selection.gif"));
	}

	/**
	 * e is allowed to be null
	 */
	protected void exitTool(MouseEvent e)
	{
		super.exitTool(e);
		viewsUnderMouse = new ArrayList();
		lastMousePoint = e != null ? e.getPoint() : null;
	}

	/**
	 * The tool is selected. Call super to select the tool button and change the cursor
	 * to the standard cursor of the platform which probably is the most suitable cursor
	 * for the selection tool. Most common is an arrow cursor.
	 */
	public void select()
	{
		super.select();
		setCursor();			
	}

			
	public void mouseClicked(MouseEvent e)
	{
		if (!popupTriggered)
		{
			getViewPanel().hit(new SingleClickGesture(e));
		}
		lastMousePoint = e.getPoint();
	}
	
	public void mouseDoubleClicked(MouseEvent e)
	{
		getViewPanel().hit(new DoubleClickGesture(e));
		lastMousePoint = e.getPoint();
	}
	
	/**
	 * Collect hitted views and tell selection manager to select them. 
	 */
	protected void rubberBandAction(MouseEvent e, Rectangle rect)
	{
		if (rect.getWidth() > 2 && rect.getHeight() > 2)
		{
			RubberBandGesture rubberBandGesture = new RubberBandGesture(e, rect);
			ViewPanel viewPanel = getViewPanel();
			viewPanel.hit(rubberBandGesture);
			viewPanel.getSelectionManager().select(rubberBandGesture.getHittedViews(), e);
		}
	}
	
	public void mousePressed(MouseEvent e)
	{
		viewsUnderMouse = findViewsUnderMouse(e.getPoint());		
		PopupMenuProvider popupMenuProvider = getTopMostPopupMenuProviderUnderMouse();
		if (e.isPopupTrigger() && popupMenuProvider instanceof PopupMenuProvider)
		{
			popupMenuProvider.showPopupMenu(getViewPanel(), e.getPoint());
			popupTriggered = true;
		}
		else
		{
			popupTriggered = false;
			super.mousePressed(e);
		}
		lastMousePoint = e.getPoint();
	}

	public void mouseReleased(MouseEvent e)
	{
		super.mouseReleased(e);
		viewsUnderMouse = findViewsUnderMouse(e.getPoint());
		PopupMenuProvider popupMenuProvider = getTopMostPopupMenuProviderUnderMouse();
		if (e.isPopupTrigger() && popupMenuProvider instanceof PopupMenuProvider)
		{
			popupMenuProvider.showPopupMenu(getViewPanel(), e.getPoint());
			popupTriggered = true;
		}
		else
		{
			popupTriggered = false;
			super.mouseReleased(e);
		}
		lastMousePoint = e.getPoint();
	}
	
	public void mouseDragged(MouseEvent e)
	{
		View hittedView = getHittedView();
		if (hittedView instanceof Handle)
		{
			hittedView.putOnTop();
			startResizeTool((Handle)hittedView);
		}
		else
		{
			TransferableView topMostTransferableViewUnderMouse = getTopMostTransferableUnderMouse();
			if (topMostTransferableViewUnderMouse != null)
				startDragAndDrop(e, topMostTransferableViewUnderMouse);
			else
				super.mouseDragged(e);
		}
		lastMousePoint = e.getPoint();
	}

	private void startDragAndDrop(MouseEvent e, TransferableView topMostTransferableViewUnderMouse)
	{
		if (DragAndDropTool.USE_MOVE_HANDLE.value)
		{
			BasicView transferableView = (BasicView)getTransferableViews(topMostTransferableViewUnderMouse).get(0);
			MoveHandle moveHandle = new MoveHandle(transferableView, e.getPoint(), this);
			transferableView.pullUpToRoot();
			transferableView.putOnTop();
			transferableView.addHandle(moveHandle);
			new MoveTool(toolDispatcher, moveHandle, e.getPoint());
		} else
		{
			topMostTransferableViewUnderMouse.putOnTop();
			new DragAndDropTool(toolDispatcher, getTransferableViews(topMostTransferableViewUnderMouse), e.getPoint());
		}
	}
	
	
	private TransferableView getTopMostTransferableUnderMouse()
	{
		if (viewsUnderMouse != null)
		{
			for (ListIterator iter = viewsUnderMouse.listIterator(viewsUnderMouse.size()); iter.hasPrevious();) 
			{
				View view = (View) iter.previous();
				if (view instanceof TransferableView)
					return (TransferableView)view;
			}
		}
		return null;
	}

	
	private PopupMenuProvider getTopMostPopupMenuProviderUnderMouse()
	{
		for (ListIterator iter = viewsUnderMouse.listIterator(viewsUnderMouse.size()); iter.hasPrevious();) 
		{
			View view = (View) iter.previous();
			if (view instanceof PopupMenuProvider)
				return (PopupMenuProvider)view;
		}
		return null;
	}



	/*
	 * For debugging purposes
	 */
	private void printViewsUnderMouse()
	{
		System.out.println("SelectionTool printViewsUnderMouse:");
		for (Iterator iter = viewsUnderMouse.iterator(); iter.hasNext();) 
		{
			View view = (View) iter.next();
			System.out.println(view.toString());
		}
	}
	

	private void startResizeTool(Handle handle)
	{
		new ResizeTool(toolDispatcher, handle, mousePressedPoint);
	}
	

	/**
	 *  If no views are selected the view under mouse is returned as a single element in the list.
	 *  If more than one view is selected return only those with the same parent as the first one.
	 */
	private List getTransferableViews(TransferableView viewUnderMouse)
	{
		if (getSelectionManager().isSelected(viewUnderMouse))
		{			
			List selectedViews = getSelectionManager().getSelection(View.ONLY_TRANSFERABLE_VIEW_FILTER);
			if (selectedViews.size() > 1)
			{
				ParentView firstParent = ((TransferableView)selectedViews.get(0)).getParent();
				List haveSameParent = new ArrayList();
				for (Iterator iter = selectedViews.iterator(); iter.hasNext();)
				{
					TransferableView transferableView = (TransferableView)iter.next();
					if (transferableView.getParent().equals(firstParent))
						haveSameParent.add(transferableView);
				}
				return haveSameParent;
			} else
			return selectedViews;
		}
		else
		{
			List list = new ArrayList();
			list.add(viewUnderMouse);
			return list;
		}		
	}

	public void mouseMoved(MouseEvent e)
	{
		viewsUnderMouse = findViewsUnderMouse(e.getPoint());
		setCursor();
		lastMousePoint = e.getPoint();
	}
	
	protected void enterTool(MouseEvent e)
	{
//		System.out.println("SelectionTool.enterTool");
		if (e != null)
			viewsUnderMouse = findViewsUnderMouse(e.getPoint());
		super.enterTool(e);
		if (e != null)
			lastMousePoint = e.getPoint();
	}


	public void setCursor()
	{
		View hittedView = getHittedView();
		if (hittedView != null)
			getViewPanel().setCursor(hittedView.getCursor());
		else
			getViewPanel().setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * Slave views has precedence
	 */
	private View getHittedView()
	{
		if (viewsUnderMouse.size() < 1)
			return null;
		if (viewsUnderMouse.get(viewsUnderMouse.size()-1) instanceof TransferableView)
			return (View)viewsUnderMouse.get(viewsUnderMouse.size()-1);
		View topMostHitted = getTopMostSlaveViewUnderMouse();
		if (topMostHitted != null)
			return topMostHitted;
		else
			return getTopMostTransferableUnderMouse();
	}


	private SlaveView getTopMostSlaveViewUnderMouse()
	{
		for (ListIterator iter = viewsUnderMouse.listIterator(viewsUnderMouse.size()); iter.hasPrevious();) 
		{
			View view = (View) iter.previous();
			if (view instanceof SlaveView)
				return (SlaveView)view;
		}
		return null;
	}

		
}