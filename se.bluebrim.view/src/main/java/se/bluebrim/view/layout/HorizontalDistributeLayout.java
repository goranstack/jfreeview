package se.bluebrim.view.layout;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.util.*;

import se.bluebrim.view.*;

/**
 * Arranges the views horizontal with a small gap between them.
 * Set the height to the container minus gap and distribute the width of the
 * container on each view. 
 * 
 * @author GStack
 */
public class HorizontalDistributeLayout extends AbstractLayout
{
	private float gap = 0;

	public HorizontalDistributeLayout(float gap)
	{
		super();
		this.gap = gap;
	}

	public HorizontalDistributeLayout()
	{
		this(0);
	}
	
	public void layoutViews(ParentView container)
	{
		float x = gap;
		float y = gap;
		for (Iterator iter = container.getChildren().iterator(); iter.hasNext();) 
		{
			Layoutable view = (Layoutable) iter.next();
			view.setWidth((container.getWidth() - gap) / (container.getChildren().size()) - gap);
			view.setHeight(container.getHeight() - 2 * gap);
			view.setX(x);
			view.setY(y);
			x = x + view.getWidth() + gap;						
		}
	}
	
	@Override
	public Dimension2D getMinimumLayoutSize(ParentView container)
	{
		float maxHeight = Float.MIN_VALUE;
		float x = gap;
		for (Iterator iter = container.getChildren().iterator(); iter.hasNext();) 
		{
			Layoutable view = (Layoutable) iter.next();
			x = x + view.getMinWidth() + gap;						
			maxHeight = Math.max(maxHeight, view.getMinHeight());					
		}	
		Dimension2D dim = new Dimension();
		dim.setSize(x, maxHeight);
		return dim;
	}


}
