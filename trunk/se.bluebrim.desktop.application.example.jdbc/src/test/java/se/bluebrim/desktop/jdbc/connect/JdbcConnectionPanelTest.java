package se.bluebrim.desktop.jdbc.connect;

import java.util.List;

import org.junit.Test;

import se.bluebrim.crud.client.AbstractPanel;
import se.bluebrim.crud.client.AbstractPanelTest;

/**
 * 
 * @author GStack
 *
 */
public class JdbcConnectionPanelTest extends AbstractPanelTest
{
	public static void main(String[] args)
	{
		new JdbcConnectionPanelTest().testCreateGui();
	}
	
	@Test
	public void testCreateGui()
	{
		List<AbstractPanel> panels = openTestWindow(JdbcConnectionPanel.class);
		JdbcConnectionModel jdbcConnection = new JdbcConnectionModel();
		((AbstractPanel)panels.get(0)).setModel(jdbcConnection);
		((AbstractPanel)panels.get(1)).setModel(jdbcConnection);		
	}

}
