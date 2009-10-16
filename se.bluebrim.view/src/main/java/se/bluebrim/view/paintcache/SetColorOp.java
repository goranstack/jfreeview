package se.bluebrim.view.paintcache;

import java.awt.*;

public class SetColorOp extends GraphicsOperation
{
	private Color c;

	public SetColorOp(Color c)
	{
		this.c = c;
	}

	public void renderOn(Graphics2D g2d)
	{
		g2d.setColor(c);
		
	}

}
