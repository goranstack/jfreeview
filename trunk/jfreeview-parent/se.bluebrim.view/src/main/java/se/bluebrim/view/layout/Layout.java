package se.bluebrim.view.layout;

import java.awt.geom.Dimension2D;

import se.bluebrim.view.*;

/**
 * Interface implemented by objects that can arrange the children views in the
 * specified container by setting their size and location.
 * 
 * @author GStack
 */
public interface Layout
{
	public abstract Dimension2D getMinimumLayoutSize(ParentView container);

	public abstract Dimension2D getPreferredLayoutSize(ParentView container);

	public abstract Dimension2D getMaximumLayoutSize(ParentView container);

	public abstract void layoutViews(ParentView container);

}
