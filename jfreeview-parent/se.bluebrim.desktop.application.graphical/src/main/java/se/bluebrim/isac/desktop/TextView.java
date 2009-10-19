package se.bluebrim.isac.desktop;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import se.bluebrim.view.geom.DoubleInsets;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.paint.Paintable;

/**
 * 
 * @author GStack
 *
 */
public class TextView extends DefaultPropertyPersistableView
{	
	private String text;
	private Font font;
	private Color color;
	
	public TextView(ViewContext viewContext, String propertyKey, String text, Font font, Color color)
	{
		super(viewContext, propertyKey);
		this.text = text;
		this.font = font;
		this.color = color;
		setWidth(100);
		setHeight(100);
		createResizeHandles();
	}

	@Override
	public Object getModel()
	{
		return text;
	}
	
	@Override
	protected void paintLayer(Paintable g)
	{
		if (text.toLowerCase().startsWith("<html>"))
		{
			JLabel component = new JLabel();
			component.setForeground(color);
			component.setFont(font);
			View htmlView = BasicHTML.createHTMLView(component, text);
			float frameThickness = getSelectedFrameThickness();
			drawTextView(g, htmlView, new DoubleInsets(frameThickness, frameThickness, frameThickness, frameThickness));
		} else
			drawTextLayout(g, getBounds(), text, color, font);
		showSelected(g);
	}

}
