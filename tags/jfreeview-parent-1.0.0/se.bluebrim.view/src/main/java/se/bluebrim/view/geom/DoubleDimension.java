package se.bluebrim.view.geom;


public class DoubleDimension
{	
	public static int MILLIMETER = 0;
	public static int PIXEL = 1;
	
	private double INCH = 25.4;
	public double width;
	public double height;
	
	public DoubleDimension()
	{
	}
	
	public DoubleDimension(double width, double height)
	{
		this(width, height, PIXEL);		
	}

	
	public DoubleDimension(double width, double height ,int unit)
	{
		if (unit == MILLIMETER)
		{
			this.width = pixels(width);
			this.height = pixels(height);
		} else
		{
			this.width = width;
			this.height = height;
		}
	}
	
	private final double pixels(double millimeters)
	{
		return millimeters/INCH * 72;
	}

}