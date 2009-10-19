package se.bluebrim.desktop;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import nu.esox.gui.aspect.EnablePredicateAdapter;
import nu.esox.util.AndPredicate;
import nu.esox.util.ObservableEvent;
import nu.esox.util.ObservableListener;
import nu.esox.util.Predicate;
import nu.esox.util.PredicateIF;
import se.bluebrim.crud.client.DirtyPredicateProvider;
import se.bluebrim.crud.client.command.CursorHandler;
import se.bluebrim.crud.client.command.DefaultAction;
import se.bluebrim.crud.client.command.DefaultForegroundCommand;
import se.bluebrim.crud.client.command.WaitCursorFrame;
import se.bluebrim.crud.esox.DirtyPredicateModel;

/**
 * Abstract superclass to objects that creates a window containing a panel
 * for editing a model and menus for actions on the model.
 * 
 * @author GStack
 *
 */
public abstract class FileEditor implements CursorHandler
{
	protected File file;
	private DirtyPredicateProvider dirtyPredicateProvider;
	private DirtyPredicateModel model;
	protected DesktopApp desktopApp;
	protected WaitCursorFrame window;
	private PredicateIF dirtyPredicate;	
	protected Predicate hasModelPredicate;	
	private JFileChooser fileChooser;
	private JMenu recentFilesMenu;
		
	public FileEditor(DirtyPredicateModel model, DesktopApp desktopApp)
	{
		super();
		hasModelPredicate = new Predicate(false);
		setModel(model);
		this.desktopApp = desktopApp;
	}
	
	/**
	 * The design of this method enables construction of mutual dependent window and panel
	 */
	public final void createWindow()
	{
		window = new WaitCursorFrame();
		createWindowContent(window);
		dirtyPredicateProvider = getDirtyPredicateProvider();
		dirtyPredicateProvider.initDirtyPredicatePanel();
		dirtyPredicate = dirtyPredicateProvider.getDirtyPredicate();
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setWindowTitle();
		window.setIconImage(getIcon());
		addWindowTitleListeners();
		createMenuBar();
		addWindowCloseListener();
		layoutWindow();
		window.setVisible(true);
		desktopApp.add(this);
	}
	
	protected void layoutWindow()
	{
		window.pack();
		window.setLocationByPlatform(true);		
	}
	
	public void setWaitCursor()
	{
		window.setWaitCursor();
	}
	
	public void resetWaitCursor()
	{
		window.resetWaitCursor();
	}
	
	protected abstract void createWindowContent(JFrame frame);
	
	protected abstract DirtyPredicateProvider getDirtyPredicateProvider();

	protected abstract Image getIcon();
	
	private void setWindowTitle()
	{
		String changeIndicator = isDirty() ? "*" :"";
		window.setTitle(changeIndicator + getWindowTitle());
	}

	/**
	 * Add listeners that update the window title when the file content name is changed
	 * or the file content gets dirty.
	 */
	private void addWindowTitleListeners()
	{
		ObservableListener observableListener = new ObservableListener(){
		
					@Override
					public void valueChanged(ObservableEvent e)
					{
						setWindowTitle();						
					}
		};					
		dirtyPredicate.addObservableListener(observableListener);
	}
	
	protected abstract void createMenuBar();

	protected abstract FileEditor readModel(File file) throws Exception;

	protected abstract FileEditor openNew();


	private JFileChooser getFileChooser()
	{
		if (fileChooser == null)
		{
			fileChooser = createFileChooser();
		}
		return fileChooser;
	}

	protected JFileChooser createFileChooser()
	{
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileFilter(){

			@Override
			public boolean accept(File file)
			{
				return file.isDirectory() || file.getName().endsWith(getFileExtension());
			}

			@Override
			public String getDescription()
			{
				return getFileFilterDescription();
			}});
		return fileChooser;
	}
	
	private void addWindowCloseListener()
	{
		window.addWindowListener(new WindowAdapter()
		{
			/**
			 * Invoked when the user attempts to close the window from the window's
			 * system menu. If the program does not explicitly hide or dispose the
			 * window while processing this event, the window close operation will be
			 * canceled.
			 */
			public void windowClosing(WindowEvent e)
			{
				if (saveIfUserWantsOrCancel())
				{
					desktopApp.remove(FileEditor.this);
				}
			}
		});
	}
	
	private boolean saveIfUserWantsOrCancel()
	{
		if (isDirty())
		{
			int result = JOptionPane.showConfirmDialog(window, "<html>Vill du spara ändringar i <strong>"
					+ getFileName() + "</strong> innan fönstret stängs?</html>");
			switch (result)
			{
			case JOptionPane.OK_OPTION:
			{
				new SaveCommand(this).run();
				return true;
			}
			case JOptionPane.CANCEL_OPTION:
				return false;

			case JOptionPane.NO_OPTION:
				return true;
			}
		}
		return true;
	}
	

	protected String getFileName()
	{
		return file != null ? file.getName() : getFileContentName();
	}

	/**
	 * Override this to perform tasks before saving for example updating
	 * the model with the file name
	 */
	protected void aboutToSave(File file)
	{
	}

	protected void boundTo(File file)
	{
		this.file = file;
		cleanDirty();
		if (window != null)
			setWindowTitle();
	}

	private boolean isDirty()
	{
		// TODO: Should not be necessary to test for null
		return dirtyPredicate == null ? false : dirtyPredicate.isTrue();
	}
	
	private String getWindowTitle()
	{
		return file != null ? file.getName() : getFileContentName();
	}

	private void cleanDirty()
	{
		model.cleanDirty();
		if (dirtyPredicateProvider != null)
			dirtyPredicateProvider.cleanDirty();
	}

	/**
	 * 
	 * @return a name that can be used as file name together with the extension
	 */
	protected abstract String getFileContentName();

	protected abstract String getFileExtension();

	protected abstract String getFileFilterDescription();

	protected abstract void writeToStream(FileOutputStream stream);
	
	protected void createMenuItemForQuitWithoutSaving(JMenu fileMenu)
	{
		fileMenu.add(new QuitWithoutSavingAction());
	}

	protected void createMenuItemForSaveAs(JMenu fileMenu)
	{
		fileMenu.add(new SaveAsAction(this));
	}

	protected void createMenuItemForSave(JMenu fileMenu)
	{
		fileMenu.add(new SaveAction(this));
	}

	protected void createMenuItemForOpen(JMenu fileMenu)
	{
		fileMenu.add(new OpenFileAction(this));
	}

	protected void createMenuItemForNew(JMenu fileMenu)
	{
		fileMenu.add(new OpenNewAction());
	}
	
	protected void createMenuItemForRecentFiles(FileEditor fileEditor, JMenu fileMenu)
	{
		fileMenu.add(recentFilesMenu = new JMenu("Open Recent"));
		fileEditor.updateRecentFileMenu();
	}

	protected void updateRecentFileMenu()
	{
		if (recentFilesMenu != null)
		{
			recentFilesMenu.removeAll();
			desktopApp.addRecentFileMenuItems(this, recentFilesMenu);
		}
	}

	private static abstract class AbstractFileAction extends DefaultAction
	{
		protected FileEditor fileEditor;

		public AbstractFileAction(String localizedName, FileEditor fileEditor)
		{
			super(localizedName);
			this.fileEditor = fileEditor;
		}	
	}
	
	private static class OpenFileAction extends AbstractFileAction
	{
		
		public OpenFileAction(FileEditor fileEditor)
		{
			super("Open...", fileEditor);
			setAccelerator(KeyStroke.getKeyStroke("control O"));
		}

		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			int result = fileEditor.getFileChooser().showOpenDialog(fileEditor.window);
			if (result != JFileChooser.APPROVE_OPTION)
				return;
			File file = fileEditor.getFileChooser().getSelectedFile();
			new OpenFileCommand(fileEditor, file).run();
		}		
	}
	
	public static class OpenFileCommand extends DefaultForegroundCommand
	{
		private FileEditor fileEditor;
		private File file;
		
		public OpenFileCommand(FileEditor fileEditor, File file)
		{
			super();
			this.fileEditor = fileEditor;
			this.file = file;
		}


		@Override
		public void tryRun() throws Exception
		{
			FileEditor createdFileEditor = fileEditor.readModel(file);
			fileEditor.desktopApp.addRecentFile(createdFileEditor, file);			
		}
		
	}
	
	public static class OpenRecentFileAction extends AbstractFileAction
	{
		private RecentFile recentFile;
		
		public OpenRecentFileAction(FileEditor fileEditor, RecentFile recentFile)
		{
			super(recentFile.getName(), fileEditor);
			setSmallIcon(recentFile);
			setShortDescription("<html>Open file:<br>" + recentFile.getFile().getAbsolutePath() + "</html>");
			this.recentFile = recentFile;
		}
		
		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			File fileToOpen = recentFile.getFile();
			if (fileToOpen.exists())
			{
				FileEditor createdFileEditor = fileEditor.readModel(fileToOpen);
				fileEditor.desktopApp.openRecentFile(createdFileEditor, recentFile);
			} else
			{
				JOptionPane.showMessageDialog(fileEditor.window, "<html>The file:<br><strong>" + fileToOpen.getAbsolutePath() + "</strong><br>has been moved or renamed</html>");
				fileEditor.desktopApp.removeRecentFile(recentFile);
			}
		}		
		
	}
	
	private class OpenNewAction extends DefaultAction
	{
		public OpenNewAction()
		{
			super("New...");
			setAccelerator(KeyStroke.getKeyStroke("control N"));
			setSmallIcon(new ImageIcon(DesktopApp.class.getResource("new.gif")));
		}

		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			openNew();			
		}		
	}
	
	private class QuitWithoutSavingAction extends DefaultAction
	{
		public QuitWithoutSavingAction()
		{
			super("Quit");
			setAccelerator(KeyStroke.getKeyStroke("control Q"));
			dirtyPredicate.addObservableListener(new ObservableListener(){

				@Override
				public void valueChanged(ObservableEvent e)
				{
					setActionName(dirtyPredicate.isTrue() ? "Quit without saving" : "Quit");				
				}});
		}

		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			desktopApp.exit();
		}	

	}

		
	/**
	 * Abstract superclass to the Save and the Save as action
	 *
	 */
	private static abstract class AbstractSaveAction extends AbstractFileAction
	{
		
		public AbstractSaveAction(String localizedName, FileEditor fileEditor)
		{
			super(localizedName, fileEditor);
		}
		
		@Override
		protected abstract void execute(ActionEvent evt) throws Exception;
									
	}

	private static class SaveAction extends AbstractSaveAction
	{

		public SaveAction(FileEditor fileEditor)
		{
			super("Save...", fileEditor);
			setAccelerator(KeyStroke.getKeyStroke("control S"));
			setSmallIcon(new ImageIcon(DesktopApp.class.getResource("save.gif")));
			new EnablePredicateAdapter(null, null, this, null, new AndPredicate(fileEditor.hasModelPredicate, fileEditor.dirtyPredicate));
		}
		
		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			new SaveCommand(fileEditor).run();
		}		
	}
	
	private static class SaveAsAction extends AbstractSaveAction
	{

		public SaveAsAction(FileEditor fileEditor)
		{
			super("Save as...", fileEditor);
			setSmallIcon(new ImageIcon(DesktopApp.class.getResource("saveas.gif")));
			new EnablePredicateAdapter(null, null, this, null, fileEditor.hasModelPredicate);
		}
		
		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			new SaveAsCommand(fileEditor).run();
		}		
	}
	
	/**
	 * Abstract superclass to the Save and the Save as action
	 *
	 */
	private static abstract class AbstractSaveCommand extends DefaultForegroundCommand
	{
		protected FileEditor fileEditor;
		
		public AbstractSaveCommand(FileEditor fileEditor)
		{
			this.fileEditor = fileEditor;
		}
				
		private String getNewFileName()
		{
			String fileName = fileEditor.getFileContentName();
			if (fileName.endsWith("." + fileEditor.getFileExtension()))
				return fileName;
			else
				return fileName + "." + fileEditor.getFileExtension();
		}



		/**
		 * This method is named after the common menu item with the same name
		 * that most application with file storage have. It gives the user a
		 * possibility to save the data under a different name. But the user
		 * is not prohibited to choose the file that the data was loaded from and
		 * if he do so, he will get the question: 
		 * "File already exists. Do you want to replace it?"
		 */
		protected void saveAs() throws IOException, RuntimeException
		{
			File file = chooseFileForSaving(fileEditor.window, fileEditor);
			if (file == null)
				return;
			else if (userConfirmsOverwriteIfExists(file, fileEditor.window))
				save(file);
		}
		
		private File chooseFileForSaving(JFrame window, FileEditor fileEditor) 
		{		
			File file = fileEditor.file;
			JFileChooser fileChooser = fileEditor.getFileChooser();
			if (file == null)
				fileChooser.setSelectedFile(new File(getNewFileName()));
			else
				fileChooser.setCurrentDirectory(file.getParentFile());
			int result = fileChooser.showSaveDialog(window);
			if (result != JFileChooser.APPROVE_OPTION)
				return null;
			else 
				return fileChooser.getSelectedFile();
		}
		
		
		/**	
		 * First the data is written to a temporary file and then renamed to the specified file.
		 * If any exception occurs during the execution of writeToStream, the renaming operation
		 * is not performed, and thus does not corrupt the specified file.
		 * This operation can generate more than one file when for example when a model is exporting
		 * XML with images that is written to separate files. Use the saveZip-method if you want a single file
		 * in these cases.
		 */
		protected void save(File file) throws IOException, RuntimeException 
		{
			System.out.println("Saving file: " + file.getAbsolutePath());
			File tempFile = File.createTempFile(fileEditor.getFileContentName(), null, file.getParentFile());
			tempFile.deleteOnExit();
			FileOutputStream stream = new FileOutputStream(tempFile);
			try {
				fileEditor.aboutToSave(file);
				fileEditor.writeToStream(stream);
			} catch (Exception e) {
				stream.close();
				tempFile.delete();
				throw new RuntimeException(e);
			}
			stream.close();
			renameTempFile(file, tempFile);
			fileEditor.boundTo(file);
			fileEditor.desktopApp.addRecentFile(fileEditor, file);
		}
		
		/**
		 * This method is called with prior confirmation from the user that
		 * it is ok to replace the file.
		 */
		private void renameTempFile(File file, File tmpFile) throws IOException {
			if (file.exists())
				if (!file.delete())
					throw new IOException("Unable to delete: " + file.getAbsolutePath());
			if (!tmpFile.renameTo(file))
				throw new IOException("Unable to rename: " + tmpFile.getAbsolutePath() + " to " + file.getAbsolutePath());
		}
	}
	
	private static class SaveCommand extends AbstractSaveCommand
	{

		public SaveCommand(FileEditor fileEditor)
		{
			super(fileEditor);
		}
		
		@Override
		protected void tryRun() throws Exception
		{
			if (fileEditor.file == null)
				saveAs(); 
			 else
				save(fileEditor.file);	
		}		
	}
	
	private static class SaveAsCommand extends AbstractSaveCommand
	{

		public SaveAsCommand( FileEditor fileEditor)
		{
			super(fileEditor);
		}
		
		@Override
		protected void tryRun() throws Exception
		{
			saveAs(); 
		}		
	}
	
	protected static class ShowHtmlWindowAction extends DefaultAction
	{
		private URL url;
		private Rectangle bounds;
		private String windowTitle;
		private Image icon;
		
		public ShowHtmlWindowAction(String actionName, URL url, Rectangle bounds, String windowTitle, Image icon)
		{
			super(actionName);
			this.url = url;
			this.bounds = bounds;
			this.windowTitle = windowTitle;
			this.icon = icon;
		}

		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			JFrame frame = new JFrame(windowTitle);
			JEditorPane editorPane = new JEditorPane(url)
			{
				protected void paintComponent(Graphics g)
				{
					((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					super.paintComponent(g);
				}
			};
			editorPane.setEditable(false);
			frame.getContentPane().add(new JScrollPane(editorPane));
			frame.setBounds(bounds);
			frame.setIconImage(icon);
			frame.setVisible(true);
		}		
	}

	public void setModel(DirtyPredicateModel model)
	{
		this.model = model;
		hasModelPredicate.set(model != null);
	}

	public JFrame getWindow()
	{
		return window;
	}
	
	public void closeWindow()
	{
		window.dispose();
	}

	/**
	 * Return a component suitable as thumbnail source. 
	 */
	public abstract JComponent getTumbnailSource();

}
