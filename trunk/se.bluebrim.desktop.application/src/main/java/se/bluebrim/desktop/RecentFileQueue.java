package se.bluebrim.desktop;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import se.bluebrim.crud.client.command.DefaultAction;

/**
 * A RecentFileQueue keep a certain number of RecentFile objects in a list. The oldest
 * is removed when an add exceeds the maximum limit. The list of RecentFile files are read
 * from the file system at start up of an application and restored when the application
 * quits. Until Java 7 there is no perfect way to access the folder where this kind of
 * information should be stored. On Windows for example most programs use the
 * <code>Documents and Settings/current-user/Application Data</code> folder.
 * Read more about this issue in: http://forums.sun.com/thread.jspa?forumID=31&threadID=5219513
 * where I found the <code>System.getenv("APPDATA")</code> that will do for now.
 * 
 * @author GStack
 *
 */
public class RecentFileQueue
{		
	private int maxLimit;	
	private List<RecentFile> recentFiles;
	private File file;
	private DesktopApp desktopApp;

	/**
	 * 
	 * @param maxLimit Defines the the maximum number of recent files that is visible at the same 
	 * time in the Open Recent menu 
	 * @param applicationPath Defines the subdirectory path below the Application Data folder. Most programs
	 * use <company name>/<application name> for example <code>Adobe/Acrobat</code> or <code>Google/GoggleEarth</code>
	 */
	public RecentFileQueue(DesktopApp desktopApp, int maxLimit, String applicationPath)
	{
		this.desktopApp = desktopApp;
		this.maxLimit = maxLimit;
		recentFiles = new ArrayList<RecentFile>();
		String userData = System.getenv("APPDATA");

		file = new File(userData, applicationPath + "/recent-files.dat");
		System.out.println("Looking for stored recent files in: " + file.getAbsolutePath());
		// Ignore exceptions since its not that important to load recent files. 
		if (file.exists())
			try
			{
				readFromFile();
			} catch (FileNotFoundException e)
			{
			} catch (IOException e)
			{
			} catch (ClassNotFoundException e)
			{
			}
	}
	
	/**
	 * Ignore exceptions since its not that important to save recent files
	 */
	public void close()
	{
		try
		{
			writeToFile();
		} catch (FileNotFoundException e)
		{
		} catch (IOException e)
		{
		}
	}
	
	private void writeToFile() throws FileNotFoundException, IOException
	{
		file.getParentFile().mkdirs();
		file.createNewFile();
		ObjectOutput out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(recentFiles);
		out.close();
	}
	
	@SuppressWarnings("unchecked")
	private void readFromFile() throws FileNotFoundException, IOException, ClassNotFoundException
	{
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		recentFiles = ( List<RecentFile>) in.readObject();
    in.close();		
	}
	
	public void addRecentFile(FileEditor fileEditor, File file)
	{
		openRecentFile(fileEditor, new RecentFile(file, fileEditor.getTumbnailSource()));
	}
	
	/**
	 * If the recent file already exist move it to the end otherwise add the specified
	 * RecentFile to the end of the list. Remove the first element if limit is exceeded.
	 */
	public void openRecentFile(FileEditor fileEditor, RecentFile recentFile)
	{
		if (recentFiles.contains(recentFile))
			recentFiles.remove(recentFile);
		recentFiles.add(recentFile);
		if (recentFiles.size() > maxLimit)
			recentFiles.remove(0);
		desktopApp.updateRecentFilesMenu();
	}
	
	private void clear()
	{
		recentFiles.clear();
		desktopApp.updateRecentFilesMenu();
	}
	
	private class ClearRecentFilesAction extends DefaultAction
	{
		public ClearRecentFilesAction()
		{
			super("Clear", new ClearRecentFileMenuIcon(RecentFileQueue.class.getResource("clear.gif")));
		}

		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			clear();			
		}		
	}
	
	/**
	 * Center the small icon in a larger area to harmonize with icons of the Recent File menu items.
	 *
	 */
	private class ClearRecentFileMenuIcon implements Icon
	{
		private ImageIcon icon;
		
		public ClearRecentFileMenuIcon(URL url)
		{
			this.icon = new ImageIcon(url);
		}

		@Override
		public int getIconHeight()
		{
			return (int) RecentFile.ICON_HEIGHT;
		}

		@Override
		public int getIconWidth()
		{
			return (int) RecentFile.ICON_WIDTH;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			Graphics2D g2d = (Graphics2D)g;
			g2d.translate(x, y);
			g2d.drawImage(icon.getImage(), AffineTransform.getTranslateInstance((getIconWidth() - icon.getIconWidth()) / 2.0, (getIconHeight() - icon.getIconHeight()) / 2.0), null);
			g2d.translate(-x, -y);			
		}
		
	}

	/**
	 * Iterate in reverse order to get the last recent file at the top of the menu
	 */
	public void createRecentFileMenuItems(FileEditor fileEditor, JMenu recentFilesMenu)
	{
		if (recentFiles.isEmpty())
		{
			JMenuItem menuItem = new JMenuItem("(none)");
			menuItem.setEnabled(false);
			recentFilesMenu.add(menuItem);
		} else
		{
			for (int i = recentFiles.size() - 1; i >= 0; i--)
			{
				RecentFile recentFile = recentFiles.get(i);
				recentFilesMenu.add(new JMenuItem(new FileEditor.OpenRecentFileAction(fileEditor, recentFile)));
			}
			recentFilesMenu.add(new JMenuItem(new ClearRecentFilesAction()));
		}
			
	}

	public void removeRecentFile(RecentFile recentFile)
	{
		recentFiles.remove(recentFile);
		desktopApp.updateRecentFilesMenu();		
	}

	
}
