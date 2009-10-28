package se.bluebrim.view.example.dnd;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import se.bluebrim.view.TestOptionMenuBuilder;
import se.bluebrim.view.dnd.MouseEventDispatcher;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.select.StandardSelectionManager;
import se.bluebrim.view.swing.ViewPanel;
import se.bluebrim.view.tool.*;
import se.bluebrim.view.zoom.Scale;

/**
 * Test of drag and drop between two ViewPanels using a supervisor objects that
 * dispatch mouse events to the two ViewPanels in a way that enables them to
 * handle a drag and drop.
 * 
 * @author Goran Stack
 * 
 */
public class DndTest {

	private JFrame window;
	private ToolDispatcher[] toolDispatchers = new ToolDispatcher[4];

	public static void main(String[] args) {
		new DndTest().run();

	}

	private void run() 
	{
		window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setTitle("Drag and drop test");
		Container contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		MouseEventDispatcher mouseEventDispatcher = new MouseEventDispatcher(contentPane);
		
		contentPane.add(createViewPanel(0, "one", mouseEventDispatcher, true));
		contentPane.add(Box.createVerticalStrut(40));
		contentPane.add(createViewPanel(1, "two", mouseEventDispatcher, true));	
		contentPane.add(Box.createVerticalStrut(40));
		contentPane.add(createViewPanel(2, "three", mouseEventDispatcher, false));	
		contentPane.add(Box.createVerticalStrut(40));
		contentPane.add(createViewPanel(3, "four", mouseEventDispatcher, false));	
		contentPane.add(Box.createVerticalStrut(20));

		createTestOptionMenu(window);
		window.setContentPane(contentPane);
		window.pack();
		window.setLocation(50, 50);
		window.setSize(500, 700);
		window.setVisible(true);
	}
	
	private Component createViewPanel(int i, String text, MouseEventDispatcher mouseEventDispatcher, boolean withScroll)
	{
		ViewContext viewContext = new ViewContext(new Scale(), new Scale(), window);
		ViewPanel viewPanel = new TestViewPanel(null, viewContext);
		viewPanel.setName("ViewPanel: " + text);
		
		TestViewContainer testViewContainer = new TestViewContainer(viewContext);
		viewPanel.setRootView(testViewContainer);
		testViewContainer.setName(text);
		if (withScroll)
		{
			testViewContainer.setWidth(800);
			testViewContainer.setHeight(500);
		} else
		{
			testViewContainer.setWidth(500);
			testViewContainer.setHeight(100);
		}
		StringView stringView = new StringView(viewContext, "View number: " + text, testViewContainer);
		stringView.setX(10);
		stringView.setY(10);
		stringView.setWidth(110);
		stringView.setHeight(30);
		testViewContainer.addChild(stringView);
		
		StandardSelectionManager standardSelectionManager = new StandardSelectionManager(viewPanel);
		standardSelectionManager.setRootView(testViewContainer);
		viewContext.setSelectionManager(standardSelectionManager);

		toolDispatchers[i] = new SingleToolDispatcher(viewPanel, false);
		mouseEventDispatcher.addToolDispatcher(toolDispatchers[i]);
		
		if (withScroll)
		{
			JScrollPane scrollPane = new JScrollPane(viewPanel);
			scrollPane.setMinimumSize(new Dimension(300, 100));
			Rule columnHeader = new Rule(Rule.HORIZONTAL, true);
			columnHeader.setPreferredWidth(1000);
			scrollPane.setColumnHeaderView(columnHeader);
			Rule rowHeader = new Rule(Rule.VERTICAL, true);
			rowHeader.setPreferredHeight(1000);
			scrollPane.setRowHeaderView(rowHeader);
			viewPanel.addMouseListener(mouseEventDispatcher);
			viewPanel.addMouseMotionListener(mouseEventDispatcher);
//			viewPanel.setAutoscrolls(true);		
			return scrollPane;
		} else
			return viewPanel;
		
	}
	
	private void createTestOptionMenu(JFrame window)
	{
		JMenuBar menuBar = new JMenuBar();
		window.setJMenuBar(menuBar);
		JMenu testMenu = new JMenu("Test");
		menuBar.add(testMenu);
		TestOptionMenuBuilder testOptionMenuBuilder = new TestOptionMenuBuilder(window.getContentPane());
		testOptionMenuBuilder.addOption("Use MoveHandle", DragAndDropTool.USE_MOVE_HANDLE);
		testOptionMenuBuilder.addMenuItems(testMenu);
		
		JMenuItem menuItem = new JMenuItem("Inspect"); //$NON-NLS-1$
		testMenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				System.out.println("One current tool: " + toolDispatchers[0].getCurrentTool());
				System.out.println("Two current tool: " + toolDispatchers[1].getCurrentTool());
				System.out.println("Three current tool: " + toolDispatchers[2].getCurrentTool());
				System.out.println("Four current tool: " + toolDispatchers[3].getCurrentTool());
			}
		});

		
	}

	

}
