package se.bluebrim.view.paintcache;

import java.awt.*;
import java.awt.RenderingHints.*;

public class SetRenderingHintOp extends GraphicsOperation
{
	private Key hintKey;
	private Object hintValue;

	public SetRenderingHintOp(Key hintKey, Object hintValue)
	{
		this.hintKey = hintKey;
		this.hintValue = hintValue;
	}

	public void renderOn(Graphics2D g2d)
	{
		g2d.setRenderingHint(hintKey, hintValue);
		
	}
	

}
