package se.bluebrim.view.paintcache;

import java.awt.*;

public class FillShapeOp extends GraphicsOperation
{
	private Shape s;

	public FillShapeOp(Shape s)
	{
		this.s = s;
	}

	public void renderOn(Graphics2D g2d)
	{
		g2d.fill(s);
		
	}

}
