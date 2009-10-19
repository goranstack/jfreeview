package se.bluebrim.view.tool;

import java.awt.event.*;

import se.bluebrim.view.*;

/**
 * The user has done a single click with the selection tool.
 * 
 * @author G Stack
 */
public class SingleClickGesture extends ClickGesture
{

	public SingleClickGesture(MouseEvent mouseEvent)
	{
		super(mouseEvent);
	}

	/**
	 * Do not hit the parent of a hitted view
	 */
	public void hitView(Hittable view)
	{
		if (isHitted(view))
			view.singleClickGesture(this);

	}

}
