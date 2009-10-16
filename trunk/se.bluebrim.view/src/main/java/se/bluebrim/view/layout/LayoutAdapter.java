package se.bluebrim.view.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import se.bluebrim.view.Layoutable;
import se.bluebrim.view.ParentView;
import se.bluebrim.view.ResizeListener;

/**
 * This class makes it possible to use AWT layout managers in the view
 * framework. You can only use the layout managers that don't require a
 * Container in the constructor. You have to create a subclass to this class for
 * using layout managers that requires a Container in the constructor. See
 * BoxLayoutAdapter how this is done.
 * 
 * @author GStack
 */
public class LayoutAdapter extends AbstractLayout
{
	private static final double SCALE_FACTOR = 1;

	private LayoutManager layoutManager;
	protected LayoutTarget layoutTarget;

	protected LayoutAdapter()
	{
	}

	public LayoutAdapter(LayoutManager layoutManager)
	{
		super();
		this.layoutManager = layoutManager;
	}
	
	/**
	 * It is unpractical to require the parent view in the constructor.
	 */
	public void setParentView(ParentView parentView)
	{
		this.layoutTarget = new LayoutTarget(parentView, layoutManager);		
	}

	public void addLayoutable(Layoutable layoutable, Object constraints)
	{
		if (constraints != null)
			layoutTarget.add(new LayoutableWrapper(layoutable), constraints);
		else
			layoutTarget.add(new LayoutableWrapper(layoutable));
	}

	public void removeLayoutable(Layoutable layoutable)
	{
		layoutTarget.remove(new LayoutableWrapper(layoutable));
	}

	@Override
	public void layoutViews(ParentView parentView)
	{
		layoutTarget.doLayout();
		Component[] components = layoutTarget.getComponents();
		for (int i = 0; i < components.length; i++)
		{
			((LayoutAdapter.LayoutableWrapper) components[i]).applyBounds();
		}
	}
	
	@Override
	public Dimension2D getMinimumLayoutSize(ParentView container)
	{
		return layoutManager.minimumLayoutSize(layoutTarget);
	}
	
	@Override
	public Dimension2D getPreferredLayoutSize(ParentView container)
	{
		return layoutManager.preferredLayoutSize(layoutTarget);
	}
	
	@Override
	public Dimension2D getMaximumLayoutSize(ParentView container)
	{
		return layoutManager.minimumLayoutSize(layoutTarget);
	}

	/**
	 * From a Layoutable to a Component
	 */
	private static void transferBounds(Layoutable layoutable, Component component)
	{
		Rectangle2D bounds = layoutable.getBounds();
		component.setBounds((int) (bounds.getX() * SCALE_FACTOR), (int) (bounds.getY() * SCALE_FACTOR),
				(int) (bounds.getWidth() * SCALE_FACTOR), (int) (bounds.getHeight() * SCALE_FACTOR));
	}
	
	/**
	 * From a Component to a Layoutable
	 */
	private static void transferBounds(Component component, Layoutable layoutable)
	{
		layoutable.setX((float) (component.getX() / SCALE_FACTOR));
		layoutable.setY((float) (component.getY() / SCALE_FACTOR));
		layoutable.setWidth((float) (component.getWidth() / SCALE_FACTOR));
		layoutable.setHeight((float) (component.getHeight() / SCALE_FACTOR));
	}

	
	/**
	 * This class make it possible for a Layoutable to act as a Component
	 * and be layouted by a layout manager.
	 */
	private static class LayoutableWrapper extends Component
	{
		private Layoutable layoutable;

		public LayoutableWrapper(Layoutable layoutable)
		{
			super();
			this.layoutable = layoutable;
			transferBounds(layoutable, this);
		}

		public void applyBounds()
		{
			transferBounds(this, layoutable);
		}
		
		@Override
		public Dimension getMinimumSize()
		{
			return new Dimension((int)layoutable.getMinWidth(), (int)layoutable.getMinHeight());
		}
		
		@Override
		public Dimension getMaximumSize()
		{
			return new Dimension((int)layoutable.getMaxWidth(), (int)layoutable.getMaxHeight());
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null)
			{
				return false;
			}
			if (obj == this)
			{
				return true;
			}
			if (obj.getClass() != getClass())
			{
				return false;
			}
			LayoutableWrapper another = (LayoutableWrapper) obj;
			return new EqualsBuilder().append(layoutable, another.layoutable).isEquals();
		}

		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(35, 49).append(layoutable).toHashCode();
		}
	}

	/**
	 * This class make it possible for a ParentView to act as a Container
	 * and be the layout target for a layout manager.
	 */
	protected static class LayoutTarget extends Container
	{		
		public LayoutTarget(final ParentView parentView)
		{
			super();
			transferBounds(parentView, this);
			parentView.addResizeListener(new ResizeListener()
			{

				public void resized(String dimension)
				{
					transferBounds(parentView, LayoutTarget.this);
					validate();
				}
			});
		}
		
		public LayoutTarget(ParentView viewContainer, LayoutManager layoutManager)
		{
			this(viewContainer);
			setLayout(layoutManager);
		}
		
	}


}