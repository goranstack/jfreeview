package se.bluebrim.view.paint;

import java.awt.*;
import java.awt.RenderingHints.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;

import se.bluebrim.view.ViewLayer;
import se.bluebrim.view.value.BooleanValue;
import se.bluebrim.view.zoom.Scale;


public interface Paintable
{
	public enum Target{ Screen, Printer, BitmapImage, SVG, PDF };
	public static final BooleanValue RANDOM_COLORS = new BooleanValue(false);

	public void drawRenderedImage(RenderedImage img, AffineTransform xform);
	public void drawRenderedImage(RenderedImage img);

	public void setColor(Color c);

	public void setStroke(Stroke s);

	public void draw(Shape s);

	public void setFont(Font font);

	public Font getFont();

	public FontMetrics getFontMetrics();

	public Rectangle2D getStringBounds(String string);

	public void translate(double tx, double ty);

	public void drawString(String s, float x, float y);

	public void fill(Shape s);

	public FontRenderContext getFontRenderContext();

	public void setRenderingHint(Key hintKey, Object hintValue);

	public Graphics2D getGraphics();

	public void setRandomColor();

	public Color getRandomColor();
	
	public void setPaint(Paint fillPaint);

	public void setComposite(Composite comp);

	public Composite getComposite();
	
	public void scale(double sx, double sy);

	public void scale(Scale scale);
	
	public void setToScale(double sx, double sy);
	
	public double getScaleX();

	public Paintable.Target getTarget();

	public void setTarget(Paintable.Target target);
	
	public void clip(Shape shape);

	public Shape getClip();

	public void setClip(Shape shape);

	public Color getColor();

	public ViewLayer getLayer();

	public void setLayer(ViewLayer layer);

	public PaintableHints getPaintableHints();

	public void setPaintableHints(PaintableHints paintableHints);
	
	public Rectangle getClipBounds();

}
