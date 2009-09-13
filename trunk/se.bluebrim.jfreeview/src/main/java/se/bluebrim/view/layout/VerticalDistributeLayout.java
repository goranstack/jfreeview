package se.bluebrim.view.layout;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.util.*;

import se.bluebrim.view.*;

/**
 * Arranges the views in vertical pile with a small gap between them.
 * Set the width to the container minus gap and distribute the hight of the
 * container on each view. 
 * 
 * @author GStack
 */
public class VerticalDistributeLayout extends AbstractLayout
{
	private float gap = 0;

	public VerticalDistributeLayout(float gap)
	{
		super();
		this.gap = gap;
	}

	public VerticalDistributeLayout()
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
			view.setHeight((container.getHeight() - gap) / (container.getChildren().size()) - gap);
			view.setWidth(container.getWidth() - 2 * gap);
			view.setX(x);
			view.setY(y);
			y = y + view.getHeight() + gap;						
		}
	}
	
	@Override
	public Dimension2D getMinimumLayoutSize(ParentView container)
	{
		float maxWidth = Float.MIN_VALUE;
		float y = gap;
		for (Iterator iter = container.getChildren().iterator(); iter.hasNext();) 
		{
			Layoutable view = (Layoutable) iter.next();
			y = y + view.getMinHeight() + gap;						
			maxWidth = Math.max(maxWidth, view.getMinWidth());					
		}	
		Dimension2D dim = new Dimension();
		dim.setSize(maxWidth, y);
		return dim;
	}


}
