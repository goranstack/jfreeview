package se.bluebrim.isac.desktop;

import java.awt.Composite;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.CSSUtilities;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import se.bluebrim.view.TransferableView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.paint.Paintable;

/**
 * Renders a SVG document from the specified URL.<br>
 * Nice SVG art can be found at: http://www.opensecurityarchitecture.org/cms/library/icon-library
 * 
 * @author GStack
 * 
 */
public class SvgView extends AbstractImageView implements TransferableView, PropertyPersistableView
{
	private GraphicsNode graphicsNode;
	
	public SvgView(ViewContext viewContext, URL url, String propertyKey)
	{
		super(viewContext, url, propertyKey);
	}

	@Override
	protected Rectangle2D loadImage(URL url)
	{
		try
		{
			return tryLoadImage1(url);
		}
		catch (IOException e)
		{
			// TODO: Generate a broken image instead
			throw new RuntimeException(e);
		}
		catch (URISyntaxException e)
		{
			// TODO: Generate a broken image instead
			throw new RuntimeException(e);
		}				
	}
	
	/**
	 * From: http://forums.sun.com/thread.jspa?threadID=5320479
	 */
	private Rectangle2D tryLoadImage1(URL url) throws IOException, URISyntaxException
	{
		String xmlParser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory df = new SAXSVGDocumentFactory(xmlParser);
		SVGDocument doc = df.createSVGDocument(url.toURI().toString());
		UserAgent userAgent = new UserAgentAdapter();
		DocumentLoader loader = new DocumentLoader(userAgent);
		BridgeContext bridgeContext = new BridgeContext(userAgent, loader);
		bridgeContext.setDynamicState(BridgeContext.DYNAMIC);
		GVTBuilder builder = new GVTBuilder();
		graphicsNode = builder.build(bridgeContext, doc);
		return graphicsNode.getPrimitiveBounds();		
	}

	

	@Override
	public Object getModel()
	{
		return graphicsNode;
	}

	@Override
	protected void paintImage(Paintable g)
	{
		Composite composite = g.getGraphics().getComposite();
		g.getGraphics().setComposite(CSSUtilities.TRANSPARENT);
		graphicsNode.primitivePaint(g.getGraphics());
		g.getGraphics().setComposite(composite);
	}
	
}