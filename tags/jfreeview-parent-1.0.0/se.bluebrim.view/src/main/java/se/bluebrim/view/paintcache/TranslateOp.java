package se.bluebrim.view.paintcache;

import java.awt.*;

public class TranslateOp extends GraphicsOperation
{
	private double tx;
	private double ty;

	public TranslateOp(double tx, double ty)
	{
		this.tx = tx;
		this.ty = ty;
	}

	public void renderOn(Graphics2D g2d)
	{
		g2d.translate(tx, ty);
		
	}

}
