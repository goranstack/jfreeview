package se.bluebrim.view.tool;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

public abstract class RubberBandingTool extends MainTool
{
	private int dy;
	private int dx;
	private Rectangle lastRect;


	public RubberBandingTool(ToolDispatcher toolDispatcher, boolean addAsFirstTool)
	{
		super(toolDispatcher, addAsFirstTool);
	}

	public RubberBandingTool(ToolDispatcher toolDispatcher)
	{
		super(toolDispatcher);
	}
	
	public void mousePressed(MouseEvent e)
	{
		super.mousePressed(e);
		dy = 0;
		dx = 0;
	}

	public void mouseDragged(MouseEvent e)
	{
	   JComponent component = ((JComponent)e.getSource());
		Graphics2D g = (Graphics2D)component.getGraphics();

		drawOutline(g, createRectangle(mousePressedPoint, new Dimension(dx, dy)));  // Repair previous damage

		// Autoscroll
		Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
 		component.scrollRectToVisible(r);
		g = (Graphics2D)component.getGraphics();	// Get graphics again in case translation is changed due to autoscroll

		dy = e.getY() - mousePressedPoint.y;
		dx = e.getX() - mousePressedPoint.x;
		lastRect = createRectangle(mousePressedPoint, new Dimension(dx, dy));
		drawOutline(g, lastRect);
	}

	public void mouseReleased(MouseEvent e)
	{
		super.mouseReleased(e);
		if (lastRect != null)
		{
			Graphics2D g = (Graphics2D)((Component)e.getSource()).getGraphics();

			drawOutline(g, lastRect);
			lastRect = null;
		}
		// If the user clicked we get a mouse release without any previous drag 
		rubberBandAction(e, createRectangle(mousePressedPoint, new Dimension(dx, dy)));
	}

	/**
	 * Handles the negative dimension we get when the user drags up to the left
	 * instead of down to the right.
	 */
	private Rectangle createRectangle(Point start, Dimension size)
	{
		int x = start.x;
		int y = start.y;
		
		if (size.width < 0)
		{
			x = x + size.width;
			size.width = -size.width;
		}

		if (size.height < 0)
		{
			y = y + size.height;
			size.height = -size.height;
		}

		return new Rectangle(x, y, size.width, size.height);
	}
	
	protected abstract void rubberBandAction(MouseEvent e, Rectangle rect);


}
