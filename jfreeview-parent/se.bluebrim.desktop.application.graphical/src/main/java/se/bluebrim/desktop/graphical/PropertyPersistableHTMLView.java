package se.bluebrim.desktop.graphical;

import java.net.URL;
import java.util.Properties;

import se.bluebrim.view.HTMLView;
import se.bluebrim.view.impl.ViewContext;

/**
 * HTML view that is able to persist its geometry on a property file
 *
 */
public abstract class PropertyPersistableHTMLView extends HTMLView implements PropertyPersistableView
{

	public PropertyPersistableHTMLView(ViewContext viewContext, URL documentBase, URL url) throws Exception
	{
		super(viewContext, documentBase, url);
	}
	
	public void writeProperties(Properties properties)
	{
		PropertyPersistUtil.writeBounds(properties, this, getGeometryPropertyKey());
	}
	
	public void readProperties(Properties properties)
	{
		PropertyPersistUtil.readBounds(properties, this, getGeometryPropertyKey());
	}

	protected abstract String getGeometryPropertyKey();
	
}