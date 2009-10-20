package se.bluebrim.desktop.graphical;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.net.URL;

import se.bluebrim.view.ConnectableView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.paint.Paintable;

/**
 * Abstract super class to views that scales and renders an image
 * @author GStack
 *
 */
public abstract class AbstractImageView extends DefaultPropertyPersistableView implements ConnectableView
{
	private float originalWidth;
	private float originalHeight;

	public AbstractImageView(ViewContext viewContext, URL url, String propertyKey)
	{
		super(viewContext, propertyKey);
		Rectangle2D originalBounds = loadImage(url);
		originalWidth = (float) originalBounds.getWidth();
		originalHeight = (float) originalBounds.getHeight();
		setWidth(originalWidth);
		createResizeHandles();
	}
	
	protected abstract Rectangle2D loadImage(URL url);

	@Override
	protected void paintLayer(Paintable g)
	{		
		float scaleX = getWidth() / (originalWidth + getSelectedFrameThickness());
		float scaleY = getHeight() / (originalHeight + getSelectedFrameThickness());
		float scale = Math.min(scaleX, scaleY);
		AffineTransform tx = g.getGraphics().getTransform();
		g.translate(getX() + getSelectedFrameThickness()/2, getY() + getSelectedFrameThickness()/2);
		g.scale(scale, scale);
		paintImage(g);
		g.getGraphics().setTransform(tx);		
		showSelected(g);
	}

	protected abstract void paintImage(Paintable g);
	
	@Override
	public void setWidth(float width)
	{
		if (width < 1)
			return;
		super.setWidth(width);
		setSilentHeight(width * originalHeight / originalWidth);
	}

	@Override
	public void setHeight(float height)
	{
		if (height < 1)
			return;
		super.setHeight(height);
		setSilentWidth(height * originalWidth / originalHeight);
	}
	
	@Override
	/**
	 * The contour of the image would be nicer of course
	 */
	public Shape getConnectionShape()
	{
		return getBounds();
	}

}