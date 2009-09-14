package se.bluebrim.view.zoom;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
/**
 * Adds menu items to the specified menu in the contructor. Updates menu items
 * with current state in the ZoomController. ScaleMenu handles both JMenu and JPopupMenu.
 * 
 * @author G Stack
 */
public class ScaleMenu
{
	private ZoomController zoomController;
	private JComponent scaleMenu;
	private JCheckBoxMenuItem scaleToFitMenuItem;
	private JRadioButtonMenuItem hiddenMenuItem; // Class comment of ButtonGroup explains the need for this
	
	public ScaleMenu(ZoomController zoomController, JComponent scaleMenu)
	{
		this.zoomController = zoomController;
		this.scaleMenu = scaleMenu;
		addScaleMenuItems(scaleMenu);
	}
	
	private final void addScaleMenuItems(JComponent menu)
	{
		addScalePercentMenuItems(menu);
		menu.add(new JSeparator());
	    scaleToFitMenuItem = new JCheckBoxMenuItem("Adjust to window");
	    menu.add(scaleToFitMenuItem);
	    scaleToFitMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				zoomController.setScaleToFitMode(!zoomController.isScaleToFitMode());
			}
		});
	}
	
	/**
	 * The hiddenMenuItem is used when the user has zoomed to a scale factor
	 * that is not one of the predefined choices in scale menu. Class comment of 
	 * ButtonGroup explains why you need a hidden menu item.
	 */
	private void addScalePercentMenuItems(JComponent menu)
	{
		ButtonGroup buttonGroup = new ButtonGroup();
		double[] scales = new double[]{0.12, 0.25, 0.50, 1, 2, 4, 8, 16};
		hiddenMenuItem = new JRadioButtonMenuItem("none");
		hiddenMenuItem.setVisible(false);
		buttonGroup.add(hiddenMenuItem);
		menu.add(hiddenMenuItem);
		
		for (int i = 0; i < scales.length; i++) 
		{
			final double scaleFactor = scales[i];
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(zoomController.getPercentFormat().format(scales[i]));
			buttonGroup.add(item);
			menu.add(item);
			item.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							zoomController.setScaleToFitMode(false);
							zoomController.setScaleFactor(new Scale(scaleFactor));
						}
					});

		}

		
	}

	/**
	 * Select one of the predefined scroll factors in the popupmenu if the
	 * user happends to land on one of them when zooming by zoom tool,
	 * window resizing or other gesture that change the zoom factor.
	 */
	public void updateMenuState()
	{		
		MenuElement[] menuItems = getMenuItems();
		boolean noneWasSelected = true;

		scaleToFitMenuItem.setState(zoomController.isScaleToFitMode());
		for (int i = 0; i < menuItems.length; i++) {
			if (menuItems[i].getComponent() instanceof JRadioButtonMenuItem)
			{
				JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem)menuItems[i].getComponent();
				if (zoomController.getScaleFactorAsString().equals(menuItem.getText()))
				{
					noneWasSelected = false;
					menuItem.setSelected(true);
				}
			}
		}
		if (noneWasSelected)
			hiddenMenuItem.setSelected(true);
	}

	/**
	 * <code>JPopupMenu</code> and <code>JMenu</code> has no common super class
	 * or interface that declares their common menu methods.
	 */
	private MenuElement[] getMenuItems()
	{
		if (scaleMenu instanceof JPopupMenu)
			return ((JPopupMenu)scaleMenu).getSubElements();
		else
			return ((JMenu)scaleMenu).getPopupMenu().getSubElements();
			
	}
	
    public void show(Component invoker, int x, int y) 
    {
    	((JPopupMenu)scaleMenu).show(invoker, x, y);
    }
    

}