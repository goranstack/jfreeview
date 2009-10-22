package se.bluebrim.view.impl;



/**
 * Abstract super class for resize handles that change the horizontal
 * size at the target object.
 * 
 * @author G Stack
 *
 */
public abstract class HorizontalResizeHandle extends ResizeHandle
{
	protected static final float RESIZE_HANDLE_WIDTH = 8;
	protected HorizontalResizeFeedbackProvider horizontalResizeFeedbackProvider;

	public HorizontalResizeHandle(AbstractView master)
	{
		super(master);
		if (master instanceof HorizontalResizeFeedbackProvider)
			horizontalResizeFeedbackProvider = (HorizontalResizeFeedbackProvider)master;

	}
	
	public float getWidth()
	{
		return RESIZE_HANDLE_WIDTH;
	}
			


}
