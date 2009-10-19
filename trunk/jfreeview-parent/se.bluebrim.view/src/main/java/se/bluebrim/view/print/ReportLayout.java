package se.bluebrim.view.print;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;

import se.bluebrim.view.*;
import se.bluebrim.view.layout.AbstractLayout;
import se.bluebrim.view.layout.Layout;

/**
 * Arrange a report with a title view at the top and a body view below it.
 * Both views will have a width equal to the container. The height of the body view
 * will fill all remaining space below the title.
 * 
 * @author G Stack
 *
 */
public class ReportLayout extends AbstractLayout
{
	private ReportTilteView titleView;
	private Layoutable bodyView;
	

	public ReportLayout(ReportTilteView titleView, Layoutable bodyView)
	{
		this.titleView = titleView;
		this.bodyView = bodyView;
	}

	public void layoutViews(ParentView container)
	{
		if (!container.getChildren().contains(bodyView))
			throw new RuntimeException("Not the right container");

		titleView.adjustSizeToText();
		titleView.setY(0);
		titleView.setX(0);
		titleView.setWidth(container.getWidth());
		bodyView.setWidth(container.getWidth());
		bodyView.setX(0);
		bodyView.setY(titleView.getHeight());
		bodyView.setHeight(container.getHeight() - titleView.getHeight());		
	}
	
	@Override
	public Dimension2D getMinimumLayoutSize(ParentView container)
	{
		Dimension2D dim = new Dimension();
		dim.setSize(Math.max(titleView.getWidth(), bodyView.getWidth()), titleView.getHeight() + bodyView.getHeight());
		return dim;
	}

}
