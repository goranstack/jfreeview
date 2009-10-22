package se.bluebrim.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicHTML;

import se.bluebrim.view.geom.DoubleInsets;
import se.bluebrim.view.impl.BasicView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.paint.Paintable;

/**
 * Load HTML from file and paint the HTML at the view location.
 * 
 * @author GStack
 *
 */
public class HTMLView extends BasicView implements Layoutable, TransferableView
{
	private URL url;
	private final JComponent component = new JLabel();
	private javax.swing.text.View htmlView;
	
	/**
	 * The documentBase is a file in the directory where the images are that are
	 * referred from the HTML
	 */
	public HTMLView(ViewContext viewContext, URL documentBase, URL url) throws Exception
	{
		super(viewContext);
		setWidth(300);
		setHeight(100);
		createResizeHandles();
		component.putClientProperty(BasicHTML.documentBaseKey, documentBase);
		this.url = url;
	}
	
	private javax.swing.text.View getHTMLView()
	{
		if (htmlView == null)
		{
			try
			{
				htmlView = BasicHTML.createHTMLView(component, filterHTML(loadHTML()));
			} catch (IOException e)
			{
				throw new RuntimeException("Unable to read legend.html");
			}		
		}
		return htmlView;
	}
	
	protected void invalidateHtml()
	{
		htmlView = null;
	}
	
	/**
	 * Override this to process the HTML for example replacing
	 * place holders with variable data.
	 */
	protected String filterHTML(String html)
	{
		return html;
	}
			
	public Object getModel()
	{
		return htmlView;
	}

	private String loadHTML() throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(getHTMLAsStream()));
		StringBuffer fileData = new StringBuffer(1000);
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1)
		{
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();			
	}

	protected InputStream getHTMLAsStream() throws IOException
	{
		return url.openStream();
	}
	
	
	@Override
	protected void paintLayer(Paintable g)
	{
		javax.swing.text.View view = getHTMLView();
		if (!view.isVisible())
			System.out.println("Not visible");
		float frameThickness = getSelectedFrameThickness();
		drawTextView(g, view, new DoubleInsets(frameThickness, frameThickness, frameThickness, frameThickness));
		if (isSelected())
		{
			g.setColor(Color.BLACK);
			g.setStroke(new BasicStroke(frameThickness));
			g.draw(getBounds());
		}
	}
						
}
