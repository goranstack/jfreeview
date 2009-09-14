package se.bluebrim.view.impl;

import java.awt.Cursor;
import java.util.List;

public class SouthResizeHandle extends VerticalResizeHandle
{
	public SouthResizeHandle(AbstractView master)
	{
		super(master);
	}

	public Cursor getCursor()
	{
		return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
	}

	public void moveTo(double x, double y)
	{
		moveSouthSideTo((float)y);			
	}

	public float getY()
	{
		return targetView.getBottomBound() - RESIZE_HANDLE_HEIGHT/2;
	}

	protected String getFeedbackString()
	{
		return verticalResizeFeedbackProvider.getSouthSideFeedback();
	}

	public List getFeedbackViews()
	{
		return master.getSouthResizeFeedbackViews();
	}		


}
