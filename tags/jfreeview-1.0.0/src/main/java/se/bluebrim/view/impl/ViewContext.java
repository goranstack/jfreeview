package se.bluebrim.view.impl;

import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import se.bluebrim.view.View;
import se.bluebrim.view.model.ObservableModel;
import se.bluebrim.view.select.NullSelectionManager;
import se.bluebrim.view.select.SelectionManager;
import se.bluebrim.view.tool.ToolDispatcher;
import se.bluebrim.view.transaction.MockTransaction;
import se.bluebrim.view.transaction.Transaction;
import se.bluebrim.view.transaction.TransactionManager;
import se.bluebrim.view.zoom.Scale;

/**
 * Properties accessible for all views in a view hierarchy. Some properties used in the paint method is also needed in other methods. For example isVisable needs
 * to know the detail level.
 * TODO: Split into several objects since some aspects could be shared among several ViewPanels but others should not be shared for example
 * SelectionManager is not desirable to share between ViewPanels.
 * 
 * @author G Stack
 * 
 */
public class ViewContext extends ObservableModel implements TransactionManager, Cloneable
{
	// Bounded properties
	public static final String SCALE = "scale";
	public static final String DIRTY = "dirty";		// Used by desktop applications to indicate unsaved work
	
	public static final Object ANTIALIASING = RenderingHints.VALUE_ANTIALIAS_ON;
	public static final Object TEXT_ANTIALIASING = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
	public static final Object FRACTIONAL_METRICS = RenderingHints.VALUE_FRACTIONALMETRICS_ON;		// Must be on to achieve correct text measurement

	// Used when calculating optical bounds since that must be done without any Graphics object
	public static final FontRenderContext DEFAULT_FONT_RENDER_CONTEXT = 
		new FontRenderContext(null, ANTIALIASING == RenderingHints.VALUE_ANTIALIAS_ON, 
				FRACTIONAL_METRICS == RenderingHints.VALUE_FRACTIONALMETRICS_ON);

	private Scale scale;
	private Scale detailLevel;
	protected SelectionManager selectionManager;
	private Transaction currentTransaction;
	private List abortListeners;
	private Class transactionType;
	private boolean isDataChangeNotified;
	private List activeTransactions;
	private Font defaultFont;

	private Frame dialogParent;
	private ToolDispatcher toolDispatcher;
	private Map properties;
	private JComponent component;
	
	public ViewContext(Scale scale, Scale detailLevel, Frame dialogParent)
	{
		this.scale = scale;
		this.detailLevel = detailLevel;
		this.selectionManager = new NullSelectionManager();
		currentTransaction = null;
		transactionType = MockTransaction.class;
		activeTransactions = new ArrayList();
		isDataChangeNotified = false;
		defaultFont = new JLabel().getFont();
		this.dialogParent = dialogParent;
		properties = new HashMap();
		abortListeners = new ArrayList();
	}
	
    /**
     * Should be used by the view as a scale factor to compute a physical size.
     * The physical size of the determines if the view is shown or not. Even if the
     * detail level will be the same as the zoom factor its necessary to separate
     * them in case the user wants a detail level not complying with the zoom factor. <br>
     * Use case: 1.Printing 2. Watching a GUI from a long distance.
     */
	public Scale getDetailLevel()
	{
		return detailLevel;
	}

	public void setDetailLevel(Scale detailLevel)
	{
		this.detailLevel = detailLevel;
	}

	public Scale getScale()
	{
		return scale;
	}

	public void setScale(Scale scale)
	{
		Object oldValue = this.scale;
		this.scale = scale;
		firePropertyChange(SCALE, oldValue, scale);
	}

	public SelectionManager getSelectionManager()
	{
		return selectionManager;
	}

	public void setSelectionManager(SelectionManager selectionManager)
	{
		this.selectionManager = selectionManager;
	}
		
	public void setTransactionType(Class transactionType)
	{
		if (currentTransaction != null)
			throw new UnsupportedOperationException("You can't change transaction type when there is a current transaction");
		else
			this.transactionType = transactionType;
		
	}

	public Transaction getCurrentTransaction()
	{
		if (currentTransaction == null)
		{
			try
			{
				Constructor con = transactionType.getConstructor(new Class[]{ViewContext.class});
				currentTransaction = (Transaction)con.newInstance(new Object[]{this});
//				System.out.println("ViewContext created transaction " + currentTransaction.getClass() + " id: " + currentTransaction.hashCode());
			}
			catch (SecurityException e)
			{
				e.printStackTrace();
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}
			catch (NoSuchMethodException e)
			{
				e.printStackTrace();
			}
			catch (InstantiationException e)
			{
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
			catch (InvocationTargetException e)
			{
				e.printStackTrace();
			}
		}
		return currentTransaction;
	}
	
	/**
	 * The transaction remove it self from the active transaction list when
	 * the execution is completed.
	 */
	public void executeCurrentTransaction()
	{
		if (currentTransaction != null)
		{
			System.out.println("ViewContext.executeCurrentTransaction " + currentTransaction.getClass() + " id: " + currentTransaction.hashCode());
			activeTransactions.add(currentTransaction);
			currentTransaction.execute();
			currentTransaction = null;
		}
		firePropertyChange(DIRTY);
	}

	public void dispose(Transaction transaction)
	{
		activeTransactions.remove(transaction);
//		if (currentTransaction == transaction)
			currentTransaction = null;
	}

	public boolean isDataChangeNotified()
	{
		return isDataChangeNotified;
	}

	public void setIsDataChangeNotified(boolean isDataChangeNotified)
	{
		this.isDataChangeNotified = isDataChangeNotified;
	}

	public Font getDefaultFont()
	{
		return defaultFont;
	}

	/**
	 * In case you need to show a dialog use this as dialog parent argument.
	 */
	public Frame getDialogParent()
	{
		return dialogParent;
	}
	
	public void setComponent(JComponent component)
	{
		this.component = component;
	}
	
	public void repaint(View view)
	{
		if (view.getDirtyRegion() != null)
			repaint(view.getDirtyRegion().getBounds());
	}
	
	public void repaint(Rectangle rectangle)
	{
		if (component != null)
			component.repaint(rectangle);
	}
	
	/**
	 * 
	 * @return a copy where everything is shared with the original except the scale
	 */
	public ViewContext getCopy()
	{
		try
		{
			ViewContext copy = (ViewContext)clone();
			copy.activeTransactions = new ArrayList();
			copy.currentTransaction = null;
			copy.defaultFont = new Font(defaultFont.getAttributes());
			copy.detailLevel = detailLevel.getCopy();
			copy.properties = new HashMap(properties);
			copy.scale = scale.getCopy();
			copy.selectionManager = selectionManager.getCopy();
			copy.component = null;	// Must be assigned after copy to avoid unwanted sharing
			return copy;
		}
		catch (CloneNotSupportedException e)
		{
			throw new RuntimeException("Clone should be supported", e);
		}
	}
	
	public void setProperty(Object key, Object value)
	{
		properties.put(key, value);
	}
	
	public Object getProperty(Object key)
	{
		return properties.get(key);
	}

	/**
	 * Overridden by subclasses that handles time scale
	 */
	public void adjustTimeScaleToFit(float width)
	{
		
	}
	
	public boolean hasTimeScale()
	{
		return false;
	}

	public JComponent getComponent()
	{
		return component;
	}

	public void addTransactionAbortedListster(TransactionAbortedListener transactionAbortedListster)
	{
		abortListeners.add(transactionAbortedListster);
		
	}

	public void removeTransactionAbortedListster(TransactionAbortedListener transactionAbortedListster)
	{
		abortListeners.remove(transactionAbortedListster);
		
	}

	public void transactionAborted(Transaction transaction)
	{
		for (Iterator iter = abortListeners.iterator(); iter.hasNext();)
		{
			TransactionAbortedListener listener = (TransactionAbortedListener)iter.next();
			listener.aborted(transaction);
			
		}		
	}
	
	public AffineTransform getTransform()
	{
		return AffineTransform.getScaleInstance(getScale().x, getScale().y);
	}
	
	public boolean isReadOnly()
	{
		if (toolDispatcher != null)
			return toolDispatcher.isReadOnly();
		else
			return false;
	}

	public void setToolDispatcher(ToolDispatcher toolDispatcher)
	{
		this.toolDispatcher = toolDispatcher;
	}

	public void abortCurrentTransaction()
	{
		if (currentTransaction != null)
			currentTransaction.abort();		
	}


}
