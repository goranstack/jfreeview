package se.bluebrim.view.example.svg.rasterizer;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.batik.transcoder.TranscoderException;
import org.junit.Test;

import se.bluebrim.maven.plugin.screenshot.Screenshot;

public class SVGRasterizerTestTest {

	@Test
	public void testMain() throws IOException, TranscoderException {
		SVGRasterizerTest.main(null);
	}
	
	@Screenshot (targetClass=SVGRasterizerTest.class)
	public JComponent createScreenshot() throws MalformedURLException
	{
		JPanel panel= new JPanel();
		new SVGRasterizerTest().addContent(panel);
		return panel;
	}

}
