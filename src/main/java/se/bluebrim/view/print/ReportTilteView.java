package se.bluebrim.view.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import se.bluebrim.view.geom.DoubleInsets;
import se.bluebrim.view.impl.BasicView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.paint.Paintable;

/**
 * Used to display a report title at the top of a report created by
 * PrintPreviewFrame
 * 
 * @author GStack
 *
 */
public class ReportTilteView extends BasicView
{
	private String title;
	private DoubleInsets textInsets;
	private Font font;

	public ReportTilteView(ViewContext viewContext, String title, DoubleInsets textInsets)
	{
		super(viewContext);
		this.title = title;
		this.textInsets = textInsets;
		font = new Font("SansSerif", Font.PLAIN, 28);
	}

	public void adjustSizeToText()
	{
		// Must set bounds to zero since getOpticalShape returns the union between bounds and textbounds 
		setWidth(0);
		setHeight(0);
		Rectangle2D size = getOpticalShape().getBounds2D();
		setWidth((float)(size.getWidth() - size.getX()));
		setHeight((float)(size.getHeight()- size.getY()));
	}
	
	protected void paintLayer(Paintable g)
	{
		g.setColor(Color.BLACK);
		g.setFont(font);
		drawLeftJustifiedText(g, title, (float)textInsets.left, (float)textInsets.top);
	}
	
	protected Font getFont()
	{
		return font;
	}

	/**
	 * The text bounds extended with the text insets
	 */
	public Shape getOpticalShape()
	{
		Shape shape = super.getOpticalShape();
		return shape.getBounds2D().createUnion(centerRectangleOverView(getStringSize(title, textInsets))); 
	}

}
