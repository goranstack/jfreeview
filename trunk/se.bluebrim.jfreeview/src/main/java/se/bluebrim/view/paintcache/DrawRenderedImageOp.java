package se.bluebrim.view.paintcache;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

public class DrawRenderedImageOp extends GraphicsOperation
{
	private RenderedImage img;
	private AffineTransform xform;

	public DrawRenderedImageOp(RenderedImage img, AffineTransform xform)
	{
		this.img = img;
		this.xform = xform;
	}

	public void renderOn(Graphics2D g2d)
	{
		g2d.drawRenderedImage(img, xform);
		
	}

}
