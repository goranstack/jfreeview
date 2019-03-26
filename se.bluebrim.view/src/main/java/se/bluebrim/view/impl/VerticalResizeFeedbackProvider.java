package se.bluebrim.view.impl;



/**
 * Implemented by objects that can provide textual feedback during 
 * vertical resizing with a resize handle.
 * 
 * @author G Stack
 *
 */
public interface VerticalResizeFeedbackProvider extends ResizeFeedbackProvider
{
	public String getNorthSideFeedback();
	public String getSouthSideFeedback();

}
