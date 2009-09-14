package se.bluebrim.view.impl;

import java.awt.geom.AffineTransform;

import se.bluebrim.view.*;
import se.bluebrim.view.paint.Paintable;

/**
 * Abstract superclass to views that is geometrical connected to a master view.
 * The geometry of a slave view is a function of the master view.
 * 
 * @author G Stack
 *
 */
public abstract class SlaveView extends AbstractView
{
	protected AbstractView master;
	
	public SlaveView(AbstractView master)
	{
		this.master = master;
	}
	
	public float getX()
	{
		return master.getX();
	}

	public float getY()
	{
		return master.getY();
	}

	public float getWidth()
	{
		return master.getWidth();
	}

	public float getHeight()
	{
		return master.getHeight();
	}

	public AffineTransform getTransform()
	{
		return master.getTransform();
	}

	public void paint(Paintable g)
	{

	}
	
	public void addToLayer(ViewLayer layer)
	{
		throw new UnsupportedOperationException("Add to layer through master instead");
	}
		
	public void removeFromLayer(ViewLayer layer)
	{
		throw new UnsupportedOperationException("remove from layer through master instead");
	}

	public boolean belongsToLayer(ViewLayer layer)
	{
		return master.belongsToLayer(layer);
	}

	public ParentView getParent()
	{
		return master.getParent();
	}
	
	public ViewContext getViewContext()
	{
		return master.getViewContext();
	}

}
