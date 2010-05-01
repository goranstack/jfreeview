package se.bluebrim.screenshot.maven.plugin;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

/**
 * Draws a a filled circle with a number: 
 * </p> <img src="doc-files/CalloutDecorator.png" /> </p>
 * 
 * @author Goran Stack
 * 
 */
public class CalloutDecorator implements ScreenshotDecorator {

	private String number;
	private DecoratorLayout layout;
	private int diameter;
	private Font font;
	private int borderThickness;
	
	
	public CalloutDecorator(int number) 
	{
		this(number, 34, new Center());
	}

	public CalloutDecorator(int number, DecoratorLayout layout) 
	{
		this(number, 34, layout);
	}
	
	public CalloutDecorator(int number, int diameter, DecoratorLayout layout) 
	{
		super();
		this.number = number + "";
		this.layout = layout;
		this.diameter = Math.max(diameter, 12);
		borderThickness = Math.max((int) Math.round(diameter * 0.1), 1);
		float fontSizeFactor = (number > 9) ? 0.55f : 0.65f;
		font = new Font("SansSerif", Font.PLAIN, (int) (diameter * fontSizeFactor));
	}


	@Override
	public void paint(Graphics2D g2d, JComponent component, JComponent rootComponent) 
	{
		AffineTransform at = g2d.getTransform();
		double radie = diameter/2.0;
		Point2D position = layout.getPosition(component, rootComponent);
		g2d.translate(position.getX() - radie, position.getY() - radie);
		Ellipse2D circle = new Ellipse2D.Float(0, 0, diameter, diameter);
		g2d.setColor(Color.WHITE);
		g2d.fill(circle);
		g2d.translate(borderThickness, borderThickness);
		circle.setFrame(0, 0, circle.getWidth() - borderThickness * 2, circle.getHeight() - borderThickness * 2);
		g2d.setColor(Color.BLACK);
		g2d.fill(circle);
		g2d.translate(-borderThickness, -borderThickness);
		g2d.setColor(Color.WHITE);
		g2d.setFont(font);
		drawCenteredText(g2d, number);
		g2d.setTransform(at);
	}
	
	private void drawCenteredText(Graphics2D g2d, String text)
	{		
		Rectangle2D bounds = getTextBounds(g2d, text);
		float radie = (float) (diameter/2.0);
		g2d.drawString(text, (float)(radie - bounds.getCenterX()), (float) (radie - bounds.getCenterY()));
	}
	
	private Rectangle2D getTextBounds(Graphics2D g, String text)
	{
		FontMetrics fontMetrics = g.getFontMetrics();
		Rectangle2D bounds = fontMetrics.getStringBounds(text, g);
		return bounds;
	}

}
