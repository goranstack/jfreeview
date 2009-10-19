package se.bluebrim.isac.desktop;

import java.util.Properties;

/**
 * Implemented by objects that can save and load properties from a Properties object
 *
 * @author GStack
 * 
 */
public interface PropertyPersistableView
{
	public abstract void writeProperties(Properties properties);
	public abstract void readProperties(Properties properties);
}