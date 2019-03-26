package se.bluebrim.view.paintcache;

import java.awt.*;

public class SetFontOp extends GraphicsOperation
{
	private Font font;

	public SetFontOp(Font font)
	{
		this.font = font;
	}

	public void renderOn(Graphics2D g2d)
	{
		g2d.setFont(font);
		
	}

}
