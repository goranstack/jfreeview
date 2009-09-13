package se.bluebrim.view.tool;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import se.bluebrim.view.View;
import se.bluebrim.view.ViewDropTarget;
import se.bluebrim.view.dnd.MouseEventDispatcher;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.select.SelectionManager;
import se.bluebrim.view.swing.ViewPanel;
import se.bluebrim.view.transaction.TransactionManager;
import se.bluebrim.view.value.IntegerValue;



/**
 * A Tool gets mouse and key event from its ToolBarOwner and performs various actions 
 * when recieving these events depending on what kind of tool it is. <br>
 * There are two kinds of tools:
 * <ul>
 * <li>Main tools that is visible in the tool bar as an button and selectable by the user</li>
 * <li>Subtools are temporary tools that take over from a main tool to handle a certain task.
 * For example the DragAndDropTool is a sub tool that takes control when the user starts a drag.</li>
 * </ul>
 * 
 * Tags for LightUML see: <a href=http://www.spinellis.gr/sw/umlgraph/doc/indexw.html>Drawing UML Diagrams with UMLGraph</a>
 * @assoc 0..* - "0..1 owned by" ToolBarOwner
 *
 * @author G Stack
 *
 */
public abstract class Tool
{
	public static final IntegerValue DOUBLE_CLICK_DELAY = new IntegerValue(0);
	private Timer doubleClickTimer;

	protected Point mousePressedPoint;
	protected ToolDispatcher toolDispatcher;
	protected JButton toolBarButton;
	protected boolean popupTriggered = false;
	private TransactionManager transactionManager;
	private PropertyChangeListener viewsAbsoleteListener;


	public Tool(ToolDispatcher toolDispatcher)
	{
		this.toolDispatcher = toolDispatcher;
		viewsAbsoleteListener = new PropertyChangeListener(){
		
					public void propertyChange(PropertyChangeEvent evt)
					{
						allViewsAbsolete();				
					}};
		getViewPanel().addPropertyChangeListener(ViewPanel.ROOT_VIEW, viewsAbsoleteListener);

	}
	
	public void dispose()
	{
		getViewPanel().removePropertyChangeListener(ViewPanel.ROOT_VIEW, viewsAbsoleteListener);
	}
	
	/**
	 * A complete rebuild of all views has occured. Throw all views and get new ones
	 */
	protected void allViewsAbsolete()
	{
		
	}
	
	protected Point screenToComponent(Point screenPoint)
	{
		Point p2 = getViewPanel().getLocationOnScreen();
		return new Point(screenPoint.x - p2.x, screenPoint.y - p2.y);
	}

	public void mousePressed(MouseEvent e)
	{
		mousePressedPoint = new Point(e.getPoint());
	}
	
	public ViewPanel getViewPanel()
	{
		return toolDispatcher.getViewPanel();
	}
	
	protected Frame getDialogParent()
	{
		return getTransactionManager() != null ? getTransactionManager().getDialogParent() : JOptionPane.getFrameForComponent(getViewPanel());
	}
	
	protected SelectionManager getSelectionManager()
	{
		return getViewPanel().getSelectionManager();
	}
	
	protected ViewContext getViewContext()
	{
		return getViewPanel().getViewContext();
	}
		
	/**
	 * The tool has been selected. Select the tool button in the tool bar
	 * and inform zoom controller about change of current tool.
	 */
	public void select()
	{
		((ToolBarOwner)toolDispatcher).getToolGroup().setSelected(toolBarButton.getModel(), true);
		toolDispatcher.setCurrentTool(this);
	}
	
	/**
	 * Load the specified image that should be found in class path and 
	 * creates a Cursor from that image.
	 */
	protected Cursor createCursor(Class resourceAnchor, String imageName, Point hotSpot, String cursorName)
	{
		Cursor cursor;
		Image image = new ImageIcon(resourceAnchor.getResource(imageName)).getImage();
		cursor = Toolkit.getDefaultToolkit().createCustomCursor(image, hotSpot,  cursorName);
		return cursor;
	}
	
	
	public void mouseDragged(MouseEvent e)
	{
		
	}

	public void mouseReleased(MouseEvent e)
	{
		exitTool(e);
	}
	
	protected void exitTool(MouseEvent e)
	{
	}

	/**
	 * Returned from a subtool
	 *
	 */
	protected void enterTool(MouseEvent e)
	{
		setCursor();
	}

	public void mouseClickedOneOrMoreTimes(MouseEvent e)
	{
		if (e.getClickCount() == 2) 
		{
			doubleClickTimer.cancel();
			mouseDoubleClicked(e);
		} 
		else 
			if (e.getClickCount() == 1) 
			{
				waitForDoubleClick(e);
			}
		
	}
	
	private void waitForDoubleClick(final MouseEvent e)
	{
	    doubleClickTimer = new Timer();
	    doubleClickTimer.schedule(new TimerTask() {
	            public void run() 
	            {
	            	mouseClicked(e);
	            }
	        }, DOUBLE_CLICK_DELAY.value);
	}


	public void mouseDoubleClicked(MouseEvent e)
	{
		
	}
	
	public void mouseClicked(MouseEvent e)
	{
		
	}


	public void mouseEntered(MouseEvent e)
	{
		
	}

	public void mouseExited(MouseEvent e)
	{
		
	}

	public void mouseMoved(MouseEvent e)
	{
		
	}
	
	protected View getTopMostViewUnderMouse(List viewsUnderMouse)
	{
		return viewsUnderMouse.size() > 0 ? (View)viewsUnderMouse.get(0) : null;
	}
		
	protected List findViewsUnderMouse(Point point)
	{
		List viewsUnderMouse = getViewPanel().getViewsUnderMouse(point, View.ACCEPT_ALL_VIEW_FILTER);
		Collections.reverse(viewsUnderMouse); 
		return viewsUnderMouse;
	}
	
	public ViewDropTarget findDropTargetUnderMouse(Point point)
	{
		List dropTargets = getViewPanel().getViewsUnderMouse(point, View.ONLY_DROP_TARGET_VIEW_FILTER);		
		return dropTargets.size() > 0 ? (ViewDropTarget)dropTargets.get(0) : null;
	}

	/**
	 * Use XOR drawing to handle the damage repair for much faster drawing
	 * than repainting with repaintmanager. To repair after a draw just draw
	 * the shape again.
	 */
	protected void drawOutline(Graphics2D g, Rectangle rect)
	{
		if (rect.getWidth() > 0 || rect.getHeight() > 0)	// Avoid overlapping frame drawing with XOR mode
		{
			Color oldColor = g.getColor();
			g.setColor(Color.BLACK);
			g.setXORMode(Color.white);
			g.draw(rect);
			g.setColor(oldColor);
			g.setPaintMode();
		}
	}

	/**
	 * Repaint a view that has changed state. When the change of state affects
	 * the dirty region for example a change of the frame thickness the repaint
	 * is done with the union of the dirty region before and after the state change.
	 */
	protected void repaint(View view, Shape previousDirtyRegion)
	{
		Rectangle2D bounds = previousDirtyRegion.getBounds2D().createUnion(view.getDirtyRegion().getBounds2D());
		repaint(bounds);
	}

	protected void repaint(Rectangle2D bounds)
	{
//		System.out.println("Tool.repaint viewPanel bounds: " + bounds);
		getViewPanel().repaint(bounds.getBounds());
	}

	protected TransactionManager getTransactionManager()
	{
		return transactionManager != null ? transactionManager : toolDispatcher.getViewPanel().getViewContext();
	}
	
	public void abort()
	{
		exitTool(null);
	}
	
	public List getTransferableViews()
	{
		return null;
	}

	public void dropEntered(MouseEvent event, List transferableViews)
	{
		
	}

	public void dropReleased(MouseEvent event, List transferableViews, ViewPanel dragSource)
	{
		
	}

	public void dropExited(MouseEvent event)
	{
		
	}

	public void repairDamage()
	{
		
	}

	public void setTransactionManager(TransactionManager transactionManager)
	{
		this.transactionManager = transactionManager;
	}
	
	public void setCursor()
	{
		getViewPanel().setCursor(Cursor.getDefaultCursor());
	}

	public void highLight(ViewDropTarget view, boolean highLight)
	{
		if (view != null && view.isHighLighted() != highLight && view.isDropTarget())
		{
			Shape dirtyRegionBeforeChange = view.getDirtyRegion();
			view.setHighLighted(highLight);
			repaint(view, dirtyRegionBeforeChange);
		}
	}

	public JButton getToolBarButton()
	{
		return toolBarButton;
	}
	
	protected boolean isReadOnly()
	{
		return toolDispatcher.isReadOnly();
	}
	
	protected MouseEventDispatcher getMouseEventDispatcher()
	{
		return toolDispatcher.getMouseEventDispatcher();
	}

	
}