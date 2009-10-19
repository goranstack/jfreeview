package se.bluebrim.view.print;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import se.bluebrim.view.ResizableContainer;
import se.bluebrim.view.geom.DoubleDimension;
import se.bluebrim.view.geom.DoubleInsets;
import se.bluebrim.view.impl.AbstractParentView;
import se.bluebrim.view.impl.BasicView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.paint.Paintable;
import se.bluebrim.view.zoom.Scale;

/**
 * A view that gives the illusion of a paper laying on a surface. To achieve correct 
 * measurements on screen, sizes expressed in pixels must consider screen resolution.
 * That means if you select A4 and 100% scale you will have a paper rectangle on the
 * screen with the correct physical measurement. <br>
 * Content is scaled to fit using two methods:
 * <ol>
 * <li><strong>Layout content to fit</strong> means that the layout manager of the content view
 * is capable of adjusting all views to fit in to the container.</li>
 * <li><strong>Scale content to fit</strong> means that the content is scaled by transformation
 * in the print method.</li>
 * </ol>
 * 
 * @author G Stack
 */
public class PaperOnDesktopView extends AbstractParentView
{
	
	class FilledArea
	{
		Area area;
		Color color;
		
		public FilledArea(Area area, Color color)
		{
			this.area = area;
			this.color = color;
		}
		
		void paint(Paintable g)
		{
			g.setColor(color);
			g.fill(area);
		}
	}
	
	static private final int SHADOW_SIZE = 4;
	static private final int FRAME_LINE_WIDTH = 1;	// The line thickness of the paper frame

	static public final int LAYOUT_CONTENT_TO_FIT = 0;
	static public final int SCALE_CONTENT_TO_FIT = 1;

	private Rectangle2D paperRect;
	private BasicView content;
	private int makeContentFitMethod;
	private float desktopSizeFactor;
	private DoubleInsets margins = new DoubleInsets(0, 0, 0, 0);
	private double resolutionFactor = Toolkit.getDefaultToolkit().getScreenResolution() / 72.;
	private Component ownerComponent;
	private boolean paintMargins = false;
	private List filledAreas;
	private boolean optimizedFilling = true;
	private boolean disposeContent = true;	

	public PaperOnDesktopView(ViewContext context, Component ownerComponent, float desktopSizeFactor, int makeContentFitMethod)
	{
		super(context);
		if (desktopSizeFactor < 1)
			throw new IllegalArgumentException("desktopSizeFactor must be larger than 1");
		this.desktopSizeFactor = desktopSizeFactor;
		this.makeContentFitMethod = makeContentFitMethod;
		this.ownerComponent = ownerComponent;
	}
		
	/**
	 * Expand the paper size with the frame line width
	 * otherwise it will be over painted by the content view.
	 * 
	 * @param paperSize in pixels
	 * @param desktopSizeFactor a value of 1.2 means that the desktop is 20% larger than 
	 * the papers largest measurement  
	 */
	private void setPageFormat(DoubleDimension paperSize, DoubleInsets margins)
	{
		this.margins = margins;
		double largestMeasurement = Math.max(paperSize.width, paperSize.height);
		double desktopOutset = ((desktopSizeFactor - 1) * largestMeasurement/2.);
		double x = desktopOutset;
		double y = desktopOutset;
		double contentWidth = paperSize.width - (margins.left + margins.right);
		double contentHeight = paperSize.height - (margins.top + margins.bottom);
		double paperWidth = (paperSize.width + FRAME_LINE_WIDTH * 2);
		double paperHeight = (paperSize.height + FRAME_LINE_WIDTH * 2);
		paperRect = new Rectangle2D.Double(x, y, paperWidth, paperHeight);
		
		setWidth((float)(paperWidth + desktopOutset * 2));
		setHeight((float)(paperHeight + desktopOutset * 2));

// 	Nice try but the print out was fucked up
//		content.setX((float)(paperRect.getX() + FRAME_LINE_WIDTH + margins.left));
//		content.setY((float)(paperRect.getY() + FRAME_LINE_WIDTH + margins.top));
		
		if (makeContentFitMethod == LAYOUT_CONTENT_TO_FIT)
		{
			content.setWidth((float)contentWidth);
			content.setHeight((float)contentHeight);
		}
		
		if (ownerComponent != null)
		{
			if (ownerComponent instanceof JComponent)
				((JComponent)ownerComponent).revalidate();
			else
					ownerComponent.invalidate();
			ownerComponent.repaint();
		}
		updateViewTransformation(transform);
		createFilledAreas();
	}

	/**
	 * Paint a paper rectangle with a gray shadow. The paper rect is expanded by the frame line width.
	 * The content is draw inside that expansion.
	 */
	protected void paintLayer(Paintable g)
	{
		if (optimizedFilling)
			optimizedFilling(g);
		else
			unOptimizedFilling(g);

		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1));
		g.draw(paperRect);
		paintMargins(g);

		if (content != null)
		{
			g.translate(paperRect.getX() + FRAME_LINE_WIDTH + margins.left, paperRect.getY() + FRAME_LINE_WIDTH + margins.top);
			if (makeContentFitMethod == SCALE_CONTENT_TO_FIT)
			{
				Scale scaleToFit = calculateScaleToFit(getPrintingArea().getWidth(), getPrintingArea().getHeight(), content.getWidth(), content.getHeight());
		      g.scale(scaleToFit.x, scaleToFit.y);
			}
			content.paint(g);
		}
	}
	
	private void unOptimizedFilling(Paintable g)
	{
		g.setColor(ownerComponent.getBackground());
		g.fill(getBounds());

		g.translate(SHADOW_SIZE, SHADOW_SIZE);
		g.setColor(Color.BLACK);
		g.fill(paperRect);
		g.translate(-SHADOW_SIZE, -SHADOW_SIZE);
		g.setColor(Color.WHITE);
		g.fill(paperRect);		
	}
	
	private void optimizedFilling(Paintable g)
	{
		Rectangle2D clip = g.getClipBounds();
		for (Iterator iter = filledAreas.iterator(); iter.hasNext();) {
			FilledArea filledArea = (FilledArea) iter.next();
			if (filledArea.area.intersects(clip))
				filledArea.paint(g);			
		}
	}
	
	/**
	 * Create the filled areas that together makes the illusion of of a paper laying on a surface.
	 * The first version of the class had a simple paint method that draw several overlapping
	 * fills to achieve the effect. That is a waste of CPU if the zoom and/or scrolls
	 * hides what this class draws. By dividing the drawing in separate views its possible to
	 * Suppress unnecessary drawing by testing intersection with the current clip area.
	 */
	private final void createFilledAreas()
	{
		filledAreas = new ArrayList();
		Area desktopArea = new Area(getBounds());
		Area paperArea = new Area(paperRect);
		Area shadowArea = new Area(new Rectangle2D.Double(paperRect.getX() + SHADOW_SIZE, paperRect.getY() + 
				SHADOW_SIZE,paperRect.getWidth(), paperRect.getHeight()));
		
		shadowArea.subtract(paperArea);
		desktopArea.subtract(paperArea);
		desktopArea.subtract(shadowArea);
		filledAreas.add(new FilledArea(desktopArea, ownerComponent.getBackground()));
		filledAreas.add(new FilledArea(shadowArea, Color.BLACK));
		filledAreas.add(new FilledArea(paperArea, Color.WHITE));
	}

	/**
	 * Margins is painted as a thin line independent of scale factor
	 */
	private void paintMargins(Paintable g)
	{
		if (paintMargins)
		{
			g.setStroke(new BasicStroke((float)(1 / g.getScaleX())));
			g.setColor(Color.LIGHT_GRAY);
			g.draw(getPrintingArea());
		}
	}

	/**
	 * 
	 * @return the area within margins
	 */
	private Rectangle2D getPrintingArea()
	{
		if (paperRect == null)
			return null;
		else
			return new Rectangle2D.Double(paperRect.getX() + margins.left, paperRect.getY() + margins.top, 
					paperRect.getWidth() - (margins.right + margins.left), 
					paperRect.getHeight()- (margins.bottom + margins.top));
	}
	
	public void setContent(BasicView content)
	{
		this.content = content;
		removeAllChildren();
		addChild(content);
	}
	
	public void setPageFormat(PageFormat pageFormat)
	{
		setPageFormat(getPageSizeFromPageFormat(pageFormat), getPageMarginsFromPageFormat(pageFormat));
	}
	
	private DoubleDimension getPageSizeFromPageFormat(PageFormat pageFormat)
	{
		Paper paper = pageFormat.getPaper();
		if (pageFormat.getOrientation() == PageFormat.LANDSCAPE)
			return new DoubleDimension(paper.getHeight()*resolutionFactor, paper.getWidth()*resolutionFactor, DoubleDimension.PIXEL);		
		else
			return new DoubleDimension(paper.getWidth()*resolutionFactor, paper.getHeight()*resolutionFactor, DoubleDimension.PIXEL);		
	}
	
	private DoubleInsets getPageMarginsFromPageFormat(PageFormat pageFormat)
	{
		Paper paper = pageFormat.getPaper();
		// top left bottom right
		double top = paper.getImageableY();
		double left = paper.getImageableX();
		double bottom = paper.getHeight() - paper.getImageableY() - paper.getImageableHeight();
		double right = paper.getWidth() - paper.getImageableX() - paper.getImageableWidth();
		
		if (pageFormat.getOrientation() == PageFormat.LANDSCAPE)
		{
			double temp = right;
			right = top;
			top = left;
			left = bottom;
			bottom = temp;
		}		
		return new DoubleInsets(top*resolutionFactor, left*resolutionFactor , bottom*resolutionFactor, right*resolutionFactor);
	}

	/**
	 *  Needs a reference to the parent component to invalidate when
	 *  our size is changed.
	 */
	public void setOwnerComponent(Component ownerComponent)
	{
		this.ownerComponent = ownerComponent;
	}

	public boolean isPaintMargins()
	{
		return paintMargins;
	}

	public void setPaintMargins(boolean paintMargins)
	{
		this.paintMargins = paintMargins;
	}

	public void toggleOptimizedFilling()
	{
		optimizedFilling = !optimizedFilling;
	}

	public boolean isOptimizedFilling()
	{
		return optimizedFilling;
	}

	/**
	 * Content is not included in the child collection
	 */
	public void dispose()
	{
		super.dispose();
		if (disposeContent)
			content.dispose();
	}

	/**
	 * Set to false if the content view hierachy is shared and not owned by the
	 * PaperOnDesktopView
	 */
	public void setDisposeContent(boolean disposeContent)
	{
		this.disposeContent = disposeContent;
	}

}
