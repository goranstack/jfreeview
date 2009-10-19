package se.bluebrim.view.tool;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.*;

import se.bluebrim.view.*;
import se.bluebrim.view.zoom.*;


/**
 * The user has done an area selection with the selection tool.
 * 
 * @author G Stack
 */
public class RubberBandGesture extends ToolGesture
{
	private Rectangle2D rubberBandRect;
	private List hittedViews;

	public RubberBandGesture(MouseEvent mouseEvent, Rectangle rubberBandRect)
	{
		super(mouseEvent);
		this.rubberBandRect = new Rectangle2D.Double(rubberBandRect.x, rubberBandRect.y, rubberBandRect.width, rubberBandRect.height);
		hittedViews = new ArrayList();
	}

	public void hitView(Hittable view)
	{
		if (isHitted(view))
		{
			view.rubberBandGesture(this);
			hittedViews.add(view);
		}
	}

	/**
	 * When a RubberBandGesture is sent down a view heirarchy as a visitor the rubber band rectangle
	 * is scaled to the current scale for easy hit testing.
	 */
	public void scaleGestureGeometry(Scale scale)
	{
		rubberBandRect.setFrame(
				rubberBandRect.getX() * scale.x, 
				rubberBandRect.getY() * scale.y, 
				rubberBandRect.getWidth() * scale.x, 
				rubberBandRect.getHeight() * scale.y);		
	}

	/**
	 * When a RubberBandGesture is sent down a view heirarchy as a visitor the rubber band rectangle
	 * is translated to the local coordinate system of each view for easy hit testing.
	 */	
	public void translateGestureGeometry(float x, float y)
	{
		rubberBandRect.setRect(rubberBandRect.getX() + x, rubberBandRect.getY() + y, rubberBandRect.getWidth(), rubberBandRect.getHeight());

	}

	protected boolean isHitted(View view)
	{
		return !consumed && view.getOpticalShape().intersects(rubberBandRect);
	}

	public List getHittedViews()
	{
		return hittedViews;
	}

}