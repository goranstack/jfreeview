package se.bluebrim.isac.desktop;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Properties;

import se.bluebrim.view.TransferableView;
import se.bluebrim.view.impl.BasicView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.paint.Paintable;

/**
 * Abstract superclass to views that persist their geometry in a property file
 * 
 * @author GStack
 *
 */
public abstract class DefaultPropertyPersistableView extends BasicView implements TransferableView, PropertyPersistableView
{
	private String propertyKey;

	public DefaultPropertyPersistableView(ViewContext viewContext, String propertyKey)
	{
		super(viewContext);
		this.propertyKey = propertyKey;
	}

	@Override
	public void readProperties(Properties properties)
	{
		PropertyPersistUtil.readBounds(properties, this, propertyKey);						
	}

	@Override
	public void writeProperties(Properties properties)
	{
		PropertyPersistUtil.writeBounds(properties, this, propertyKey);						
	}

	protected void showSelected(Paintable g)
	{
		if (isSelected())
		{
			g.setColor(Color.BLACK);
			g.setStroke(new BasicStroke(getSelectedFrameThickness()));
			g.draw(getBounds());
		}
	}

}