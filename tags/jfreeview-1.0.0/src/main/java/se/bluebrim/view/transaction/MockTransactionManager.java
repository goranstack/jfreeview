package se.bluebrim.view.transaction;

import java.awt.Frame;


/**
 * A transaction manager to be used when no transaction handling is
 * desired.
 * 
 * @author G Stack
 *
 */
public class MockTransactionManager implements TransactionManager
{

	public Transaction getCurrentTransaction()
	{
		return null;
	}

	public void executeCurrentTransaction()
	{

	}

	public boolean isDataChangeNotified()
	{
		return false;
	}

	public void dispose(Transaction transaction)
	{

	}

	public Frame getDialogParent()
	{
		return null;
	}

	public void setCurrentTransaction(Transaction currentTransaction)
	{
		// TODO Auto-generated method stub
		
	}

	public void setTransactionType(Class transactionType)
	{
		// TODO Auto-generated method stub
		
	}

	public void addTransactionAbortedListster(TransactionAbortedListener transactionAbortedListster)
	{
		// TODO Auto-generated method stub
		
	}

	public void removeTransactionAbortedListster(TransactionAbortedListener transactionAbortedListster)
	{
		// TODO Auto-generated method stub
		
	}

	public void transactionAborted(Transaction transaction)
	{
		// TODO Auto-generated method stub
		
	}

	public void abortCurrentTransaction()
	{
		// TODO Auto-generated method stub
		
	}

}
