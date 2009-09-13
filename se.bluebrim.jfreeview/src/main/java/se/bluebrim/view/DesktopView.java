package se.bluebrim.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.geom.Dimension2D;

import se.bluebrim.view.impl.AbstractParentView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.layout.AbstractLayout;
import se.bluebrim.view.paint.Paintable;

import com.lowagie.text.PageSize;

/**
 * Background for the shadowed paper view. Included in the
 * ViewPanel that is included in the ScrollPane that is the
 * content of the window. The RootView has a single PaperView child.
 * The PaperView size and location in the RootView is managed
 * by the RootLayout installed in the RootView.
 *
 */
public class DesktopView extends AbstractParentView
{
	public static final Color DESKTOP_COLOR = Color.LIGHT_GRAY;

	public DesktopView(ViewContext viewContext)
	{
		super(viewContext, new RootLayout());
		setSizeControlledByChildren(true);
	}
	
	public void setPaperView(PaperView paperView)
	{
		addChild(paperView);
		layout();			
	}
	
	@Override
	protected void drawBeforeChildren(Paintable g)
	{
		g.setColor(DESKTOP_COLOR);
		g.fill(getBounds());
		g.setColor(Color.BLACK);
	}
	
	@Override
	public void adjustSizeToChildren()
	{
		PaperView paperView = (PaperView)getChildren().get(0);
		setWidth(paperView.getWidth() + (2 * RootLayout.PAPER_INSET));
		setHeight(paperView.getHeight() + (2 * RootLayout.PAPER_INSET));
	}

	private static class RootLayout extends AbstractLayout
	{		
		private static final int PAPER_INSET = 20;

		@Override
		public void layoutViews(ParentView container)
		{
			Layoutable paperView = (Layoutable)container.getChildren().get(0);
			paperView.setX(PAPER_INSET);
			paperView.setY(PAPER_INSET);
				
			double resolutionFactor = Toolkit.getDefaultToolkit().getScreenResolution() / 72.0;
			com.lowagie.text.Rectangle paperSize = PageSize.A3.rotate();
			paperView.setHeight((float)(paperSize.height() * resolutionFactor));
			paperView.setWidth((float)(paperSize.width() * resolutionFactor));
		}
		
		@Override
		public Dimension2D getMinimumLayoutSize(ParentView container)
		{
			double resolutionFactor = Toolkit.getDefaultToolkit().getScreenResolution() / 72.0;
			com.lowagie.text.Rectangle paperSize = PageSize.A3.rotate();			
			Dimension2D dim = new Dimension();
			dim.setSize(paperSize.width() * resolutionFactor + (2 * PAPER_INSET), paperSize.height() * resolutionFactor + (2 * PAPER_INSET));
			return dim;
		}

	}
	
}