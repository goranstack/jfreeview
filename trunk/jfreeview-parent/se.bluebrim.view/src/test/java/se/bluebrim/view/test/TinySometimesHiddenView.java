package se.bluebrim.view.test;

import java.awt.*;

import se.bluebrim.view.*;
import se.bluebrim.view.impl.*;
import se.bluebrim.view.paint.Paintable;
import se.bluebrim.view.tool.DoubleClickGesture;

public class TinySometimesHiddenView extends BasicView implements Hittable
{
	public static final Color STATE_TRUE_COLOR = Color.YELLOW;
	public static final Color STATE_FALSE_COLOR = Color.BLUE;
	
	private static int MIN_HEIGHT = 10;
	private boolean state;
	
	public TinySometimesHiddenView(ViewContext viewContext)
	{
		super(viewContext);
	}
	
	protected void paintLayer(Paintable g)
	{
		if (isVisible())
		{
			g.setStroke(new BasicStroke(0));
			g.setColor(state ? STATE_TRUE_COLOR : STATE_FALSE_COLOR);
			g.fill(getBounds());
		}
	}
	
	public boolean isVisible()
	{
		return getHeight() * viewContext.getDetailLevel().y > MIN_HEIGHT;
	}

	
	public void doubleClickGesture(DoubleClickGesture doubleClickGesture)
	{
		if (isVisible())
		{
			state = !state;
			doubleClickGesture.consumed();
			doubleClickGesture.getComponent().repaint(getDirtyRegion().getBounds());
		}
	}

	public boolean getState()
	{
		return state;
	}
	
	public Cursor getCursor()
	{
		return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	}

	

}
