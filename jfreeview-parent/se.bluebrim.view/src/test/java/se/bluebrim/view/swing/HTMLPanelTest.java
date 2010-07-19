package se.bluebrim.view.swing;

import java.awt.Dimension;
import java.io.IOException;

import se.bluebrim.maven.plugin.screenshot.Screenshot;

public class HTMLPanelTest {

	
	@Screenshot (targetClass=HTMLPanel.class)
	public HTMLPanel createHTMLPanelWithSomeContent() throws IOException
	{
		HTMLPanel htmlPanel = new HTMLPanel(getClass().getResource("/test.html"), HTMLPanel.createOpenInBrowserHyperlinkListener());
		htmlPanel.setPreferredSize(new Dimension(400, 500));
		return htmlPanel;

	}


}
