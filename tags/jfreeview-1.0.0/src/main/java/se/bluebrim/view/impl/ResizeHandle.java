package se.bluebrim.view.impl;

import java.awt.*;
import java.awt.geom.*;

import se.bluebrim.view.*;
import se.bluebrim.view.paint.Paintable;
import se.bluebrim.view.value.BooleanValue;

/**
 * Abstract superclass to handles that translate mouse movements to manipulation of one of the view sides. 
 * Subclasses use the same semantics as the predefined cursors for example WestResizeHandle
 * 
 * @author Görans Stäck
 * 
 */
public abstract class ResizeHandle extends AbstractHandle implements ToolTipProvider
{
	public static final BooleanValue PAINT_HANDLES = new BooleanValue(false);		// For debugging
	private static final int TEXT_OFFSET = 2;  // Number of pixels above the handle
	private static final int TEXT_MARGINS = 3;  // Right and left margins
	protected static Font feedBackFont;
	protected ResizeFeedbackProvider resizeFeedbackProvider;
	
	/**
	 * 
	 * @param master must be of type <code>Layoutable</code>
	 */
	public ResizeHandle(AbstractView master)
	{
		super(master);
		if (master instanceof ResizeFeedbackProvider)
		{
			resizeFeedbackProvider = (ResizeFeedbackProvider)master;
			if (feedBackFont == null)
				feedBackFont = master.getViewContext().getDefaultFont().deriveFont(Font.PLAIN, 18);
		}
	}

	/**
	 * @return Cursor suitable for the edge the handle is operating on. For example if the 
	 * handle operates on the left edge of a view the predefined west-resize cursor would be suitable.
	 */
	public abstract Cursor getCursor();


	/**
	 * 
	 * Draws a black rounded rect slightly transparent with white text. This method
	 * is used when moving as well where all four sides can produce a feedback.
	 */
	public void paintFeedback(Paintable g)
	{
		if (resizeFeedbackProvider != null)
		{
			Composite composite = g.getComposite();
			Rectangle2D bounds = getFeedBackBounds(getFeedbackString());
			Shape shape = new RoundRectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 8, 8);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			g.setColor(Color.BLACK);
			g.fill(shape);
			g.setComposite(composite);
			g.setColor(Color.WHITE);
			g.setFont(feedBackFont);
			g.drawString(getFeedbackString(), (float)(bounds.getX() + TEXT_MARGINS), getY() - TEXT_OFFSET);
		}
	}
		
	/**
	 * Moving the north side means changing height and y of the target view
	 */
	protected void moveNorthSideTo(float y)
	{
		float constrainedY = Math.max(getMinY(), y);
		constrainedY = Math.min(getMaxY(), constrainedY);
		float newHeight =  targetView.getY() - constrainedY + targetView.getHeight();	// Up movement increase Height
		newHeight = Math.max(getMinHeight(), newHeight);
		newHeight = Math.min(getMaxHeight(), newHeight);
		targetView.setY(targetView.getBottomBound() - newHeight);
		setTargetHeight(newHeight);
	}
	/**
	 * Moving the west side means changing width and x of the target view
	 */
	protected void moveWestSideTo(float x)
	{
		float constrainedX = Math.max(getMinX(), x);
		constrainedX = Math.min(getMaxX(), constrainedX);
		float newWidth = targetView.getX() - constrainedX + targetView.getWidth();
		newWidth = Math.max(getMinWidth(), newWidth);
		newWidth = Math.min(getMaxWidth(), newWidth);
		targetView.setX(targetView.getRightBound() - newWidth);
		setTargetWidth(newWidth);
	}

	/**
	 * Moving the east side means changing width of the target view
	 */
	protected void moveEastSideTo(float x)
	{
		float newWidth = x - targetView.getX();
		newWidth = Math.max(getMinWidth(), newWidth);
		newWidth = Math.min(getMaxWidth(), newWidth);
		setTargetWidth(newWidth);		
	}

	private void setTargetWidth(float newWidth)
	{
		targetView.setWidth(newWidth);
		targetView.expandParentIfNeeded();
	}

	/**
	 * Moving the south side means changing height of the target view
	 */
	protected void moveSouthSideTo(float y)
	{
		float newHeight = y - targetView.getY();
		newHeight = Math.max(targetView.getMinHeight(), newHeight);
		newHeight = Math.min(targetView.getMaxHeight(), newHeight);
		setTargetHeight(newHeight);
	}

	private void setTargetHeight(float newHeight)
	{
		targetView.setHeight(newHeight);
		targetView.expandParentIfNeeded();
	}

	/**
	 * We don't want any tooltip when mouse is over a handle
	 */
	public String getToolTipText()
	{
		return null;
	}
	
	public Shape getOpticalShape()
	{
		Shape shape = super.getOpticalShape();
		if (resizeFeedbackProvider != null)
			return extendOpticalShapeWidthFeedBack(getFeedbackString(), shape);
		else
			return shape;
	}
	
	protected abstract String getFeedbackString();
	
	private Shape extendOpticalShapeWidthFeedBack(String text, Shape shape) 
	{
		Rectangle2D stringBounds = getFeedBackBounds(text);
		return shape.getBounds2D().createUnion(stringBounds);
	}

	protected Rectangle2D getFeedBackBounds(String text)
	{
		Rectangle2D stringBounds = feedBackFont.getStringBounds(text, ViewContext.DEFAULT_FONT_RENDER_CONTEXT);
		// StringBounds has a negative Y representing the ascent
		stringBounds.setFrame(getX() + getWidth()/2, 
			getY() + stringBounds.getY() - TEXT_OFFSET, 
			stringBounds.getWidth() + TEXT_MARGINS*2, 
			stringBounds.getHeight());
		return stringBounds;
	}

	
}
