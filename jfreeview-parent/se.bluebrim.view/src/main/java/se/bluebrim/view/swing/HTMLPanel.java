package se.bluebrim.view.swing;

import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Subclass configured to render HTML and handle hyper links
 * 
 * @author Goran Stack
 *
 */
public class HTMLPanel extends JEditorPane 
{	
	public HTMLPanel(URL url, HyperlinkListener hyperlinkListener) throws IOException
	{
		super(url);
		if (hyperlinkListener != null)
			addHyperlinkListener(hyperlinkListener);
		setOpaque(false);
		setEditable(false);
		setBorder(null);
	}
	
	public HTMLPanel(URL url) throws IOException
	{
		this(url, null);
	}
	
	/**
	 * The bullets in html lists are ugly without antialiasing on
	 */
	protected void paintComponent(Graphics g)
	{
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paintComponent(g);
	}

	public static HyperlinkListener createOpenInBrowserHyperlinkListener() 
	{
		return new HyperlinkListener() {			
			
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
				{
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException e1) {
						throw new RuntimeException(e1);
					} catch (URISyntaxException e1) {
						throw new RuntimeException(e1);
					}
				}
				
			}
		};
	}
}
