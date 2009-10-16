package se.bluebrim.view.print;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;

import se.bluebrim.view.Layoutable;
import se.bluebrim.view.ParentView;
import se.bluebrim.view.layout.AbstractLayout;

/**
 * Arrange a report body with a rowHeadersView at the top,
 * a columnHeadersrowHeadersiew to the left and the contentView in the middle.<br>
 * rowHeadersView and columnHeadersView are allowed to null.
 * 
 * @author G Stack
 *
 */
public class ReportBodyLayout extends AbstractLayout
{
	private Layoutable rowHeadersView;
	private Layoutable columnHeadersView;
	private Layoutable contentView;
	

	public ReportBodyLayout(Layoutable columnHeadersView, Layoutable contentView, Layoutable rowHeadersView)
	{
		this.columnHeadersView = columnHeadersView;
		this.contentView = contentView;
		this.rowHeadersView = rowHeadersView;
	}

	public void layoutViews(ParentView container)
	{
		if (!container.getChildren().contains(contentView))
			throw new RuntimeException("Not the right container");

		contentView.setY(0);
		if (rowHeadersView != null)
		{
			columnHeadersView.setX(rowHeadersView.getWidth());
			columnHeadersView.setY(0);
			contentView.setX(rowHeadersView.getWidth());
		}
		if (columnHeadersView != null)
		{
			rowHeadersView.setY(columnHeadersView.getHeight());
			contentView.setY(columnHeadersView.getHeight());		
		}
		container.adjustSizeToChildren();		
	}
	
	@Override
	public Dimension2D getMinimumLayoutSize(ParentView container)
	{
		float width = contentView.getWidth();
		float height = contentView.getHeight();
		if (rowHeadersView != null)
			width = width + rowHeadersView.getWidth();
		if (columnHeadersView != null)
			height = height + columnHeadersView.getHeight();					
		Dimension2D dim = new Dimension();
		dim.setSize(width, height);
		return dim;
	}


}
