package se.bluebrim.view.paintcache;

import java.awt.*;
import java.awt.RenderingHints.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

import se.bluebrim.view.paint.Graphics2DWrapper;


public class GraphicsCache extends Graphics2DWrapper
{	
	private List operations;

	public GraphicsCache(Graphics2D g2d)
	{
		super(g2d);
		operations = new ArrayList();
	}
	
	public void drawString(String s, float x, float y)
	{
		operations.add(new DrawStringOp(s, x, y));
	}
	
	public void renderOn(Graphics2D g2d)
	{
		for (Iterator iter = operations.iterator(); iter.hasNext();) 
		{
			((GraphicsOperation)iter.next()).renderOn(g2d);
		}
	}

	public void draw(Shape s)
	{
		operations.add(new DrawShapeOp(s));
	}

	public void drawRenderedImage(RenderedImage img, AffineTransform xform)
	{
		operations.add(new DrawRenderedImageOp(img, xform));
	}

	public void fill(Shape s)
	{
		operations.add(new FillShapeOp(s));
	}

	public void setColor(Color c)
	{
		operations.add(new SetColorOp(c));
	}

	public void setFont(Font font)
	{
		operations.add(new SetFontOp(font));
	}

	public void setRenderingHint(Key hintKey, Object hintValue)
	{
		operations.add(new SetRenderingHintOp(hintKey, hintValue));
	}

	public void setStroke(Stroke s)
	{
		operations.add(new SetStrokeOp(s));
	}

	public void translate(double tx, double ty)
	{
		operations.add(new TranslateOp(tx, ty));
	}

}
