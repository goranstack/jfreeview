package se.bluebrim.view.impl;

import java.awt.Cursor;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class EastResizeHandle extends HorizontalResizeHandle
{
	public EastResizeHandle(AbstractView master)
	{
		super(master);
	}

	public Cursor getCursor()
	{
		return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
	}

	public void moveTo(double x, double y)
	{
		moveEastSideTo((float)x);
	}
	
	public float getX()
	{
		return targetView.getRightBound() - RESIZE_HANDLE_WIDTH/2;
	}

	protected String getFeedbackString()
	{
		return horizontalResizeFeedbackProvider.getEastSideFeedback();
	}
	
	/**
	 * Anchor the feedback rectangle in lower left corner to upper right corner of the target view.
	 * Add a small gap of 2 pixels.
	 */
	protected Rectangle2D getFeedBackBounds(String text)
	{
		Rectangle2D bounds =  super.getFeedBackBounds(text);
		bounds.setFrame(bounds.getX() + 2, bounds.getY(), bounds.getWidth(), bounds.getHeight());
		return bounds;
	}
	
	
	public List getFeedbackViews()
	{
		return master.getEastResizeFeedbackViews();
	}		


}
