package se.bluebrim.view.dependencygraph;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;

import nu.esox.gui.aspect.EnablePredicateAdapter;
import nu.esox.gui.layout.ColumnLayout;
import nu.esox.gui.layout.RowLayout;
import nu.esox.gui.list.ListSelectionPredicate;

import org.apache.commons.lang.mutable.MutableInt;

import se.bluebrim.crud.client.AbstractCrudPanel;
import se.bluebrim.crud.client.AbstractPanel;
import se.bluebrim.crud.client.UiUtil;
import se.bluebrim.crud.client.command.DefaultAction;
/**
 * Panel that let the user selects a collection of files/directories by drag and drop
 * or with the file chooser. The model of the panel is a collection of files.
 * 
 * @author GStack
 *
 */
public class FileListPanel extends AbstractPanel
{
	private String labelText;
	private JFileChooser fileChooser;
	private FileListModel fileListModel;
	private JList fileListComponent;
	private JButton clearButton;
	private JButton addButton;
	private JButton removeButton;
	private Action removeAction;
	private List<File> files;

	public FileListPanel()
	{
		this("Select one or more files/directories", new ArrayList<File>(), new JFileChooser());
	}
	
	public FileListPanel(String labelText, List<File> files, JFileChooser fileChooser)
	{
		this.labelText = labelText;
		this.files = files;
		this.fileChooser = fileChooser;
		initComponents();
		arrangeLayout();
		createBindings();
	}
	
	
	private void createBindings()
	{
		fileListModel = new FileListModel();
		fileListComponent.setModel(fileListModel);
		DropTarget dropTarget = new DropTarget(fileListComponent, TransferHandler.COPY_OR_MOVE, new FileDropListener());
		fileListComponent.setDropTarget(dropTarget);
		clearButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fileListModel.clear();
			}
		});

		addButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int result = fileChooser.showDialog(FileListPanel.this, "Select");
				if (result != JFileChooser.APPROVE_OPTION)
					return;
				File[] files = fileChooser.getSelectedFiles();
				for (int i = 0; i < files.length; i++)
				{
					fileListModel.addFile(files[i]);
				}
			}
		});
		new EnablePredicateAdapter(null, null, removeAction, null, new ListSelectionPredicate(fileListComponent.getSelectionModel(), ListSelectionPredicate.TEST_SOME));		
	}

	private void initComponents()
	{
		fileListComponent = new JList();
		clearButton = new JButton("Clear");		
		addButton = new JButton("Add");		
		removeButton = new JButton(removeAction = new RemoveAction());		
	}

	private void arrangeLayout()
	{
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JPanel northPanel = new JPanel(new ColumnLayout());		
		add(new JScrollPane(fileListComponent));
		JPanel buttonPanel = new JPanel(new RowLayout(5, true, RowLayout.LEFT));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);
		buttonPanel.add(clearButton);
		northPanel.add(new JLabel(labelText));
		northPanel.add(buttonPanel);
		add(northPanel, BorderLayout.NORTH);
	}


	private class FileListModel extends AbstractListModel
	{
		
		public FileListModel()
		{
		}
		
		public int getSize()
		{
			return files.size();
		}

		public Object getElementAt(int index)
		{
			return files.get(index).getName();
		}
		
		private void addFile(File file)
		{
			if (!files.contains(file))
			{
				files.add(file);
				fireIntervalAdded(this, files.size()-1, files.size());
			}
		}
		
		private void removeFile(int[] indices)
		{
			List<Object> toBeRemoved = new ArrayList<Object>();
			for (int i = 0; i < indices.length; i++)
			{
				toBeRemoved.add(files.get(indices[i]));
			}		
			files.removeAll(toBeRemoved);
			fireIntervalRemoved(this, 0, files.size());
		}
		
		
		public void clear()
		{
			int size = files.size();
			files.clear();
			fireIntervalRemoved(this, 0, size);
		}		
	}
	
	private class FileDropListener extends DropTargetAdapter
	{

		@SuppressWarnings("unchecked")
		public void drop(DropTargetDropEvent dropTargetDropEvent)
		{
//			printFlavours(dropTargetDropEvent);
			try
			{
				Transferable tr = dropTargetDropEvent.getTransferable();
				if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
					dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_REFERENCE);
					List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
					Iterator<File> iterator = fileList.iterator();
					while (iterator.hasNext())
					{
						File file = iterator.next();
						if (fileChooser.getFileFilter().accept(file))
							FileListPanel.this.fileListModel.addFile(file);
					}
					dropTargetDropEvent.getDropTargetContext().dropComplete(true);
				} else
				{
					dropTargetDropEvent.rejectDrop();
				}
			} catch (IOException io)
			{
				io.printStackTrace();
				dropTargetDropEvent.rejectDrop();
			} catch (UnsupportedFlavorException ufe)
			{
				dropTargetDropEvent.rejectDrop();
			}
		}		
	}
	
	/**
	 * For debugging purposes. A breakpoint during drag and drop will hang the application.
	 */
//	private void printFlavours(DropTargetDropEvent dropTargetDropEvent)
//	{
//		System.out.println("Data flavors ");
//		List<DataFlavor> list = Arrays.asList(dropTargetDropEvent.getTransferable().getTransferDataFlavors());
//		for (DataFlavor dataFlavor : list)
//		{
//			System.out.println(dataFlavor.getMimeType());
//		}				
//	}

	public List<File> getFiles()
	{
		return files;
	}
	
	public int openInModalDialog(Frame owner, String dialogTitle)
	{
		final MutableInt result = new MutableInt(JOptionPane.CANCEL_OPTION);
		final JDialog dialog = new JDialog(owner, dialogTitle, true);
		Container contentPane = dialog.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(this, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new RowLayout(5, true, RowLayout.RIGHT));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonPanel.add( new JButton(new AbstractAction("OK"){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				result.setValue(JOptionPane.OK_OPTION);	
				dialog.setVisible(false);
			}}));
		buttonPanel.add( new JButton(new AbstractAction("Cancel"){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				result.setValue(JOptionPane.CANCEL_OPTION);				
				dialog.setVisible(false);
			}}));
		
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setLocationByPlatform(true);
		dialog.setVisible(true);
		return result.intValue();
	}

	private class RemoveAction extends DefaultAction
	{

		public RemoveAction()
		{
			super("Remove", UiUtil.getIcon("delete.gif", AbstractCrudPanel.class));
		}
		
		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			int[] selectedFiles = fileListComponent.getSelectedIndices();
			fileListModel.removeFile(selectedFiles);						
		}
		
	}

}
