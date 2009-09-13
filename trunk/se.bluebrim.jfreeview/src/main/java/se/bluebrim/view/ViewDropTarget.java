package se.bluebrim.view;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;

import se.bluebrim.view.impl.ActionModifiers;

/**
 * Implemented by views that acts as drop target for drag & drop with the <code>DragAndDropTool</code>.
 * 
 * @author G Stack
 */
public interface ViewDropTarget extends View
{
	public boolean isDropTarget();
	public boolean acceptDrop(TransferableView transferable);
	public boolean acceptDrop(List transferables);
	public void drop(TransferableView transferable, Point location, ActionModifiers modifiers, Component dialogParent);
	public void drop(List transferables, Point location, ActionModifiers modifiers, Component dialogParent);
	public void setHighLighted(boolean highLighted);
	public boolean isHighLighted();
	public boolean isChildren(List transferables);
	public boolean isChild(TransferableView transferable);
	/**
	 * Overridden by rop targets that has an aspect that should be reflected in 
	 * the transferabe. For example when a process view is dragged in the
	 * allocation graph the length of the process view should reflect the capacity
	 * of the drop target resource
	 * @param transferable TODO
	 */
	public void reflectInTransferable(TransferableView transferable);

	/**
	 * Return a point that specify the distance that the rectangle should move
	 * to align to one of the edges of the ViewDropTarget. The distance is only
	 * returned when the rectangle is near a edge in other cases a 0,0 point is returned.
	 */
	public Point2D snapToEdge(Rectangle2D rect);

}