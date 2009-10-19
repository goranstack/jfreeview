package se.bluebrim.swing.zoom.test;

import java.awt.*;
import java.awt.geom.*;

import se.bluebrim.view.impl.*;
import se.bluebrim.view.swing.ViewPanel;

/**
 * An example of a zoomable component. The first version just painted som lines and numbers.
 * The method is left unused. The second version has a view hierarchy that demonstrates the
 * use of view LayoutManagers and SelectionTool used to select the views.
 * 
 * @author G Stack
 */
public class ZoomableComponent extends ViewPanel
{	

	public ZoomableComponent(AbstractParentView rootView, ViewContext viewContext)
	{
		super(rootView, viewContext);
	}
	
//	public void setSize(Dimension d)
//	{
//		super.setSize(d);
//		rootView.invalidateLayout();
//	}
		
	
	private void drawFigure(Graphics2D g2d)
	{
		g2d.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, 600, new Color(115, 126, 183)));
		g2d.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
		drawLines(g2d);
		int x = 60;
		int y = 80;
		g2d.setColor(new Color(218, 110, 171));
		float lineThickness = 5;
		g2d.setStroke(new BasicStroke(lineThickness));
		g2d.draw(new Line2D.Double(x, y + lineThickness / 2.0, 300, y));
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("SansSerif", Font.ITALIC, 36));
		g2d.drawString("Zoom me!", x, y);
	}

	private void drawLines(Graphics2D g2d)
	{
		int frequence = 30;
		g2d.setColor(Color.LIGHT_GRAY);

		// Horizontal lines
		int width = (int) (getWidth());
		int y = frequence;
		while (y < getHeight()) {
			g2d.drawLine(0, y, width, y);
			y = y + frequence;
		}
		// verical lines
		int height = (int) (getHeight());
		int x = frequence;
		while (x < getWidth()) {
			g2d.drawLine(x, 0, x, height);
			x = x + frequence;
		}
		drawNumbers(g2d, frequence);
	}

	private void drawNumbers(Graphics2D g2d, int frequence)
	{
		int number = 1;
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 8));

		int x = frequence / 2;
		int y = frequence / 2;
		float ascent = g2d.getFont().getLineMetrics("123", g2d.getFontRenderContext()).getAscent();
		FontMetrics fontMetrics = g2d.getFontMetrics();
		while (y < getHeight()) {
			String text = number + "";
			Rectangle2D bounds = fontMetrics.getStringBounds(text, g2d);

			g2d.drawString(text, (float) (x - bounds.getWidth() / 2.0), (float) (y + ascent - bounds.getHeight() / 2.0));
			x = x + frequence;
			// Line break
			if (x > getWidth()) {
				x = frequence / 2;
				y = y + frequence;
			}
			number++;
		}
	}
						
}