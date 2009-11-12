package se.bluebrim.view.example.svg;

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;

/**
 * Use this paint to get a checker board pattern common in
 * many paint programs. The checker board make it easier to detect transparent
 * areas in graphics drawn on top of the checker board pattern.
 * 
 * @author Goran Stack
 *
 */
public class CheckerBoardPaint implements Paint
{

	@Override
	public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform,
			RenderingHints hints)
	{
		return new PaintContext(){

			@Override
			public void dispose()
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public ColorModel getColorModel()
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Raster getRaster(int x, int y, int w, int h)
			{
				return null;
			}
			
		};
	}

	@Override
	public int getTransparency()
	{
		return 0;
	}

}
