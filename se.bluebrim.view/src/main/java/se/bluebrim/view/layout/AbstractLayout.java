package se.bluebrim.view.layout;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;

import se.bluebrim.view.ParentView;

/**
 * Abstract super class to the layout managers in the view framework
 * 
 * @author GStack
 *
 */
public abstract class AbstractLayout implements Layout
{

	public Dimension2D getMaximumLayoutSize(ParentView container)
	{
		Dimension2D dim = new Dimension();
		dim.setSize(Double.MAX_VALUE, Double.MAX_VALUE);
		return dim;
	}

	public abstract Dimension2D getMinimumLayoutSize(ParentView container);

	public Dimension2D getPreferredLayoutSize(ParentView container)
	{
		return getMinimumLayoutSize(container);
	}

	public void layoutViews(ParentView container)
	{
		// TODO Auto-generated method stub

	}

}
