package se.bluebrim.view.example;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.util.Iterator;

import se.bluebrim.view.Layoutable;
import se.bluebrim.view.ParentView;
import se.bluebrim.view.layout.AbstractLayout;

/**
 * Set the size to fraction of the container and put them in
 * a row at the bottom.
 * 
 * @author GStack
 *
 */
public class TinyViewLayout extends AbstractLayout
{
	private static float gap = 1;

	public void layoutViews(ParentView container)
	{
		float x = gap;
		for (Iterator iter = container.getChildren().iterator(); iter.hasNext();) 
		{
			Layoutable view = (Layoutable) iter.next();
			view.setHeight((float)(container.getHeight() * 0.2));
			view.setWidth(view.getHeight());
			view.setX(x);
			view.setY(container.getHeight() - view.getHeight() - gap);
			x = x + view.getWidth() + gap;						
		}
	}
	
	@Override
	public Dimension2D getMinimumLayoutSize(ParentView container)
	{
		Dimension2D dim = new Dimension();
		dim.setSize(container.getHeight() * 0.2 + ((container.getChildren().size() - 1) * gap), (container.getHeight() * 0.2) + gap);
		return dim;
	}


}
