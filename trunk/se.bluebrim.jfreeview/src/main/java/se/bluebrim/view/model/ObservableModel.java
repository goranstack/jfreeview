package se.bluebrim.view.model;

import java.beans.*;
import java.beans.PropertyChangeSupport;

/**
 * Subclasses represent model objects in the model-view paradigm we are using
 * in allocation graph design. A view that wants to be notified of changes in its model
 * register as property listener at the model. The view will get a callback each time
 * a property of interest is changed. <br>
 * This kind of object is often called beans but I think we should avoid that kind of nonsense name. 
 * 
 * @author G Stack
 */
public abstract class ObservableModel implements PropertyChangeNotifier
{
	public static final String VIEWS_OUT_OF_SYNC = "outOfSync";
	public static final String HAS_UNCONFIRMED_CHANGES = "hasUnconfirmedChanges";
		
	private PropertyChangeSupport propertyChangeSupport;
	private boolean hasUnconfirmedChanges;		// Updates are sent to server but we have no response from server yet
	private boolean silent;
	
	public ObservableModel()
	{
		propertyChangeSupport = new PropertyChangeSupport(this);
		hasUnconfirmedChanges = false;
		silent = false;
	}
	
	public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) 
	{
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener); 
	}

	public synchronized void removePropertyChangeListener(PropertyChangeListener listener)
	{
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
	public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}
	
   public void firePropertyChange(String propertyName, Object oldValue, Object newValue) 
   {
   	if (!silent)
   		propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
   }
	
   public void firePropertyChange(String propertyName) 
   {
   	if (!silent)
   		propertyChangeSupport.firePropertyChange(propertyName, null, null);
   }

   public boolean hasUnconfirmedChanges()
	{
		return hasUnconfirmedChanges;
	}

	public void setHasUnconfirmedChanges(boolean hasUnconfirmedChanges)
	{
		boolean oldValue = this.hasUnconfirmedChanges;
		this.hasUnconfirmedChanges = hasUnconfirmedChanges;
		propertyChangeSupport.firePropertyChange(HAS_UNCONFIRMED_CHANGES, oldValue, hasUnconfirmedChanges);
	}

	/**
	 * Used when views is out of sync and must be synchronized to show the correct state.
	 * This can for example happen when a transaction is aborted.
	 *
	 * @deprecated Can't handle structural changes of views for example
	 * parent switching in drag and drop operations. <br>
	 * Use addTransactionAbortedListener instead to rebuild the entire
	 * view structure when needed.
	 *
	 */
	public void notifiyViewsOutOfSync()
	{
		propertyChangeSupport.firePropertyChange(VIEWS_OUT_OF_SYNC, null, null);
	}
	
	/*
	 * For debugging purposes
	 */
	public int getNoOfListeners()
	{
		return propertyChangeSupport.getPropertyChangeListeners().length;
	}

	public void setSilent(boolean silent)
	{
		this.silent = silent;
	}


}
