package se.bluebrim.view.layout;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.util.*;

import se.bluebrim.view.*;

/**
 * Arranges the views in rows with a small gap between them.
 * Adjust the size of the views keeping aspect ratio to an optimal
 * fit in the container.
 * 
 * @author G Stack
 */
public class OptimalFitLayout extends AbstractLayout
{
	private static float gap = 8;

	
	private ParentView container;
	private float desiredLayoutWidth;
	private float desiredLayoutHeight;
	private List views;

	public void layoutViews(ParentView container)
	{
		if (container.getWidth() <= 0 || container.getHeight() <= 0)
			return;
		this.container = container;
		views = container.getChildren();
		desiredLayoutWidth = container.getWidth() - gap;
		desiredLayoutHeight = container.getHeight() - gap;
		
		binarySearch();
	}
	
	@Override
	public Dimension2D getMinimumLayoutSize(ParentView container)
	{
		Dimension2D dim = new Dimension();
		dim.setSize(desiredLayoutWidth, desiredLayoutHeight);
		return dim;
	}
	
	private void tryLayout(float viewWidth)
	{
		float x = gap;
		float y = gap;
		for (Iterator iter = container.getChildren().iterator(); iter.hasNext();) 
		{
			Layoutable view = (Layoutable) iter.next();
			double aspectRatio = view.getWidth() / view.getHeight();
			view.setWidth(viewWidth);
			view.setHeight((float)(viewWidth / aspectRatio));
			view.setX(x);
			view.setY(y);
			x = advanceRight(x, view);
			
			// Linebreak
			if (view.getRightBound() > container.getWidth() - gap)
			{
				y = advanceDown(y, view);
				x = gap;
				view.setX(x);
				view.setY(y);
				x = advanceRight(x, view);
			}
			
		}
		
	}

	private float advanceDown(float y, Layoutable view)
	{
		y = y + view.getHeight() + gap;
		return y;
	}

	private float advanceRight(float x, Layoutable view)
	{
		x = x + view.getWidth() + gap;
		return x;
	}
	
	/**
	 * Use binary search to find out the view width that gives the best fitted layout.
	 * Find a height as close as possible to the desired layout height without overstep.
	 * Since the function contains sudden jumps we do an extra check at the end and
	 * if there is an overstep the method return the value prior to the overstep. <br>
	 * Finally the layout is centered since the width of the lines are rarely the same
	 * as the desired width.
	 */
	private void binarySearch()
	{
		float min = 0;
		float max = desiredLayoutHeight * 3;
		float mid = 0;
		float prevMid = 0;
		float closeEnough = 0.1f;
		while (max - min > closeEnough)
		{
			mid = (min + max)/2;
			tryLayout(mid);
//			System.out.println("Trying with: " + mid + " .Resulting in: " + getLayoutHeight());
			if (getLayoutHeight() > desiredLayoutHeight)
				max = mid;
			else
			{
				min = mid;
				prevMid = mid;
			}
		}
		// Handle the sudden jumps issue
		if (getLayoutHeight() > desiredLayoutHeight)
			tryLayout(prevMid);
		
		maximizeWidthToContainerWidth();
		centerLayoutHorizontally();
	}
	
	/**
	 * Returns the smallest height that contains all views
	 */
	private float getLayoutHeight()
	{
		float height = 0;
		for (Iterator iter = views.iterator(); iter.hasNext();) 
		{
			Layoutable view = (Layoutable) iter.next();
			height = Math.max(height, view.getBottomBound());
		}
		return height;		
	}

	/**
	 * Returns the smallest width that contains all views
	 */
	private float getLayoutWidth()
	{
		float width = 0;
		for (Iterator iter = views.iterator(); iter.hasNext();) 
		{
			Layoutable view = (Layoutable) iter.next();
			width = Math.max(width, view.getRightBound());
		}
		return width;		
	}

	private void maximizeWidthToContainerWidth()
	{
		for (Iterator iter = views.iterator(); iter.hasNext();) 
		{
			Layoutable view = (Layoutable) iter.next();
			view.setWidth(Math.min(container.getWidth(), view.getWidth()));
		}

	}

	private void centerLayoutHorizontally()
	{
		float offset = desiredLayoutWidth - getLayoutWidth();
		if (offset > 0)
			translateHorizontal((float)((offset)/2.));
	}
	
	/**
	 * Move all views horizontally the specified distance and limit the with
	 * to the container.
	 */
	private void translateHorizontal(float distance)
	{
		for (Iterator iter = views.iterator(); iter.hasNext();) 
		{
			Layoutable view = (Layoutable) iter.next();
			view.setX(view.getX() + distance);
			view.setWidth(Math.min(container.getWidth(), view.getWidth()));
		}

	}

}
