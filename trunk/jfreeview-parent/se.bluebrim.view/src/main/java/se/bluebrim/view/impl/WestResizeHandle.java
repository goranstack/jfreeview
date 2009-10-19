package se.bluebrim.view.impl;

import java.awt.Cursor;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class WestResizeHandle extends HorizontalResizeHandle
{

	public WestResizeHandle(AbstractView master)
	{
		super(master);
	}

	public Cursor getCursor()
	{
		return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
	}

	public void moveTo(double x, double y)
	{
		moveWestSideTo((float)x);
	}

	public float getX()
	{
		return targetView.getX() - RESIZE_HANDLE_WIDTH/2;
	}

	public float getY()
	{
		return targetView.getY();
	}

	protected String getFeedbackString()
	{
		return horizontalResizeFeedbackProvider.getWestSideFeedback();
	}

	/**
	 * Anchor the feedback rectangle in lower right corner to uper left corner of the target view.
	 * Add a small gap of 2 pixels.
	 */
	protected Rectangle2D getFeedBackBounds(String text)
	{
		Rectangle2D bounds =  super.getFeedBackBounds(text);
		bounds.setFrame(bounds.getX() - bounds.getWidth() - 2, bounds.getY(), bounds.getWidth(), bounds.getHeight());
		return bounds;
	}
	
	public List getFeedbackViews()
	{
		return master.getWestResizeFeedbackViews();
	}		



}
