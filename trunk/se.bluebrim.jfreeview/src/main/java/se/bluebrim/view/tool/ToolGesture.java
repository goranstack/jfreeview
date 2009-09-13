package se.bluebrim.view.tool;

import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import se.bluebrim.view.*;
import se.bluebrim.view.zoom.*;

/**
 * A ToolGesture is created when the user performs a gesture with the SelectionTool. The ToolGesture
 * gather all information about the gesture that is nessecary for views when they
 * should react upon the ToolGesture. The ToolGesture object save us from having separate
 * view hierarchy traverse method for different kind of hits.
 * 
 * @author G Stack
 */
public abstract class ToolGesture
{
	private MouseEvent mouseEvent;
	protected boolean consumed = false;

	public ToolGesture(MouseEvent mouseEvent)
	{
		this.mouseEvent = mouseEvent;
	}
	
	public abstract void hitView(Hittable view);

	/**
	 * When a ToolGesture is sent down a view heirarchy as a visitor the click point
	 * or rubberband rectangle is translated to the local coordinate system of each
	 * view for easy hit testing
	 */
	public abstract void translateGestureGeometry(float x, float y);

	/**
	 * When a ToolGesture is sent down a view heirarchy as a visitor the click point
	 * or rubberband rectangle is scaled to the current scale for easy hit testing.
	 */
	public abstract void scaleGestureGeometry(Scale scale);
	
	protected abstract boolean isHitted(View view);
	
	public MouseEvent getMouseEvent()
	{
		return mouseEvent;
	}
	
	public JComponent getComponent()
	{
		return ( JComponent)mouseEvent.getComponent();
	}

	public void consumed()
	{
		consumed = true;
	}

}
