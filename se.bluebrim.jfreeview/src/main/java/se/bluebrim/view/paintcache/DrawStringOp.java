package se.bluebrim.view.paintcache;

import java.awt.*;

public class DrawStringOp extends GraphicsOperation
{
	private String s;
	private float x;
	private float y;
	
	public DrawStringOp(String s, float x, float y)
	{
		this.s = s;
		this.x = x;
		this.y = y;
	}

	public void renderOn(Graphics2D g2d)
	{
		g2d.drawString(s, x, y);
	}
		
	
}
