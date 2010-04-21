package se.bluebrim.screenshot.maven.plugin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Used to draw a red frame around a component as in this example: </p> <img
 * src="doc-files/RedFrameDecorator.png" /> </p>
 * 
 * @author Goran Stack
 * 
 */
public class RedFrameDecorator implements ScreenshotDecorator {

	@Override
	public void paint(Graphics2D g2d, JComponent component, JComponent rootComponent) 
	{
		g2d.setColor(Color.RED.darker());
		g2d.setStroke(new BasicStroke(2));
		g2d.draw(SwingUtilities.convertRectangle(component.getParent(), component.getBounds(), rootComponent));

	}

}
