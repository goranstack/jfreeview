package se.bluebrim.view.impl;

import java.awt.Cursor;
import java.util.List;

public class NorthResizeHandle extends VerticalResizeHandle
{
	public NorthResizeHandle(AbstractView master)
	{
		super(master);
	}

	public Cursor getCursor()
	{
		return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
	}

	public void moveTo(double x, double y)
	{
		moveNorthSideTo((float)y);			
	}
	
	public float getY()
	{
		return targetView.getY() - RESIZE_HANDLE_HEIGHT/2;
	}

	protected String getFeedbackString()
	{
		return verticalResizeFeedbackProvider.getNorthSideFeedback();
	}
	
	public List getFeedbackViews()
	{
		return master.getNorthResizeFeedbackViews();
	}		


}
