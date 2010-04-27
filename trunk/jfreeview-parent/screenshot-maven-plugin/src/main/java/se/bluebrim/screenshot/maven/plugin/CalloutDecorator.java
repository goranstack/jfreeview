package se.bluebrim.screenshot.maven.plugin;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Draws a a filled circle with a number: 
 * </p> <img src="doc-files/CalloutDecorator.png" /> </p>
 * 
 * @author Goran Stack
 * 
 */
public class CalloutDecorator implements ScreenshotDecorator {

	private String number;
	private Point2D position;
	private Point offset;
	private int diameter;
	private Font font;
	private int borderThickness;
	
	
	public CalloutDecorator(int number) 
	{
		this(number, 34, new Point(0, 0));
	}

	public CalloutDecorator(int number, Point offset) 
	{
		this(number, 34, offset);
	}
	
	public CalloutDecorator(int number, int diameter, Point offset) 
	{
		super();
		this.number = number + "";
		this.offset = offset;
		this.diameter = Math.max(diameter, 12);
		borderThickness = Math.max((int) Math.round(diameter * 0.1), 1);		
		font = new Font("SansSerif", Font.PLAIN, (int) (diameter * 0.7));
	}


	@Override
	public void paint(Graphics2D g2d, JComponent component, JComponent rootComponent) 
	{
		Rectangle bounds = SwingUtilities.convertRectangle(component.getParent(), component.getBounds(), rootComponent);
		position = new Point2D.Double(bounds.getX() + offset.x, bounds.getY() + offset.y);
		g2d.translate(position.getX(), position.getY());
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
	}
	
	private void drawCenteredText(Graphics2D g2d, String text)
	{		
		float ascent = getAscent(g2d, text);
		Rectangle2D bounds = getTextBounds(g2d, text);
		float radie = (float) (diameter/2.0);
		float tx = (float)(-bounds.getWidth()/2.0);
		float ty = (float)(ascent - bounds.getHeight()/2.0);
		g2d.drawString(text, tx + radie, ty + radie);
	}
	
	private float getAscent(Graphics2D g, String text)
	{
		return g.getFont().getLineMetrics(text, g.getFontRenderContext()).getAscent();
	}


	private Rectangle2D getTextBounds(Graphics2D g, String text)
	{
		FontMetrics fontMetrics = g.getFontMetrics();
		Rectangle2D bounds = fontMetrics.getStringBounds(text, g);
		return bounds;
	}

}
