package se.bluebrim.view.impl;



/**
 * Implemented by objects that can provide textual feedback during 
 * horizontal resizing with a resize handle.
 * 
 * @author G Stack
 *
 */
public interface HorizontalResizeFeedbackProvider extends ResizeFeedbackProvider
{
	public String getWestSideFeedback();
	public String getEastSideFeedback();
}
