package se.bluebrim.view.paintcache;

import java.awt.*;

public class DrawShapeOp extends GraphicsOperation
{
	private Shape s;

	public DrawShapeOp(Shape s)
	{
		this.s = s;
	}

	public void renderOn(Graphics2D g2d)
	{
		g2d.draw(s);
		
	}

}
