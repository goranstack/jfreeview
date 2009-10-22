package se.bluebrim.view.zoom;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import se.bluebrim.view.tool.RubberBandingTool;

public class ZoomTool extends RubberBandingTool
{
	private Cursor zoomCursor;
	private ZoomController zoomController;

	public ZoomTool(ZoomController zoomController)
	{
		super(zoomController);
		this.zoomController = zoomController;
		zoomCursor = createCursor(ZoomTool.class, "zoom-cursor.gif", new Point(14, 12), "Zoom cursor");
	}

	protected void rubberBandAction(MouseEvent e, Rectangle rect)
	{
		if (rect.getWidth() < 3 && rect.getHeight() < 3)
			zoomController.zoomInOneStep();
		else {
			double hScale = getViewPanel().getWidth() / rect.getWidth();
			double vScale = getViewPanel().getHeight() / rect.getHeight();
			double scale = Math.min(hScale, vScale);
			zoomController.setScaleAndScroll(new Scale(scale), rect.x, rect.y);
		}

	}

	protected Icon getIcon()
	{
		return new ImageIcon(getClass().getResource("zoom.gif"));
	}

	public void select()
	{
		super.select();
		getViewPanel().setCursor(zoomCursor);
	}

}