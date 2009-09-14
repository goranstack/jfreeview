package se.bluebrim.view.value;

/**
 * A mutable integer value.
 * 
 * @author G Stack
 *
 */
public class IntegerValue extends MutableValue
{	
	public int value;

	public IntegerValue(int value)
	{
		this.value = value;
	}
	
	public Integer getIntegerValue()
	{
		return new Integer(value);
	}
}
