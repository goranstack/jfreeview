package se.bluebrim.view.transaction;

import java.awt.Frame;

/**
 * Implemented by objects that manages transactions.
 * 
 * @author G Stack
 */
public interface TransactionManager
{
	public interface TransactionAbortedListener
	{
		public void aborted(Transaction transaction);
	}
	
	public void addTransactionAbortedListster(TransactionAbortedListener transactionAbortedListster);
	public void removeTransactionAbortedListster(TransactionAbortedListener transactionAbortedListster);
	
	public Transaction getCurrentTransaction();
	
	public void executeCurrentTransaction();
	public void abortCurrentTransaction();
	
	public void setTransactionType(Class transactionType);
	
	/**
	 * The client will get a notification of the changed data when the transaction is
	 * completed. This method can be used for suppressing unnecessary rebuild of models 
	 * and repaint of views that will be redone when the data change notification arrives.
	 */
	public boolean isDataChangeNotified();
	
	/**
	 * A Transaction dispose it self when the execution is completed
	 */
	public void dispose(Transaction transaction);
	
	/**
	 * In case you need to show a dialog use this as dialog parent argument.
	 */
	public Frame getDialogParent();

	/**
	 * Tell listeners about aborted transaction
	 */
	public void transactionAborted(Transaction transaction);


}