package se.bluebrim.view.geom;



public class FloatDimension
{	
	public float width;
	public float height;
	
	public FloatDimension()
	{
	}
	
	public FloatDimension(float width, float height)
	{
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Same kind of implementation as java.awt.Point
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof FloatDimension)
		{
			FloatDimension dimension = (FloatDimension)obj;
			return dimension.width == width && dimension.height == height;
		}
		return super.equals(obj);
	}
	

}