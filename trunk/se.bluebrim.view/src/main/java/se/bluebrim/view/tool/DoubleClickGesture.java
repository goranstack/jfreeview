package se.bluebrim.view.tool;

import java.awt.event.*;

import se.bluebrim.view.*;

/**
 * The user has done a double click with the selection tool.
 * 
 * @author G Stack
 */
public class DoubleClickGesture extends ClickGesture
{

	public DoubleClickGesture(MouseEvent mouseEvent)
	{
		super(mouseEvent);
	}

	public void hitView(Hittable view)
	{
		if (isHitted(view))
			view.doubleClickGesture(this);

	}

}
