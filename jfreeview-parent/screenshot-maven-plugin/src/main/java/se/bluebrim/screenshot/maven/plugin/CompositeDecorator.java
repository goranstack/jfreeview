package se.bluebrim.screenshot.maven.plugin;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * Enables more than one screenshot decorator per Swing component
 * 
 * @author Goran Stack
 *
 */
public class CompositeDecorator implements ScreenshotDecorator 
{
	private List<ScreenshotDecorator> decorators;
	
	public CompositeDecorator()
	{
		this(new ScreenshotDecorator[]{});		
	}
	
	public CompositeDecorator(ScreenshotDecorator decorator) 
	{
		this(new ScreenshotDecorator[]{decorator});
	}
	
	public CompositeDecorator(ScreenshotDecorator[] decorators)
	{
		this.decorators = new ArrayList<ScreenshotDecorator>();
		for (ScreenshotDecorator decorator : decorators)
			this.decorators.add(decorator);
	}
	
	@Override
	public void paint(Graphics2D g2d, JComponent component, JComponent rootComponent) 
	{
		for (ScreenshotDecorator screenshotDecorator : decorators) 
		{
			screenshotDecorator.paint(g2d, component, rootComponent);
		}
	}

	public void add(ScreenshotDecorator decorator) 
	{
		decorators.add(decorator);		
	}

}
