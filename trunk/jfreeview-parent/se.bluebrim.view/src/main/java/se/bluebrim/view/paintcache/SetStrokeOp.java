package se.bluebrim.view.paintcache;

import java.awt.*;

public class SetStrokeOp extends GraphicsOperation
{
	private Stroke s;

	public SetStrokeOp(Stroke s)
	{
		this.s = s;
	}

	public void renderOn(Graphics2D g2d)
	{
		g2d.setStroke(s);
	}

}
