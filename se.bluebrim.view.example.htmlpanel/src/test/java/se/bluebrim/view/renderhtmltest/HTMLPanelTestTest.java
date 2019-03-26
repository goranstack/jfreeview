package se.bluebrim.view.renderhtmltest;

import java.awt.Dimension;
import java.io.IOException;

import se.bluebrim.view.swing.HTMLPanel;

public class HTMLPanelTestTest {

	
	public HTMLPanel createHTMLPanelWithSomeContent() throws IOException
	{
		HTMLPanel htmlPanel = new HTMLPanel(getClass().getResource("/test.html"), HTMLPanel.createOpenInBrowserHyperlinkListener());
		htmlPanel.setPreferredSize(new Dimension(400, 500));
		return htmlPanel;

	}


}
