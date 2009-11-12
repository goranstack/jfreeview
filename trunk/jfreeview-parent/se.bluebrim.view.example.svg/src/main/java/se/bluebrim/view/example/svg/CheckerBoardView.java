package se.bluebrim.view.example.svg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import se.bluebrim.view.impl.AbstractParentView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.layout.Layout;
import se.bluebrim.view.paint.Paintable;

/**
 * Use this as a background view to get a checker board pattern common in
 * many paint programs. The checker board make it easier to detect transparent
 * areas in graphics drawn on top of the checker board pattern.
 * 
 * @author Goran Stack
 *
 */
public class CheckerBoardView extends AbstractParentView
{

	private int square = 8;
	private Rectangle rectangle = new Rectangle(0, 0, square, square);
	private Color lightGray = new Color(239, 239, 239);

	public CheckerBoardView(ViewContext viewContext, Layout layoutManager)
	{
		super(viewContext, layoutManager);
	}
	
	@Override
	protected void drawBeforeChildren(Paintable g)
	{
		int rows = Math.round(getHeight() / square) + 1;
		Rectangle clipBounds = g.getClipBounds();

		for (int row = 0; row < rows; row++) {
			drawRow(g.getGraphics(), row, clipBounds);
		}
	}
	
	private void drawRow(Graphics2D g2d, int row, Rectangle clipBounds)
	{
		Rectangle2D rowBounds = new Rectangle2D.Float(0, row * square, getWidth(), square);
		if (rowBounds.intersects(clipBounds))
		{
			AffineTransform at = g2d.getTransform();
			int columns = Math.round(getWidth() / square) + 1;
			g2d.translate(0, row * square);
			for (int column = 0; column < columns; column++) {
				drawSquare(g2d, row, column, clipBounds);
			}
			g2d.setTransform(at);
		}
	}

	private void drawSquare(Graphics2D g2d, int row, int column, Rectangle clipBounds)
	{
		Rectangle squareBounds = new Rectangle(column * square, row * square, square, square);
		if (squareBounds.intersects(clipBounds))
		{
			AffineTransform at = g2d.getTransform();
			g2d.translate(column * square, 0);
			g2d.setColor((isEven(row) && isEven(column)) || (isOdd(row) && isOdd(column)) ? lightGray :getSquareColor());
			g2d.fill(rectangle);
			g2d.translate(square, 0);
			g2d.setTransform(at);
		}
	}
	
	/**
	 * Uncomment to verify clipping testing. Reveals what is repainted. Areas that
	 * should not be repainted keep the colors.
	 *
	 */
	private Color getSquareColor()
	{
//		return new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
		return Color.WHITE;
	}

	private boolean isEven(int number)
	{
		return number % 2 == 0;
	}

	private boolean isOdd(int number)
	{
		return !isEven(number);
	}


}
