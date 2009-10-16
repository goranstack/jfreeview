package se.bluebrim.view;

import java.awt.geom.*;

import se.bluebrim.view.impl.ViewContext;


/**
 * Implemented by views that can be transfered to another parent view
 * 
 * @author G Stack
 */
public interface TransferableView extends Selectable, View
{

	public ParentView getParent();

	public void changeParent(ParentView parentView);
	
	/**
	 * 
	 * @return The model that the view is visualizing
	 */
	public Object getModel();
	
	public TransferableView getTransferableCopy(ViewContext viewContext);

	public void setViewContext(ViewContext viewContext);
	
	public void setTransform(AffineTransform transform);
	
	public void translate(float dx, float dy);
	
	public void setPositionDevice(float x, float y);

	public void setLocation(Point2D location);

}
