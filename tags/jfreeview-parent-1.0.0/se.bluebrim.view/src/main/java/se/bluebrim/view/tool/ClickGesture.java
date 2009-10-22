package se.bluebrim.view.tool;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import se.bluebrim.view.View;
import se.bluebrim.view.zoom.Scale;

public abstract class ClickGesture extends ToolGesture
{
	private Point2D clickPoint;

	
	public ClickGesture(MouseEvent mouseEvent)
	{
		super(mouseEvent);
		clickPoint = new Point2D.Double(mouseEvent.getPoint().x, mouseEvent.getPoint().y);
	}
	
	/**
	 * When a ClickGesture is sent down a view heirarchy as a visitor the click point
	 * is translated to the local coordinate system of each view for easy hit testing.
	 */
	public void scaleGestureGeometry(Scale scale)
	{
		clickPoint.setLocation(clickPoint.getX() * scale.x, clickPoint.getY() * scale.y);
		
	}

	/**
	 * When a ClickGesture is sent down a view heirarchy as a visitor the click point
	 * is scaled to the current scale for easy hit testing.
	 */
	public void translateGestureGeometry(float x, float y)
	{
//		System.out.println(getClass().getName() + " x:" + x + ", y:" + y);
		clickPoint.setLocation(clickPoint.getX() + x, clickPoint.getY() + y);		
	}
	
	protected boolean isHitted(View view)
	{
		return !consumed && view.getBounds().contains(clickPoint.getX(), clickPoint.getY()); 
	}

}