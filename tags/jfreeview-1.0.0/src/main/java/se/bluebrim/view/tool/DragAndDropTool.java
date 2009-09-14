package se.bluebrim.view.tool;

import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import javax.swing.JComponent;

import se.bluebrim.view.*;
import se.bluebrim.view.dnd.MouseEventDispatcher;
import se.bluebrim.view.impl.*;
import se.bluebrim.view.paint.*;
import se.bluebrim.view.swing.ViewPanel;
import se.bluebrim.view.value.BooleanValue;

/**
 * DragAndDropTool is a sub tool that is created by SelectionTool when the user initialize
 * a view transfer by starting a drag on a transferable view. The DragAndDropTool will take over as current
 * tool as long the drag is going on. When the drag is interrupted the DragAndDropTool return the control
 * to the SelectionTool.<br>
 * TODO: Implement improved drag and drop feedback by using the glassPane in the frame.
 * Examples of this can be found at <a href=http://jroller.com/page/gfx/20050216#drag_with_style_in_swing>Drag with style in swing</a>
 * 
 * @author G Stack
 *
 */
public class DragAndDropTool extends SubTool
{	
	public static final BooleanValue USE_MOVE_HANDLE = new BooleanValue(false);		// For testing
	// Total drag distance from drag start	
	private int tddy;
	private int tddx;
	private List transferableViews;		// <View>
	private ViewDropTarget dropTarget;
	private Point startDragPoint;
	private Point lastDragPoint;
	private boolean damageToRepair;
	
	public DragAndDropTool(ToolDispatcher toolDispatcher, List transferableViews, Point startDragPoint)
	{
		super(toolDispatcher);
		this.startDragPoint = startDragPoint;
		this.transferableViews = transferableViews;
		startHandleManipulation();
		setCursor();
	}
	
	private void startHandleManipulation()
	{
		for (Iterator iter = transferableViews.iterator(); iter.hasNext();)
		{
			BasicView view = (BasicView)iter.next();
			view.setHidden(true);
			view.startHandleManipulation(new MoveHandle(view, new Point(0,0), this));		// Use a fake MoveHandle to that can deliver feedback views
		}
	}
		
	private void stopHandleManipulation(ActionModifiers modifiers)
	{
		for (Iterator iter = transferableViews.iterator(); iter.hasNext();)
		{
			BasicView view = (BasicView)iter.next();
			view.stopHandleManipulation(modifiers);
			view.setHidden(false);
		}
	}

	private boolean lockVerticalMovement(ActionModifiers actionModifiers)
	{
		return transferableViews.size() == 1 && ((Layoutable)transferableViews.get(0)).lockVerticalMovement(actionModifiers);
	}
	
	private boolean lockHorizontalMovement(ActionModifiers actionModifiers)
	{
		return transferableViews.size() == 1 && ((Layoutable)transferableViews.get(0)).lockHorizontalMovement(actionModifiers);
	}

	protected void allViewsAbsolete()
	{
		transferableViews = new ArrayList();
		dropTarget = null;
	}
	
	/**
	 * Use XOR technique to draw an outline of transfered views. Makes the
	 * outlines move along with the mouse by translating the Graphics.
	 */
	public void mouseDragged(MouseEvent mouseEvent)
	{
		JComponent component = getViewPanel();
		Point mousePoint = mouseEvent.getPoint();
		ActionModifiers actionModifiers = new ActionModifiers(mouseEvent);
		if (lockVerticalMovement(actionModifiers))
			mousePoint = new Point(mousePoint.x, startDragPoint.y);
		if (lockHorizontalMovement(actionModifiers))
			mousePoint = new Point(startDragPoint.x, mousePoint.y);
		
		Graphics2D g = (Graphics2D)component.getGraphics();
		tddy = mousePoint.y - startDragPoint.y;
		tddx = mousePoint.x - startDragPoint.x;
		Point newDragPoint = new Point(tddx, tddy);
		
		repairOutlineDamage(lastDragPoint, g);		// Repair previous damage
			
		// Autoscroll
		Rectangle r = new Rectangle(mousePoint.x, mousePoint.y, 1, 1);
		component.scrollRectToVisible(r);
		g = (Graphics2D)component.getGraphics();	// Get graphics again in case translation is changed due to autoscroll

		ViewDropTarget newDropTarget = findDropTargetUnderMouse(mousePoint);

		updateDropTarget(newDropTarget);
		setCursor();
		reflectDropTargetInTransferableView(newDropTarget);
		drawOutlines(newDragPoint, g);		// Draw new outline
		damageToRepair = true;
		if (lastDragPoint == null)
			getViewPanel().repaint();	// Fix for bug: Part of UnplannedCopiesView is left behind
		lastDragPoint = newDragPoint;
	}

	/**
	 * Sometimes its desirable to reflect aspects of the drop target in the
	 * transferable view. For example when a process view is dragged in a resource
	 * allocation application the length of the process view should reflect the capacity
	 * of the drop target resource.<br>
	 * The method assumes that we only have one transferable view.
	 */
	private void reflectDropTargetInTransferableView(ViewDropTarget newDropTarget)
	{
		if (newDropTarget != null && transferableViews.size() == 1)
			newDropTarget.reflectInTransferable((TransferableView)transferableViews.get(0));
	}

	/**
	 * Turn off high lighting of previous drop target if a new has arrived
	 */
	private void updateDropTarget(ViewDropTarget newDropTarget)
	{
		if (newDropTarget != dropTarget)
		{
			if (dropTarget != null)
				highLight(dropTarget, false);
			dropTarget = newDropTarget;
		}
	}

	/**
	 * Set cursor depending on if the drop target accept the transferables or not.
	 * Highlight drop target as well if the drop target accept the transferables.
	 * While dragging in source set move cursor
	 */
	public void setCursor()
	{
		if (dropTarget != null && acceptDropIn(dropTarget))
		{
			highLight(dropTarget, true);
			getViewPanel().setCursor(DragSource.DefaultMoveDrop);
		}
		else
			// Dragging in source
			if (dropTarget != null && dropTarget.isChildren(transferableViews))
				getViewPanel().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			else
				getViewPanel().setCursor(DragSource.DefaultMoveNoDrop);
	}
		
	protected boolean acceptDropIn(View view)
	{
		return view instanceof ViewDropTarget && ((ViewDropTarget)view).acceptDrop(transferableViews);
	}
	
	public void mouseReleased(MouseEvent e)
	{
		super.mouseReleased(e);
		mouseReleased(e, getViewPanel());
	}
	
	private void mouseReleased(MouseEvent e, ViewPanel viewPanel)
	{
		stopHandleManipulation(e == null ? null :new ActionModifiers(e));
		if (dropTarget != null)
			if (dropTarget.isChildren(transferableViews))
				moveAction(tddx, tddy, new ActionModifiers(e));
			else
				dropAction(tddx, tddy, new ActionModifiers(e), viewPanel);
		getViewPanel().repaint();	// TODO: Damage repair should have taken care of this		
	}

	protected void exitTool(MouseEvent e)
	{
		super.exitTool(e);
		// Repair damage for the last time
		repairDamage();
	}

	public void repairDamage()
	{
		if (damageToRepair)
		{
//			System.out.println("DragAndDropTool repair damage in " + getViewPanel().getName());
			if (lastDragPoint != null)
			{
				Graphics2D g = (Graphics2D)getViewPanel().getGraphics();
				repairOutlineDamage(lastDragPoint, g);
			}
			if (dropTarget != null)
				highLight(dropTarget, false);
			damageToRepair = false;
		}
	}
		

	private void moveAction(int horizontalDistance, int verticalDistance, ActionModifiers modifiers)
	{
		try
		{
			updateLocation(horizontalDistance, verticalDistance, modifiers);
		}
		catch (RuntimeException e)
		{
			System.out.println("Unable to perform move action due to: " + e);
			getTransactionManager().abortCurrentTransaction();
			return;
		}
		getViewPanel().repaint();		// Paint the final destination
		getTransactionManager().executeCurrentTransaction();
		if (!getTransactionManager().isDataChangeNotified())
			getViewPanel().invalidate();
		getViewPanel().repaint();		// Paint the unconfirmed changes state
	}

	/**
	 * The concept of fixed Y or X is a way to handle the fact that movement of a view during
	 * a drag and drop that is done to change the container of the view is not always should change
	 * the coordinates as well. For example when moving a resource allocation block from one resource to
	 * another the y-coordinate should not change but the x-coordinate will change to reflect the
	 * movement in time that i possible to do at the same time as change of resource. I have plans
	 * to replace most or all of the code in this class by the MoveHandle and then this aspect should
	 * be solved in a better way. The coordinates of a view that is dropped in a new container should
	 * perhaps be settled by some kind of double dispatching mechanism between the drop target and the
	 * transferable. How about running the layout manager of the drop target. It should know how to locate
	 * an added view and correct irrelevant coordinates. <br>
	 * In cases where a complete rebuild of views is performed the coordinates of a drop view doesn't really
	 * matter other than for the visible feedback that is produced by the hasUncommittedChanges flag. <br>
	 * While waiting for this to settle we also has the concept of lock movement to achieve immediate restrictions
	 * of vertically or horizontal movement. In the end we should only have one concept for restricting view location.
	 */
	private void updateLocation(int horizontalDistance, int verticalDistance, ActionModifiers modifiers)
	{
		for (Iterator iter = transferableViews.iterator(); iter.hasNext();) 
		{
			BasicView view = (BasicView) iter.next();
			float x = (float)(view.getX() + horizontalDistance / getViewContext().getScale().x);
			float y = (float)(view.getY() + verticalDistance / getViewContext().getScale().y);
			if (!view.hasFixedX() && !view.hasFixedY())
				view.setLocation(new Point2D.Float(x, y));
			else
			{
			if (!view.hasFixedX())
				view.setX(x);
			if (!view.hasFixedY())
				view.setY(y);
			}
			view.stopHandleManipulation(modifiers);
		}
	}

	/**
	 * 
	 * The dropTarget variable contains a potential drop target therefor we must
	 * check if it accept drop
	 */
	private void dropAction(int horizontalDistance, int verticalDistance, ActionModifiers modifiers, ViewPanel dragSource)
	{
		if (dropTarget.acceptDrop(transferableViews)) 
		{
			try
			{
				updateLocation(horizontalDistance, verticalDistance, modifiers);
			}
			catch (RuntimeException e)
			{
				System.out.println("Unable to perform move action due to: " + e);
				getTransactionManager().abortCurrentTransaction();
				return;
			}
			dropTarget.drop(transferableViews, null, modifiers, getDialogParent());
			getTransactionManager().executeCurrentTransaction();
			ViewPanel dropTargetPanel = getViewPanel();
			updateViewPanel(dropTargetPanel);
			// Drop from another view panel than ours?
			if (dragSource != null && dragSource != dropTargetPanel)
				updateViewPanel(dragSource);
		}
	}

	/**
	 * See drawOutlines comment
	 */
	private void repairOutlineDamage(Point oldTranslation, Graphics2D g)
	{
		if (oldTranslation == null)
			return;
		
		if (USE_MOVE_HANDLE.value)
		{
			for (Iterator iter = transferableViews.iterator(); iter.hasNext();)
			{
				View view = (View)iter.next();
				getViewPanel().repaint(view.getDirtyRegion().getBounds());				
			}
			return;
		}
			
		JComponent component = getViewPanel();
		AffineTransform oldAt = AffineTransform.getTranslateInstance(oldTranslation.x, oldTranslation.y);
		for (Iterator iter = transferableViews.iterator(); iter.hasNext();) 
		{
			View view = (View)iter.next();
			Shape shape = view.getDirtyRegion();
			Shape oldShape = oldAt.createTransformedShape(shape);
			component.paintImmediately(oldShape.getBounds());					
		}
	}

	/**
	 * Draw the view or the views that are moved during the drag and drop
	 * operation. To avoid updating the geometry of the views the movement
	 * of the views is faked by translating the graphics that is used for
	 * painting. <br>
	 * There are two ways of drawing the outlines.
	 * <ul>
	 * <li>When XOR painting i used the shapes of the views are stroked with
	 * a pixel wide stroke. The damage repair is done by painting twice at each location.</li>
	 * <li>When AphaComposite i used the actual views are painted with the normal
	 * paint-method. The damage repair is done by repainting the damaged region. When
	 * calculating the damaged region we have to consider the drag&drop translation.</li>
	 * </ul>
	 */
	private void drawOutlines(Point newDragPoint, Graphics2D g2d)
	{
		if (USE_MOVE_HANDLE.value)
		{
			int ddx = newDragPoint.x;
			int ddy = newDragPoint.y;
			if (lastDragPoint != null)
			{
				ddx = ddx - lastDragPoint.x;
				ddy = ddy - lastDragPoint.y;
			}
				
			for (Iterator iter = transferableViews.iterator(); iter.hasNext();)
			{
				TransferableView view = (TransferableView)iter.next();
				view.translate(ddx, ddy);
				getViewPanel().repaint(view.getDirtyRegion().getBounds());				
			}
			return;
		}
		
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
		g2d.translate(newDragPoint.getX(), newDragPoint.getY());
		for (Iterator iter = transferableViews.iterator(); iter.hasNext();) 
		{
			BasicView view = (BasicView)iter.next();
			AffineTransform at = g2d.getTransform();
			at.concatenate(view.getTransform());
			Graphics2DWrapper g2Dw = new Graphics2DWrapper(g2d);
			g2Dw.setPaintableHints(PaintableHints.PAINT_HIDDEN);
			AffineTransform savedAt = g2d.getTransform();
			g2d.setTransform(at);
			view.setProposedX((float)(view.getX() + tddx / getViewContext().getScale().x));
			view.paint(g2Dw);
			g2d.setTransform(savedAt);
		}
	}


	private void updateViewPanel(ViewPanel viewPanel)
	{
//		System.out.println("DragAndDropTool.updateViewPanel " + viewPanel.getName());

		if (!getTransactionManager().isDataChangeNotified())
		{
			viewPanel.invalidate();
			viewPanel.validate();
		}
		viewPanel.repaint();		// Paint the unconfirmed changes state
	}

	public List getTransferableViews()
	{
		return transferableViews;
	}

	/**
	 * Handle the case where a drag has started in an other view panel and released in the
	 * view panel this tool belongs to
	 */
	public void dropReleased(MouseEvent event, List transferableViews, ViewPanel dragSource)
	{
		mouseReleased(event, dragSource);
	}
	
	public void dropExited(MouseEvent event)
	{
		exitTool(event);		
	}
	
	public void abort()
	{
		super.abort();
		stopHandleManipulation(null);
	}
}
