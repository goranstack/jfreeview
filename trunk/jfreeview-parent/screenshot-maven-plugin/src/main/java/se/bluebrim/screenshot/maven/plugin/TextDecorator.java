package se.bluebrim.screenshot.maven.plugin;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

/**
 * 
 * @author Goran Stack
 * 
 */
public class TextDecorator implements ScreenshotDecorator {

	private String text;
	private DecoratorLayout layout;
	private Font font;
	
	
	public TextDecorator(String text) 
	{
		this(text, new Center());
	}

	
	public TextDecorator(String text, DecoratorLayout layout) 
	{
		this.text = text;
		this.layout = layout;
		font = new Font("SansSerif", Font.PLAIN, 12);
	}


	@Override
	public void paint(Graphics2D g2d, JComponent component, JComponent rootComponent) 
	{
		Point2D position = layout.getPosition(component, rootComponent);
		Rectangle2D bounds = getTextBounds(g2d, text);		
		g2d.setColor(Color.BLACK.brighter());
		g2d.setFont(font);
		g2d.drawString(text, (float) (position.getX() - bounds.getCenterX()), (float) (position.getY() - bounds.getCenterY()));
	}
	
	
	private Rectangle2D getTextBounds(Graphics2D g, String text)
	{
		FontMetrics fontMetrics = g.getFontMetrics();
		Rectangle2D bounds = fontMetrics.getStringBounds(text, g);
		return bounds;
	}

}
