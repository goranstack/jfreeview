package se.bluebrim.view.swing;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.JPanel;

import se.bluebrim.view.View;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.paint.*;

/**
 * A ViewGlassPane is used as glassPane in the Frame for animation of view
 * movement. This gives us a flicker free movement without repaint caused by
 * damage repair. The class is inspired by Romain Gui's 
 * <a href=http://www.jroller.com/page/gfx?entry=drag_with_style_in_swing>Drag With Style</a>
 * 
 * @author G Stack
 *
 */
public class ViewGlassPane extends JPanel
{
	private AlphaComposite composite;
	private List views;
	private ViewContext viewContext;
	
	public ViewGlassPane(ViewContext viewContext)
	{
		setOpaque(false);
		composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
		this.viewContext = viewContext;
	}
	
	public void setView(View view)
	{
		views = new ArrayList();
		views.add(view);		
	}
	
	public void setViews(List views)
	{
		this.views = views;
	}

	public void paintComponent(Graphics g)
	{
		if (views == null)
			return;
		Graphics2D g2d = (Graphics2D)g;
		Paintable g2dw = new Graphics2DWrapper(g2d, Paintable.Target.Screen, viewContext);
		g2dw.setComposite(composite);
		
		for (Iterator iter = views.iterator(); iter.hasNext();) 
		{
			View view = (View) iter.next();
			view.paint(g2dw);
		}

	}

	public void clearViews()
	{
		views = null;		
	}

}