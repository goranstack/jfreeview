package se.bluebrim.view.model;

import java.beans.PropertyChangeListener;

/**
 * 
 * @author GStack
 *
 */
public interface PropertyChangeNotifier
{
	public abstract void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

}