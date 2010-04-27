package se.bluebrim.screenshot.maven.plugin;

import java.awt.Component;

import javax.swing.JComponent;


/**
 * Helper methods for applying screenshot decorator's to Swing UI's
 * 
 * @author Goran Stack
 *
 */
public class DecoratorUtils {

	public interface ComponentVisitor
	{
		public void visit(JComponent component);
	}

	/**
	 * Recursive method traversing the component hierarchy
	 */
	public static void eachComponent(JComponent component, DecoratorUtils.ComponentVisitor visitor)
	{
		for (Component child : component.getComponents())
			if (child instanceof JComponent) {
				eachComponent((JComponent) child, visitor);
			}
		visitor.visit(component);
	}
	
	/**
	 * 
	 * @return The last component in the component hierarchy with the specified name.
	 */
	public static JComponent findNamedComponent(JComponent root, final String name)
	{
		if (name.equals(root.getName()))
			return root;
		FindNamedComponentVisitor visitor = new FindNamedComponentVisitor(name);
		eachComponent(root, visitor);		
		return visitor.found;
	}
	
	private static class FindNamedComponentVisitor implements ComponentVisitor
	{
		String name;
		JComponent found;
		
		
		public FindNamedComponentVisitor(String name) {
			super();
			this.name = name;
		}


		@Override
		public void visit(JComponent component) {
			if (name.equals(component.getName()))
				found = component;			
		}
		
	}

}
