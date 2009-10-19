package se.bluebrim.view.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import se.bluebrim.view.Handle;
import se.bluebrim.view.ParentView;
import se.bluebrim.view.ResizableContainer;
import se.bluebrim.view.TransferableView;
import se.bluebrim.view.View;
import se.bluebrim.view.geom.FloatInsets;
import se.bluebrim.view.model.ObservableModel;
import se.bluebrim.view.paint.Paintable;



/**
 * Abstract super class to all kind of views. Contains the most basic functionality
 * for a view.
 * 
 * @author G Stack
 *
 */
public abstract class AbstractView extends ObservableModel implements View
{	
	private static final double VERTICAL_LEFT = 3 * Math.PI / 2;

	public abstract float getX();
	public abstract float getY();
	public abstract float getWidth();
	public abstract float getHeight();
	public abstract AffineTransform getTransform();
	public abstract ViewContext getViewContext();

	public float getRightBound()
	{
		return getX() + getWidth();
	}

	public float getBottomBound()
	{
		return getY() + getHeight();
	}


	/**
	 * Return the optical bounds of the view transformed into the
	 * coordinate system of the component that owns the view hierarchy.
	 * Useful as argument to the repaint method when only the view
	 * needs to be repainted. <br>
	 * If you change a state of a view that affects the optical bounds you
	 * must repaint the union of the optical bounds before and after the state change.
	 */
	public Shape getDirtyRegion()
	{
		return getTransform().createTransformedShape(getOpticalShape());
	}

	/**
	 * Return the geometrical bounds of the view transformed into the
	 * coordinate system of the component that owns the view hierarchy.
	 * Useful for XOR drawing when tracking a drag.
	 */
	public Rectangle getBoundsRelativeComponent()
	{
		return getTransform().createTransformedShape(getBounds()).getBounds();		
	}
	
	public Point2D getLocationRelativeComponent()
	{
		Point2D location = getLocation();
		return getTransform().transform(location, location);
	}
	
	public Point2D getLocationInRoot()
	{
		double x = getX();
		double y = getY();
		ParentView container = getParent();
		while(container != null)
		{
			if (container.getParent() != null)
			{
				x = x + container.getX();
				y = y + container.getY();
			}
			container = container.getParent();
		}
		return new Point2D.Double(x, y);		
	}


	public Point2D getLocation()
	{
		return new Point2D.Float(getX(), getY());
	}
		
	public Rectangle2D getBounds()
	{
		return new Rectangle2D.Float(getX(), getY(), getWidth(), getHeight());
	}
	
	public Rectangle2D getInsettedBounds(FloatInsets insets)
	{
		return new Rectangle2D.Float(getX() + insets.left, 
			getY() + insets.top, 
			getWidth() - (insets.left + insets.right), 
			getHeight() - (insets.top + insets.bottom));		
	}
	
	public Point2D getCenter()
	{
		return new Point2D.Double(getX() + getWidth()/2.0, getY() + getHeight()/2.0);
	}

	
	public Shape getOpticalShape()
	{
		return getBounds();
	}
	

	public boolean isDropTarget()
	{
		return false;
	}
	
	public String getToolTipText()
	{
		return null;
	}
	
	public BufferedImage getVisualRepresentation()
	{
		throw new UnsupportedOperationException("Only subclasses to BasicView support this method");
	}
	
	/**
	 * Recursively descend down the view structure in a depth-first manner. That means we visit the 
	 * children of a view before visiting the view itself. Subclasses that have children should override
	 * this method.<br>
	 * Since we have no childern just visit our self.
	 */
	public void traverseDepthFirst(ViewVisitor visitor)
	{
		visit(visitor);
	}

	public void traverseHeightFirst(ViewVisitor visitor)
	{
		visit(visitor);
	}

	
	public void visit(ViewVisitor visitor)
	{
		visitor.visit(this);		
	}
	
	protected void debugPaintGeometry(String text, Graphics2D g)
	{
		AffineTransform transform = g.getTransform();
		Rectangle tBounds = transform.createTransformedShape(getBounds()).getBounds();
		System.out.println(text + " x: " + tBounds.getX()
				+ " y: " + tBounds.getY() + 
				" width: " + tBounds.getWidth() + 
				" height: " + tBounds.getHeight());

	}
	
	public void updateViewFromModel()
	{
	}
	
	/**
	 * The view has been manipulated by a handle and this method is called when
	 * the manipulation terminates. Give the view a change to take some action for
	 * example update its model to reflect the changes applied by the handle.
	 */
	public void stopHandleManipulation(ActionModifiers modifiers)
	{
		
	}

	/**
	 * The view is going to be manipulated by a handle and this method is called when
	 * the manipulation starts. Give the view a change to take some action for
	 * before the manipulation starts.
	 * @param handle TODO
	 */
	public void startHandleManipulation(Handle handle)
	{
		
	}
	
	protected static double getStringWidth(Font font, String text)
	{
		return getStringBounds(font, text).getWidth();
	}
	
	protected static Rectangle2D getStringBounds(Font font, String text)
	{
		return font.getStringBounds(text, ViewContext.DEFAULT_FONT_RENDER_CONTEXT);
	}
	
	protected double getStringHeight(Font font, String text)
	{
		Rectangle2D stringBounds = getStringBounds(font, text);
		// StringBound has negative Y representing the ascent
		return stringBounds.getHeight() - stringBounds.getY();
	}
	
	public float getPreferredHeight()
	{
		throw new UnsupportedOperationException();
	}
	
	public void containerResized(ResizableContainer container)
	{
		
	}

	/**
	 * Overide this method to implenment snapping when dragging
	 */
	public Point2D snapToEdge(Rectangle2D rect)
	{
		return new Point();
	}
	
	public void dispose()
	{
		
	}
	
	protected void drawInsideBottomLine(Paintable g, float width, Color color)
	{
		Line2D line = new Line2D.Float(getX(), getBottomBound()-width/2, getRightBound(), getBottomBound()-width/2);
		drawLine(g, width, color, line);
	}

	protected void drawBottomLine(Paintable g, float width, Color color)
	{
		Line2D line = new Line2D.Float(getX(), getBottomBound(), getRightBound(), getBottomBound());
		drawLine(g, width, color, line);
	}

	protected void drawTopLine(Paintable g, float width, Color color)
	{
		Line2D line = new Line2D.Float(getX(), getY(), getRightBound(), getY());
		drawLine(g, width, color, line);
	}
	
	private void drawLine(Paintable g, float width, Color color, Line2D line)
	{
		g.setStroke(new BasicStroke(width));
		g.setColor(color);
		g.draw(line);
	}
	
	protected Rectangle2D getTextBounds(Paintable g, String text)
	{
		FontMetrics fontMetrics = g.getFontMetrics();
		Rectangle2D bounds = fontMetrics.getStringBounds(text, g.getGraphics());
		return bounds;
	}
	
	protected float getAscent(Paintable g, String text)
	{
		return g.getFont().getLineMetrics(text, g.getFontRenderContext()).getAscent();
	}

	protected void drawCenteredText(Paintable g, String text)
	{
		drawCenteredText(g, text, false);
	}
	
	protected void drawCenteredText(Paintable g, String text, boolean vertically)
	{		
		float ascent = getAscent(g, text);
		Rectangle2D bounds = getTextBounds(g, text);
		Point2D center = getCenter();
		g.translate(center.getX(), center.getY());
		float tx = (float)(-bounds.getWidth()/2.0);
		float ty = (float)(ascent - bounds.getHeight()/2.0);
		if (vertically)
			g.getGraphics().rotate(VERTICAL_LEFT);
		g.drawString(text, tx, ty);
		if (vertically)
			g.getGraphics().rotate(-VERTICAL_LEFT);
		g.translate(-center.getX(), -center.getY());
	}
	
	public Cursor getCursor()
	{
		if (this instanceof TransferableView && !getViewContext().isReadOnly())
			return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		else
			return Cursor.getDefaultCursor();
	}
	
	public void putOnTop()
	{
	}

	public List getMoveFeedbackViews()
	{
		return new ArrayList();
	}
	
	public List getEastResizeFeedbackViews()
	{
		return new ArrayList();
	}

	public List getWestResizeFeedbackViews()
	{
		return new ArrayList();
	}
	
	public List getNorthResizeFeedbackViews()
	{
		return new ArrayList();
	}
	
	public List getSouthResizeFeedbackViews()
	{
		return new ArrayList();
	}

	public String asString()
	{
		return toString();
	}
			
}
