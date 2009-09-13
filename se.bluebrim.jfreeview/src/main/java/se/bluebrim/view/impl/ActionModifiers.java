package se.bluebrim.view.impl;

import java.awt.event.*;


/**
 * Instances represent various key combinations that control the behaviour of certain actions. Simular methods as in
 * this class exists in InputEvent but we can't pass around an InputEvent since modifiers are present at drag and drop
 * as well where we dont't have access to an InputEvent.
 * 
 * @author G Stack
 * 
 */
public class ActionModifiers
{
	private int modifiers;


	public ActionModifiers(MouseEvent e)
	{
		this(e.getModifiers());
	}

	
	public ActionModifiers(int modifiers)
	{
		this.modifiers = modifiers;
	}

	/**
	 * Returns whether or not the Shift modifier is down on this event.
	 */
	public boolean isShiftDown()
	{
		return (modifiers & InputEvent.SHIFT_MASK) != 0;
	}

	/**
	 * Returns whether or not the Control modifier is down on this event.
	 */
	public boolean isControlDown()
	{
		return (modifiers & InputEvent.CTRL_MASK) != 0;
	}

	/**
	 * Returns whether or not the Meta modifier is down on this event.
	 */
	public boolean isMetaDown()
	{
		return (modifiers & InputEvent.META_MASK) != 0;
	}

	/**
	 * Returns whether or not the Alt modifier is down on this event.
	 */
	public boolean isAltDown()
	{
		return (modifiers & InputEvent.ALT_MASK) != 0;
	}

	/**
	 * Returns whether or not the AltGraph modifier is down on this event.
	 */
	public boolean isAltGraphDown()
	{
		return (modifiers & InputEvent.ALT_GRAPH_MASK) != 0;
	}

}
