package se.bluebrim.desktop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JMenu;

import se.bluebrim.crud.client.command.CursorHandler;
import se.bluebrim.crud.client.command.DefaultAction;
import se.bluebrim.crud.client.command.DefaultCommand;
import se.bluebrim.crud.client.command.DefaultExceptionHandler;


/**
 * Abstract super class to a standard desktop application working with files containing
 * a serialized model. DesktopApp supports multiple windows and to make that work you need
 * to subclass the FileEditor as well. A FileEditor is created for each window. See the
 * example in <code>scr-example</code> folder <br>
 * 
 * To do:
 * 
 * <ul>
 * <li>Update model with current text editing when saving. This is not happening since a menu choice 
 * only creates a temporary focus lost which is not handled in the TextFieldFocusHandler<li>
 * <li>Saving with CTRL-S do not reset dirty flag</li>
 * </ul>
 * 
 * @author GStack
 *
 */
public abstract class DesktopApp implements CursorHandler
{
	public static final ResourceBundle DESKTOP_APP_BUNDLE = ResourceBundle.getBundle(DesktopApp.class.getPackage().getName() + ".desktopApp");
	
	protected List<FileEditor> fileEditors;		// Application exit when last window is closed.
	private RecentFileQueue recentFileQueue;

	public DesktopApp()
	{
		super();
		fileEditors = new ArrayList<FileEditor>();
		recentFileQueue = new RecentFileQueue(this, 8, getApplicationPath());
		DefaultExceptionHandler exceptionHandler = new DefaultExceptionHandler(null);
		DefaultAction.setDefaultExceptionHandler(exceptionHandler);
		DefaultCommand.setDefaultExceptionHandler(exceptionHandler);
		DefaultAction.setDefaultCursorHandler(this);
		DefaultCommand.setDefaultCursorHandler(this);
	}
	
	/**
	 * @return A subdirectory path to be used in the Application Data folder. Most programs
	 * use <company name>/<application name> for example <code>Adobe/Acrobat</code> or <code>Google/GoggleEarth</code>
	 */
	protected abstract String getApplicationPath();
				
	
	public void add(FileEditor fileEditor)
	{
		fileEditors.add(fileEditor);
	}
	
	public void remove(FileEditor fileEditor)
	{
		fileEditors.remove(fileEditor);
		if (fileEditors.size() < 1)
		{
			exit();
		}
		else					
			fileEditor.closeWindow();
	}
	
	public void addRecentFileMenuItems(FileEditor fileEditor, JMenu recentFilesMenu)
	{
		recentFileQueue.createRecentFileMenuItems(fileEditor, recentFilesMenu);		
	}

	public void addRecentFile(FileEditor fileEditor, File file)
	{
		recentFileQueue.addRecentFile(fileEditor, file);
	}
	
	public void removeRecentFile(RecentFile recentFile)
	{
		recentFileQueue.removeRecentFile(recentFile);		
	}
	
	
	public void openRecentFile(FileEditor fileEditor, RecentFile recentFile)
	{
		recentFileQueue.openRecentFile(fileEditor, recentFile);
	}

	@Override
	public void setWaitCursor()
	{
		for (FileEditor fileEditor : fileEditors)
		{
			fileEditor.setWaitCursor();
		}		
	}
	
	@Override
	public void resetWaitCursor()
	{
		for (FileEditor fileEditor : fileEditors)
		{
			fileEditor.resetWaitCursor();
		}	
	}

	public void exit()
	{
		recentFileQueue.close();
		System.exit(0);		
	}

	public void updateRecentFilesMenu()
	{
		for (FileEditor fileEditor: fileEditors)
		{
			fileEditor.updateRecentFileMenu();
		}		
	}


}