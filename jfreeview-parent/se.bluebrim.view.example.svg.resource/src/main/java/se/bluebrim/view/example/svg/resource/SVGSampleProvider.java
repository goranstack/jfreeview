package se.bluebrim.view.example.svg.resource;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * Initializes a list of predefined SVG resources.
 * 
 * @author Goran Stack
 *
 */
public class SVGSampleProvider {

	
	public static class Resource
	{
		private URL originator;
		private URL resource;

		public Resource(URL originator, URL resource) {
			super();
			this.originator = originator;
			this.resource = resource;
		}

		public URL getOriginator() {
			return originator;
		}
		
		public URL getResource() {
			return resource;
		}
				
	}
	
	private List<Resource> resources = new ArrayList<Resource>();
	private Iterator<Resource> resourceIterator;
	private AbstractAction hyperLinkAction;

	public SVGSampleProvider() throws MalformedURLException 
	{
		initializeSampleList();
	}
	
	private void initializeSampleList() throws MalformedURLException
	{
		addSample("http://vector-art.blogspot.com", "Bolt_NWR1.svg");
		addSample("http://xmlgraphics.apache.org/batik", "batik3D.svg");
		addSample("http://www.isc.tamu.edu/~lewing", "NewTux.svg");
		addSample("http://openclipart.org/media/people/mokush", "mokush_Realistic_Coffee_cup_-_Front_3D_view.svg");
		addSample("http://openclipart.org/media/files/Chrisdesign/3587", "glossy-buttons.svg");
		addSample("http://openclipart.org/media/files/Chrisdesign/9624", "tutanchamun.svg");
		addSample("http://openclipart.org/media/files/Chrisdesign/3727", "gibson-les-paul.svg");
		addSample("http://openclipart.org/media/files/yves_guillou/11115", "yves_guillou_sport_car_2.svg");
	}
	
	private void addSample(String originator, String resourceName) throws MalformedURLException
	{
		resources.add(new Resource(new URL(originator), getClass().getResource("/" + resourceName)));		
	}
	
	public Resource next()
	{
		if (resourceIterator == null || !resourceIterator.hasNext())
			resourceIterator = resources.iterator();
		Resource svgSample = resourceIterator.next();
		if (hyperLinkAction != null)
			hyperLinkAction.putValue(Action.NAME, svgSample.getOriginator().toExternalForm());
		return svgSample;
	}
	
	public void setForeignURL(URL url)
	{
		hyperLinkAction.putValue(Action.NAME, url.toExternalForm());
	}
	
	@SuppressWarnings("unchecked")
	public JButton createOriginatorHyperLinkButton()
	{
		hyperLinkAction = new AbstractAction("Xxxxxxxx")
		{
			public void actionPerformed(ActionEvent event)
			{
				try {
					Desktop.getDesktop().browse(new URI((String) hyperLinkAction.getValue(Action.NAME)));
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
			}
		};
		JButton hyperLinkButton = new JButton(hyperLinkAction);
		hyperLinkButton.setBorderPainted(false);
		hyperLinkButton.setContentAreaFilled(false);

		Map map = hyperLinkButton.getFont().getAttributes();
		map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
		map.put(TextAttribute.FOREGROUND, Color.BLUE);
		hyperLinkButton.setFont(new Font(map));
		hyperLinkButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return hyperLinkButton;
	}

	public JToolBar createNavigatorToolbar(Action nextAction)
	{
		JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
		JButton nextButton = new JButton(nextAction);
		nextButton.setContentAreaFilled(true);
		nextButton.setMargin(new Insets(0, 0, 0, 0));
		nextButton.setIcon(new ImageIcon(getClass().getResource("nav_forward.gif")));
		toolBar.add(nextButton);
		toolBar.addSeparator();
		return toolBar;
	}
	
	/**
	 * Creates a horizontal panel with the originator hyper link button 
	 * 
	 */
	public Component createOriginatorBar()
	{
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(createOriginatorHyperLinkButton());
		return box;
	}


	
}
