package se.bluebrim.view.zoom;


/**
 * Struct kind of class that enables easy handling of different x and y scale
 * 
 * @author G Stack
 */
public class Scale
{
	public double x;
	public double y;
	
	public Scale()
	{
		this(1.0);
	}
	
	public Scale(double scale)
	{
		this(scale, scale);
	}

	/**
	 * Java2D don't like zero scale factor
	 */
	public Scale(double scaleX, double scaleY)
	{
		x = Math.max(scaleX, 0.02);
		y = Math.max(scaleY, 0.02);
	}
	
	public Scale createInverted()
	{
		return new Scale(1/x, 1/y);
	}

	/**
	 * Same kind of implementation as java.awt.Point
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof Scale)
		{
			Scale scale = (Scale)obj;
			return scale.x == x && scale.y == y;
		}
		return super.equals(obj);
	}
	
	public Scale getCopy()
	{
		return new Scale(x, y);
	}

}
