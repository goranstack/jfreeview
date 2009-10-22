package se.bluebrim.view.tool;

import se.bluebrim.view.swing.ViewPanel;


/**
 * Abstract superclass to toolbar owners that has only one tool and therefor
 * don't show any toolbar for the user since that makes no sence when only
 * one tool is used all the time.
 * 
 * @author G Stack
 */
public class SingleToolDispatcher extends AbstractToolDispatcher
{
	
	public SingleToolDispatcher(ViewPanel viewPanel, boolean readOnly)
	{
		super(viewPanel, readOnly);
		SelectionTool selectionTool = new SelectionTool(this, false);
		selectionTool.setTransactionManager(viewPanel.getViewContext());
		setCurrentTool(selectionTool);
	}
	
}
