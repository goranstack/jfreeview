package se.bluebrim.view.impl;


/**
 * Abstract super class for resize handles that change the vertical
 * size at the target object.
 * 
 * @author G Stack
 *
 */
public abstract class VerticalResizeHandle extends ResizeHandle
{
	protected static final float RESIZE_HANDLE_HEIGHT = 8;
	protected VerticalResizeFeedbackProvider verticalResizeFeedbackProvider;

	public VerticalResizeHandle(AbstractView master)
	{
		super(master);
		if (master instanceof VerticalResizeFeedbackProvider)
			verticalResizeFeedbackProvider = (VerticalResizeFeedbackProvider)master;
	}
	
	public float getHeight()
	{
		return RESIZE_HANDLE_HEIGHT;
	}
	

}
