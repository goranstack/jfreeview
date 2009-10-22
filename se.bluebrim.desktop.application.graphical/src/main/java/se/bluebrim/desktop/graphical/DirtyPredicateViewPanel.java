package se.bluebrim.desktop.graphical;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import nu.esox.util.Predicate;
import se.bluebrim.crud.client.DirtyPredicateProvider;
import se.bluebrim.view.impl.AbstractParentView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.model.ObservableModel;
import se.bluebrim.view.swing.ViewPanel;

/**
 * 
 * @author GStack
 *
 */
public class DirtyPredicateViewPanel extends ViewPanel implements DirtyPredicateProvider
{
	private Predicate dirtyPredicate;

	public DirtyPredicateViewPanel(AbstractParentView rootView, ViewContext viewContext)
	{
		super(rootView, viewContext);
		dirtyPredicate = new Predicate(false);
		PropertyChangeListener propertyChangeListener = new PropertyChangeListener(){

			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				dirtyPredicate.set(true);			
			}};
		viewContext.addPropertyChangeListener(ObservableModel.HAS_UNCONFIRMED_CHANGES, propertyChangeListener);
		viewContext.addPropertyChangeListener(ViewContext.DIRTY, propertyChangeListener);
	}

	@Override
	public void cleanDirty()
	{
		dirtyPredicate.set(false);		
	}

	@Override
	public Predicate getDirtyPredicate()
	{
		return dirtyPredicate;
	}

	@Override
	public void initDirtyPredicatePanel()
	{
		// TODO Auto-generated method stub		
	}
	
	public void setDirty()
	{
		dirtyPredicate.set(true);
	}

	@Override
	public boolean isDirty()
	{
		return dirtyPredicate.isTrue();
	}

	@Override
	public void updateModelWithOnGoingTextEditing()
	{
		// TODO Auto-generated method stub
		
	}
	
	
}
