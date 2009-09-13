package se.bluebrim.view;

import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import se.bluebrim.view.value.*;

/**
 * 
 * @author G Stack
 */
public class TestOptionMenuBuilder
{
	private Map testOptions;	// OptionName<String> : Value<BooleanValue/IntegerValue>
	private Component component;
	
	public TestOptionMenuBuilder()
	{
		this(null);
	}
	
	public TestOptionMenuBuilder(Component component)
	{
		testOptions = new HashMap();
		this.component = component;
	}

	
	public void addOption(String name, MutableValue value)
	{
		testOptions.put(name, value);		
	}
	
	public void createTestOptionMenu(JMenuBar menuBar)
	{

		JMenu optionMenu = new JMenu("Test options");
		menuBar.add(optionMenu);
		addMenuItems(optionMenu);
	}

	public void addMenuItems(JMenu menu)
	{
		for (Iterator iter = testOptions.entrySet().iterator(); iter.hasNext();) 
		{
			Map.Entry option = (Map.Entry) iter.next();
			if (option.getValue() instanceof BooleanValue)
				addBooleanOptionMenuItem(menu, (String)option.getKey());
			else
				if (option.getValue() instanceof IntegerValue)
					addIntegerOptionMenuItem(menu, (String)option.getKey());
		}
	}
	
	private void addBooleanOptionMenuItem(JMenu optionMenu, final String optionName)
	{
		final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(optionName);
		menuItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					((BooleanValue)testOptions.get(optionName)).value = menuItem.getState();
					repaintComponent();
				}
			});
		menuItem.setState(((BooleanValue)testOptions.get(optionName)).value);
		optionMenu.add(menuItem);		
	}
	
	private void addIntegerOptionMenuItem(JMenu optionMenu, final String optionName)
	{
		JMenuItem menuItem = new JMenuItem(optionName + "...");
		menuItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					String answer = JOptionPane.showInputDialog(optionName, ((IntegerValue)testOptions.get(optionName)).getIntegerValue());
					if (answer != null)
						((IntegerValue)testOptions.get(optionName)).value = new Integer(answer).intValue();
				}
			});
		optionMenu.add(menuItem);
		
	}
	
	private void repaintComponent()
	{
		if (component != null)
			component.repaint();
	}
	
	public int getIntegerOption(String name)
	{
		return ((Integer)testOptions.get(name)).intValue();
	}

	public boolean getBooleanOption(String name)
	{
		return ((Boolean)testOptions.get(name)).booleanValue();
	}

}
