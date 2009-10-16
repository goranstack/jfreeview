package se.bluebrim.view;

import java.awt.*;

/**
 * Implemented by views that is able to show a popup menu.
 * 
 * @author G Stack
 */
public interface PopupMenuProvider
{
	public void showPopupMenu(Component invoker, Point point);

}
