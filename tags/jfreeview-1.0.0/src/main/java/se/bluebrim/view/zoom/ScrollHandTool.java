package se.bluebrim.view.zoom;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollBar;

import se.bluebrim.view.tool.MainTool;

public class ScrollHandTool extends MainTool
{
	private Cursor openHandCursor;
	private Cursor closedHandCursor;
	private JScrollBar vsb;
	private JScrollBar hsb;


	public ScrollHandTool(ZoomController zoomController)
	{
		super(zoomController);
		vsb = zoomController.getScrollPane().getVerticalScrollBar();
		hsb = zoomController.getScrollPane().getHorizontalScrollBar();
		openHandCursor = createCursor(ScrollHandTool.class, "open-hand-cursor.gif", new Point(16, 16), "Open hand cursor");
		closedHandCursor = createCursor(ScrollHandTool.class, "closed-hand-cursor.gif", new Point(15,15), "Closed hand cursor");
	}

	public void mousePressed(MouseEvent e)
	{
		super.mousePressed(e);
		getViewPanel().setCursor(closedHandCursor);

	}

	public void mouseDragged(MouseEvent e)
	{
		int dy = e.getY() - mousePressedPoint.y;
		int dx = e.getX() - mousePressedPoint.x;
		vsb.setValue(vsb.getValue() - dy);
		hsb.setValue(hsb.getValue() - dx);
	}

	public void mouseReleased(MouseEvent e)
	{
		super.mouseReleased(e);
		getViewPanel().setCursor(openHandCursor);
	}


	protected Icon getIcon()
	{
		return new ImageIcon(getClass().getResource("scrollhand.gif"));
	}

	public void select()
	{
		super.select();
		getViewPanel().setCursor(openHandCursor);		
	}

	
}