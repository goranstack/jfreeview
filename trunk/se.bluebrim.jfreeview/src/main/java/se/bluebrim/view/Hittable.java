package se.bluebrim.view;

import se.bluebrim.view.tool.*;

/**
 * Implemented by objects that are targets for hitting by tool gestures.
 * 
 * @author G Stack
 */
public interface Hittable extends View
{
	public void singleClickGesture(SingleClickGesture singleClickGesture);
	public void doubleClickGesture(DoubleClickGesture doubleClickGesture);
	public void rubberBandGesture(RubberBandGesture rubberBandGesture);

}
