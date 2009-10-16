package se.bluebrim.view.dnd;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import se.bluebrim.view.swing.ViewPanel;
import se.bluebrim.view.tool.DragAndDropTool;
import se.bluebrim.view.tool.GlobalTool;
import se.bluebrim.view.tool.ToolDispatcher;


/**
 * A <strong>MouseEventDispatcher</strong> is used to handle drag and drop of Views 
 * between ViewPanels. This home brewed solution was choosen after serious attemt to 
 * integrate the JDK dnd api with the view framework. But it was difficult to make it work
 * for various reasons:
 * <ul>
 * <li>No Transferable object is avaiable in the <code>DropTargetListener.dragOver(DropTargetDragEvent dtde)</code> call which
 * is neccesary for giving a correct feedback during dragOver</li>
 * <li>The dnd api pumps events even when the mouse is still which means a lot of anoying flickering. To filter out events on the spot
 * means that the aoutscroll wont't work</li>
 * <li><code>DragAndDropTool</code> has a autoscroll mechanism implemented "by the book" in the mouseDragged method that works perfectly
 * when dragging around a view in a ViewPanel. The autoscroll mechanism is an "oneliner" and works thanks to the synthetic mouse events that is
 * generated when the mouse is dragged outside a component. How outoscroll is supposed to work when using the dnd api is hard to understand.</li>
 * <li>Call's to <code>DropTargetDragEvent.acceptDrag(int dragOperation)</code> and <code>DropTargetDragEvent.rejctDrag(int dragOperation)</code>
 * has no effect on the cursor</li>
 * <li>Key modifiers works in a different way in the JDK dnd api and its uncertain if they are avaiable when needed<li> 
 * 
 * Instead of struggle with the JDK dnd api it's much more easy to handle the drag and drop by this class. This is how it works: <br>
 * Create an instance of this class and add all ViewPanels that should support drag and drop beetween them. The added ViewPanels must
 * be children on the first level to a container of any kind. Add the MouseEventDispatcher as MouseListener and MouseMotionListener to that
 * container. In case the ViewPanel resides in a JScrollPane you must add the MouseEventDispatcher as MouseListener and MouseMotionListener to
 * the JScrollPane as well since it consumes all mouse events in its area<br>
 * When this is done the MouseEventDispatcher detects if the mouse is over a ViewPanel and delegates the mouse event to that ViewPanel. If
 * a mouse dragged is started in one ViewPanel and then moved to another ViewPanel the dropEntered-method is called in that ViewPanel. 
 * Then as long the drag is moving around in entered ViewPanel mouseDragged called If the mouse
 * is released in the without leaving the view panel the dropRelease method is called.
 * 
 * @author G Stack
 *
 */
public class MouseEventDispatcher implements MouseListener, MouseMotionListener
{
	private static final int AUTOSCROLL_THRESHOLD = 12;	// The distance to the edge that trigger autoscroll
	private static final int AUTOSCROLL_STEP = 15;

	/**
	 * TODO: What if the AUTOSCROLL_THRESHOLD is to large for the scroll rect.
	 * Perhaps we should reduce the AUTOSCROLL_THRESHOLD in that case.
	 */
	class AutoscrollTrigger
	{		
		// How near is the mouse to the edge
		int leftSideDistance;
		int topDistance;
		int rightSideDistance;
		int bottomDistance;
		
		// How much is there to scroll
		int leftScrollSpace;
		int topScrollSpace;
		int rightScrollSpace;
		int bottomScrollSpace;
		
		public AutoscrollTrigger(Point mouse, Dimension scrollClientSize, Rectangle scrollRect)
		{			
			leftSideDistance = mouse.x - scrollRect.x;
			rightSideDistance = scrollRect.x + scrollRect.width - mouse.x;
			topDistance = mouse.y - scrollRect.y;
			bottomDistance = scrollRect.y + scrollRect.height - mouse.y;
			
			leftScrollSpace = scrollRect.x;
			rightScrollSpace = scrollClientSize.width - (scrollRect.x + scrollRect.width);
			topScrollSpace = scrollRect.y;
			bottomScrollSpace = scrollClientSize.height - (scrollRect.y + scrollRect.height);
		}

		/**
		 * Answer true if we are near at least one of the edges and there is
		 * more to scroll at that edge
		 */
		public boolean isInAutoscrollZone()
		{
			return nearLeftSide() && leftScrollSpace > 0 ||
			nearTop() && topScrollSpace > 0 ||
			nearRightSide() && rightScrollSpace > 0 ||
			nearBottom() && bottomScrollSpace > 0;
		}
		
		private boolean nearLeftSide()
		{
			return leftSideDistance < AUTOSCROLL_THRESHOLD;
		}

		private boolean nearTop()
		{
			return topDistance < AUTOSCROLL_THRESHOLD;
		}

		private boolean nearBottom()
		{
			return bottomDistance < AUTOSCROLL_THRESHOLD;
		}

		private boolean nearRightSide()
		{
			return rightSideDistance < AUTOSCROLL_THRESHOLD;
		}
		/**
		 * Translate the mouse point to be slightly outside the edge it is near.
		 * The metod assumes that we cant be near both the left and right edge or
		 * top and bottom edge at the same time.
		 */
		public void translateMouseEvent(MouseEvent event)
		{
			if (nearTop())
				event.translatePoint(0, -(topDistance + AUTOSCROLL_STEP));
			else
				if (nearBottom())
					event.translatePoint(0, bottomDistance + AUTOSCROLL_STEP);
			if (nearLeftSide())
				event.translatePoint(-(leftSideDistance + AUTOSCROLL_STEP), 0);
			else
				if (nearRightSide())
					event.translatePoint(rightSideDistance + AUTOSCROLL_STEP, 0);
		}

		/*
		 * For debugging purposes
		 */
//		private void printContent()
//		{
//			System.out.println("Edge left: " + leftSideDistance + " top: " + topDistance + " right: " + rightSideDistance + " bottom: " + bottomDistance);
//			System.out.println("Scroll left: " + leftScrollSpace + " top: " + topScrollSpace + " right: " + rightScrollSpace + " bottom: " + bottomScrollSpace);
//		}
		
	}
	
	private List toolDispatchers;
	private ToolDispatcher mousePressedIn;
	private ToolDispatcher dropEnteredIn;
	private Component component;
	private GlobalTool globalTool;		// A Tool that is used over tool dispatcher boundaries for example MoveTool		
	private Timer autoScrollerTimer;
	private AutoscrollTrigger autoscrollTrigger;

	public MouseEventDispatcher(Component component)
	{
		toolDispatchers = new ArrayList();
		this.component = component;
		component.addMouseListener(this);
		component.addMouseMotionListener(this);
	}
	
	public void addToolDispatcher(ToolDispatcher toolDispatcher)
	{
		if (toolDispatcher == null)
			throw new IllegalArgumentException("Null is not legal in toolDispatcher");
		toolDispatcher.setMouseEventDispatcher(this);
		toolDispatchers.add(toolDispatcher);
	}

	public void mouseClicked(MouseEvent e)
	{
		MouseEvent event = createGlobalMouseEvent(e);
		ToolDispatcher mouseClickedIn = getToolDispatcherUnderMouse(event);
		if (mouseClickedIn != null)
		{
			mouseClickedIn.mouseClicked(createLocalMouseEvent(mouseClickedIn, event));
		}				
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent e)
	{
		MouseEvent event = createGlobalMouseEvent(e);
		mousePressedIn = getToolDispatcherUnderMouse(event);
		if (mousePressedIn != null)
		{
			mousePressedIn.mousePressed(createLocalMouseEvent(mousePressedIn, event));
		}				
	}
	/**
	 * The method identifies the following cases:
	 * <ol>
	 * <li>Mouse released in the panel that has recieved a drop enter. Call drop release of that panel.
	 * <li>Mouse released in the same panel as mouse pressed in. Call mouse released for that panel. 
	 * </ol>
	 */	
	public void mouseReleased(MouseEvent e)
	{
		if (autoScrollerTimer != null)
			autoScrollerTimer.stop();
		MouseEvent event = createGlobalMouseEvent(e);
		ToolDispatcher mouseReleasedIn = getToolDispatcherUnderMouse(event);
		// Case 1
		if (mouseReleasedIn != null && dropEnteredIn == mouseReleasedIn)
		{
			mouseReleasedIn.dropReleased(createLocalMouseEvent(mouseReleasedIn, event), mousePressedIn.getTransferableViews(), mousePressedIn.getViewPanel());
			mouseReleasedIn = null;
			repaintAllViewPanels();		// TODO: Should not be nessecary but for now it cleans everything that was left behind
		} else
		// Case 2
		if (mouseReleasedIn != null && mouseReleasedIn == mousePressedIn)
		{
			mousePressedIn.mouseReleased(createLocalMouseEvent(mousePressedIn, event));
			mousePressedIn = null;
		}
		// All other cases just to be sure:
		if (dropEnteredIn != null)
			dropEnteredIn.abortCurrentTool();
		if (mousePressedIn != null)
			mousePressedIn.mouseReleased(createLocalMouseEvent(mousePressedIn, event));		
		dropEnteredIn = null;
		mousePressedIn = null;
		resetToDefaultCursor();
	}

	/**
	 * The repair damage mechanism sometimes fails. This method will reapair
	 * all remaining damage at the cost of a complete repaint. <br>
	 * Abort all tools as well.
	 */
	private void repaintAllViewPanels()
	{
		for (Iterator iter = toolDispatchers.iterator(); iter.hasNext();)
		{
			ToolDispatcher toolDispatcher = (ToolDispatcher)iter.next();
			toolDispatcher.getViewPanel().repaint();
			toolDispatcher.getCurrentTool().abort();
		}
	}

	/**
	 * The method identifies the following cases:
	 * <ol>
	 * <li>Mouse dragged ouside all ViewPanels. Trigger autoscroll by calling mouseDragged to last 
	 * dragged in panel and set cursor to no drop if transfer is going on</li>
	 * <li>We have entered a drop and is still dragging around in the same ViewPanel. Call mouseDragged of the ViewPanel</li>
	 * <li>We are dragging around in the same Viewpanel as we started the drag in. Call mouseDragged of the ViewPanel</li>
	 * <li>We are dragging in a different ViewPanel from the one we started in. If the ViewPanel we started in has transferable views
	 * start a transfer by assigning the dropEnteredIn variable. </li>
	 * </ol>
	 */
	public void mouseDragged(MouseEvent e)
	{
		if (autoScrollerTimer != null)
			autoScrollerTimer.stop();
		final MouseEvent event = createGlobalMouseEvent(e);
		ToolDispatcher mouseDraggedIn = getToolDispatcherUnderMouse(event);
		if (mouseDraggedIn == mousePressedIn)		// We are back where we started. Clear drop entered in
			if (dropEnteredIn != null)
			{
//				System.out.println("We are back where we started");
				if (DragAndDropTool.USE_MOVE_HANDLE.value)
					globalTool.changeToolDispatcher(mouseDraggedIn);
				else
					dropEnteredIn.abortCurrentTool();
				dropEnteredIn = null;
			}
		autoscrollTrigger = null;
		if (mouseDraggedIn != null)
			autoscrollTrigger = createAutoscrollerTrigger(e, mouseDraggedIn.getViewPanel());
		if (mouseDraggedIn == null || autoscrollTrigger != null && autoscrollTrigger.isInAutoscrollZone())
		{
			final ToolDispatcher lastDraggedIn = getLastDraggedIn();
			if (lastDraggedIn != null)
			{
				lastDraggedIn.repairDamage();
				autoScrollerTimer = new Timer(100, new ActionListener(){
	
					public void actionPerformed(ActionEvent e)
					{
						MouseEvent localMouseEvent = createLocalMouseEvent(lastDraggedIn, event);
						if (autoscrollTrigger != null)
							autoscrollTrigger.translateMouseEvent(localMouseEvent);
						lastDraggedIn.mouseDragged(localMouseEvent);		// Trigger autoscroll
					}});
				autoScrollerTimer.start();
			}
		}
		
		// Case 1
		if (mouseDraggedIn == null)
		{
//			System.out.println("MouseEventDispatcher.mouseDragged case 1");
			setCursorDuringDrag();
			return;
		}
		// Case 2
		if (dropEnteredIn != null && dropEnteredIn == mouseDraggedIn)
		{
//			System.out.println("MouseEventDispatcher.mouseDragged case 2");
			mouseDraggedIn.mouseDragged(createLocalMouseEvent(mouseDraggedIn, event));
		}
		else
		// Case 3
		if (mouseDraggedIn == mousePressedIn)
		{
//			System.out.println("MouseEventDispatcher.mouseDragged case 3 ");
			mouseDraggedIn.mouseDragged(createLocalMouseEvent(mouseDraggedIn, event));
		}
		else
		{
			// Case 4
//			System.out.println("MouseEventDispatcher.mouseDragged case 4");
			List transferableViews = mousePressedIn.getTransferableViews();
			if (transferableViews != null)
			{
				// Exit from previous panel
				if (dropEnteredIn != null)
				{
					dropEnteredIn.dropExited(createLocalMouseEvent(dropEnteredIn, event));
//					System.out.println("MouseEventDispatcher drop exited");
				}
				dropEnteredIn = mouseDraggedIn;
				mouseDraggedIn.dropEntered(createLocalMouseEvent(mouseDraggedIn, event), transferableViews);
//				System.out.println("MouseEventDispatcher drop entered");
			}
			if (DragAndDropTool.USE_MOVE_HANDLE.value)
				globalTool.changeToolDispatcher(mouseDraggedIn);

		}
		ToolDispatcher lastDraggedIn = getLastDraggedIn();
		if (mouseDraggedIn != lastDraggedIn && lastDraggedIn != null)
			lastDraggedIn.repairDamage();

	}
	
	private AutoscrollTrigger createAutoscrollerTrigger(MouseEvent event, ViewPanel viewPanel)
	{
		Container parent = viewPanel.getParent();
		if (parent instanceof JViewport)
		{
			Point mousePoint = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), viewPanel);
			return new AutoscrollTrigger(mousePoint, viewPanel.getSize(), ((JViewport)parent).getViewRect());
		}
		return null;
	}

	private ToolDispatcher getLastDraggedIn()
	{
		return (dropEnteredIn != null) ? dropEnteredIn : mousePressedIn;
	}
	
	private void setCursorDuringDrag()
	{
		if (mousePressedIn != null && mousePressedIn.getTransferableViews() != null)
		{
			component.setCursor(DragSource.DefaultMoveNoDrop);
// This experiment was not successful. The cursor size is limited.
//			BufferedImage visualRepresentation = ((View)mousePressedIn.getTransferableViews().get(0)).getVisualRepresentation();
//			Point hotSpot = new Point(visualRepresentation.getWidth()/2, visualRepresentation.getHeight()/2);
//			Point hotSpot = new Point(0, 0);
//			Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(visualRepresentation, hotSpot, "transfered view");
//			component.setCursor(cursor);
		}
	}
	
	private void resetToDefaultCursor()
	{
		component.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	

	public void mouseMoved(MouseEvent e)
	{
		MouseEvent event = createGlobalMouseEvent(e);
		ToolDispatcher mouseMovedIn = getToolDispatcherUnderMouse(event);
		if (mouseMovedIn != null)
			mouseMovedIn.mouseMoved(createLocalMouseEvent(mouseMovedIn, event));
		else
			resetToDefaultCursor();
	}
	
	private ToolDispatcher getToolDispatcherUnderMouse(MouseEvent e)
	{
		for (Iterator iter = toolDispatchers.iterator(); iter.hasNext();)
		{
			ToolDispatcher toolDispatcher = (ToolDispatcher)iter.next();
			if (containsMouse(e, toolDispatcher))
				return toolDispatcher;
		}
		return null;
	}

	/**
	 * The mouse events this object is receiving has various orginators.
	 * It can be the container ore one of the ViewPanels. 
	 * This method creates a MouseEvent that looks if it came from our component.
	 */
	private MouseEvent createGlobalMouseEvent(MouseEvent e)
	{
		Component eventOrginator = e.getComponent();
		if (eventOrginator == component)
			return e;
		else
			return SwingUtilities.convertMouseEvent(e.getComponent(), e, component);
	}

	/**
	 * This method creates a MouseEvent that looks like the mouse events the ViewPanel
	 * should haved recieved when listen for mouse events.
	 */
	private MouseEvent createLocalMouseEvent(ToolDispatcher toolDispatcher, MouseEvent e)
	{
		if (toolDispatcher == null)
			return e;
		else
		{
			ViewPanel viewPanel = toolDispatcher.getViewPanel();
			return SwingUtilities.convertMouseEvent(e.getComponent(), e, viewPanel);
		}
	}

	/**
	 * Answer true if the mouse is over the ViewPanel in the specified ToolDispatcher
	 */
	private boolean containsMouse(MouseEvent e, ToolDispatcher toolDispatcher)
	{
		return toolDispatcher.getViewPanel() == SwingUtilities.getDeepestComponentAt(component, e.getPoint().x, e.getPoint().y);
	}

	public void setGlobalTool(GlobalTool globalTool)
	{
		this.globalTool = globalTool;
	}
}
