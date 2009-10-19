package se.bluebrim.desktop.jdbc.connect;

import java.awt.BorderLayout;

import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

import nu.esox.gui.TextFieldFocusHandler;
import nu.esox.gui.aspect.TextFieldAdapter;
import se.bluebrim.crud.client.DirtyPredicatePanel;
import se.bluebrim.crud.client.FormPanel;
import se.bluebrim.crud.client.esox.XFormattedTextFieldAdapter;

/**
 * 
 * @author GStack
 *
 */
public class JdbcConnectionPanel extends DirtyPredicatePanel
{
	private JTextField hostName;
	private JFormattedTextField port;
	private JTextField databaseName;
	private JTextField userName;
	private JTextField password;
	

	public JdbcConnectionPanel()
	{
		initComponents();
		arrangeLayout();
		createBindings();
	}

	public JdbcConnectionPanel(JdbcConnectionModel model)
	{
		this();
		setModel(model);
	}

	private void initComponents()
	{
		hostName = new JTextField(30);
		port = createIntegerIdField(8);
		databaseName = new JTextField(30); 
		userName = new JTextField(30);
		password = new JTextField(30);
	}

	private void arrangeLayout()
	{
		setLayout(new BorderLayout());
		FormPanel form = new FormPanel();
		form.addFormRow("Host name", hostName);
		form.addFormRow("Port", port);
		form.addFormRow("Database name", databaseName);
		form.addFormRow("User name", userName);
		form.addFormRow("Password", password);
		form.adjustLabelWidthsToLargest();
		add(form, BorderLayout.CENTER);
	}

	private void createBindings()
	{
		new TextFieldAdapter(hostName, this, JdbcConnectionModel.class, "getHostName", "setHostName", null);
		new TextFieldAdapter(databaseName, this, JdbcConnectionModel.class, "getDatabaseName", "setDatabaseName", null);
		new TextFieldAdapter(userName, this, JdbcConnectionModel.class, "getUserName", "setUserName", null);
		new TextFieldAdapter(password, this, JdbcConnectionModel.class, "getPassword", "setPassword", null);
		new XFormattedTextFieldAdapter(port, this, JdbcConnectionModel.class, "getPort", "setPort", Integer.class, null, null);
		TextFieldFocusHandler.add(hostName);
		TextFieldFocusHandler.add(databaseName);
		TextFieldFocusHandler.add(userName);
		TextFieldFocusHandler.add(password);
	}
}
