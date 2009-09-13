package se.bluebrim.view.paint;

import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.awt.image.RenderedImage;
import java.util.Random;

import se.bluebrim.view.ViewLayer;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.zoom.Scale;

/**
 * Default implementation of Paintable used for both painting on screen and printing. <br>
 * The bug <a href=http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4696289>Antialiasing and large lines throw sun.dc.pr.PRException: endPath: bad path</a> 
 * in 1.4 forces us to turn off antialiasing when printing but that's probably ok
 * because of the larger resolution of printers.
 *   
 * @author G Stack
 *
 */

public class Graphics2DWrapper implements Paintable
{
	private static final Random random = new Random(System.currentTimeMillis());
	
	private Graphics2D g2d;
	private Paintable.Target target;
	private ViewLayer layer = ViewLayer.DEFAULT_LAYER;
	private PaintableHints paintableHints;
	
	public Graphics2DWrapper(Graphics2D g2d)
	{
		this(g2d, Paintable.Target.Screen);
	}

	public Graphics2DWrapper(Graphics2D g2d, Paintable.Target target)
	{
		this.g2d = g2d;
		this.target = target;
		if (target == Paintable.Target.Printer)
		{
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		} else
		{
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, ViewContext.ANTIALIASING);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, ViewContext.TEXT_ANTIALIASING);
		}
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, ViewContext.FRACTIONAL_METRICS);			
	}

	public Graphics2DWrapper(Graphics2D g2d, Paintable.Target target, ViewContext viewContext)
	{
		this(g2d, target);
		g2d.scale(viewContext.getScale().x, viewContext.getScale().y);
	}

	public Paintable.Target getTarget()
	{
		return target;
	}

	public void setTarget(Paintable.Target target)
	{
		this.target = target;
	}


	public void drawRenderedImage(RenderedImage img, AffineTransform xform)
	{
		g2d.drawRenderedImage(img, xform);		
	}

	public void drawRenderedImage(RenderedImage img)
	{
		g2d.drawRenderedImage(img, new AffineTransform());		
	}

	public void setColor(Color c)
	{
		g2d.setColor(c);		
	}
	
	public Color getColor()
	{
		return g2d.getColor();		
	}

	public void setStroke(Stroke s)
	{
		g2d.setStroke(s);		
	}

	public void draw(Shape s)
	{
		g2d.draw(s);		
	}

	public void setFont(Font font)
	{
		g2d.setFont(font);		
	}

	public Font getFont()
	{
		return g2d.getFont();
	}

	public FontMetrics getFontMetrics()
	{
		return g2d.getFontMetrics();
	}

	public void translate(double tx, double ty)
	{
		g2d.translate(tx, ty);
		
	}

	public void drawString(String s, float x, float y)
	{
		g2d.drawString(s, x, y);		
	}

	public void fill(Shape s)
	{
		g2d.fill(s);		
	}

	public FontRenderContext getFontRenderContext()
	{
		return g2d.getFontRenderContext();
	}

	public void setRenderingHint(Key hintKey, Object hintValue)
	{
		g2d.setRenderingHint(hintKey, hintValue);		
	}

	public Graphics2D getGraphics()
	{
		return g2d;
	}
	
	public Rectangle2D getStringBounds(String string)
	{
		FontMetrics fontMetrics = g2d.getFontMetrics();
		return fontMetrics.getStringBounds(string, g2d);
	}
		
	/**
	 * Useful when verifying repaint. By using random color for filling views 
	 * it's easy to detect the repainted areas since they change color.
	 */
	public void setRandomColor()
	{
		setColor(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat() ));		
	}
	
	public Color getRandomColor()
	{
		return new Color(random.nextFloat(), random.nextFloat(), random.nextFloat() );
	}

	public void setPaint(Paint paint)
	{
		g2d.setPaint(paint);		
	}

	public void setComposite(Composite comp)
	{
		g2d.setComposite(comp);		
	}
	
	public Composite getComposite()
	{
		return g2d.getComposite();		
	}

	public void scale(double sx, double sy)
	{
		g2d.scale(sx, sy);		
	}

	public void setToScale(double sx, double sy)
	{
		g2d.scale(1/g2d.getTransform().getScaleX() * sx, 1/g2d.getTransform().getScaleY() * sy);				
	}

	public void scale(Scale scale)
	{
		scale(scale.x, scale.y);		
	}

	public void clip(Shape shape)
	{
		g2d.clip(shape);		
	}

	public Shape getClip()
	{
		return g2d.getClip();
	}

	public void setClip(Shape shape)
	{
		g2d.setClip(shape);		
	}

	public ViewLayer getLayer()
	{
		return layer;
	}

	public void setLayer(ViewLayer layer)
	{
		this.layer = layer;
	}

	public PaintableHints getPaintableHints()
	{
		if (paintableHints == null)
			return PaintableHints.DEFAULT;
		else
			return paintableHints;
	}

	public void setPaintableHints(PaintableHints paintableHints)
	{
		this.paintableHints = paintableHints;
	}

	public double getScaleX()
	{
		return g2d.getTransform().getScaleX();
	}

	public Rectangle getClipBounds()
	{
		return g2d.getClipBounds();
	}

}
