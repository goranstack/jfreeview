package se.bluebrim.view;


/**
 * Views can be arranged in several layers that is drawn separate
 * from each other. Layers is used to draw dragged and resized views on
 * top of the other views.
 * 
 * @author G Stack
 *
 */
public class ViewLayer
{
	public static final ViewLayer DEFAULT_LAYER = new ViewLayer("Default");
	public static final ViewLayer DRAG_LAYER = new ViewLayer("Drag");
	
	private String name;

	public ViewLayer(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
	
}
