package se.bluebrim.view.select;

import se.bluebrim.view.ParentView;

/**
 * Abstract superclass to containing common aspects of various selection managers
 * 
 * @author G Stack
 *
 */
public abstract class AbstractSelectionManager implements SelectionManager, Cloneable
{
	protected ParentView rootView;
	protected SelectableLookup selectableLookup;

	public AbstractSelectionManager(SelectableLookup selectableLookup)
	{
		this.selectableLookup = selectableLookup;
	}
	
	public void setRootView(ParentView rootView)
	{
		this.rootView = rootView;
	}

	public SelectionManager getCopy()
	{
		AbstractSelectionManager copy;
		try
		{
			copy = (AbstractSelectionManager)clone();
			rootView = null;
		}
		catch (CloneNotSupportedException e)
		{
			throw new RuntimeException("Clone should be supported", e);
		}
		return copy;
	}
	
	public abstract void deselectAll();
	
}
