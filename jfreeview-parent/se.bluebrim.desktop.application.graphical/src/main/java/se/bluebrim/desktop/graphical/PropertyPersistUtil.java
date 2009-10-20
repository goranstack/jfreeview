package se.bluebrim.desktop.graphical;

import java.awt.geom.Point2D;
import java.util.Properties;

import se.bluebrim.view.impl.BasicView;

/**
 * 
 * @author GStack
 *
 */
public class PropertyPersistUtil
{
	public static void writeBounds(Properties properties, BasicView view, String key)
	{
		properties.setProperty(key, 
				Float.toString(view.getX()) + ";" + 
				Float.toString(view.getY()) + ";" +
				Float.toString(view.getWidth()) + ";" +
				Float.toString(view.getHeight()));		
	}
	
	public static void readBounds(Properties positions, BasicView view, String key)
	{
		String string = positions.getProperty(key);
		if (string != null)
		{
			String[] parts = string.split(";");
			view.setLocation(new Point2D.Float(Float.valueOf(parts[0]), Float.valueOf(parts[1])));
			view.setWidth(Float.valueOf(parts[2]));
			view.setHeight(Float.valueOf(parts[3]));
		}
	}

	public static void writeLocation(Properties properties, BasicView view, String key)
	{
		properties.setProperty(key, 
				Float.toString(view.getX()) + ";" + 
				Float.toString(view.getY()));		
	}
	
	public static void readLocation(Properties positions, BasicView view, String key)
	{
		String string = positions.getProperty(key);
		if (string != null)
		{
			String[] parts = string.split(";");
			view.setLocation(new Point2D.Float(Float.valueOf(parts[0]), Float.valueOf(parts[1])));
		}
	}
	

}
