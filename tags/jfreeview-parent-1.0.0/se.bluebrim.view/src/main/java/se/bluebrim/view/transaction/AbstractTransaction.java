package se.bluebrim.view.transaction;

import java.awt.Frame;
import java.util.*;

/**
 * Abstract superclass to all kinds of transaction objects
 * 
 * @author G Stack
 *
 */
public abstract class AbstractTransaction implements Transaction
{
	private TransactionManager transactionManager;
	protected List commands;
	protected Command finalCommand;
	protected Map isolatedObjects;	// <TransactionalObject>'s embraced by the transaction
	protected boolean aborted;
	private Iterator commandIterator;

	
	public AbstractTransaction(TransactionManager transactionManager)
	{
		this.transactionManager = transactionManager;
		commands = new ArrayList();
		isolatedObjects = new HashMap();
		aborted = false;
	}
	
	/**
	 * TODO: Pull up to AbstractTransaction since this is not WMTransaction specific
	 */
	public void abort()
	{
		markTargetObjects(false);
//		notifiyViewsOutOfSync();	Replaced by transactionAborted call
		aborted = true;
		transactionManager.transactionAborted(this);
		transactionManager.dispose(this);		
	}
	
	public void dispose()
	{
		markTargetObjects(false);
		transactionManager.dispose(this);		
	}


	protected String listCommands()
	{
		StringBuffer buffer = new StringBuffer();
		for (Iterator iter = getCommandIterator(); iter.hasNext();)
		{
			Command command = (Command) iter.next();
			buffer.append(command.getName());
			if (iter.hasNext())
				buffer.append(", ");
		}
		return buffer.toString();
	}
	
	
	synchronized protected void executeCommands()
	{
		commandIterator = getCommandIterator();
		continuExecuteCommands();
	}

	/**
	 * Keep the command iterator in an instance variable to
	 * avoid filling the stack when running this recursive
	 * method.
	 */
	synchronized private void continuExecuteCommands()
	{
		if (commandIterator.hasNext())
		{
			Command command = (Command) commandIterator.next();
			command.execute(this, new Command.CompletedListener(){

				public void completed()
				{
					continuExecuteCommands();
				}});
		}		
	}



	protected void markTargetObjects(boolean hasUnconfirmedChanges)
	{
		for (Iterator iter = getCommandIterator(); iter.hasNext();)
		{
			Command command = (Command) iter.next();
			TransactionalObject targetObject = command.getTargetObject();
			if (targetObject != null)
				targetObject.setHasUnconfirmedChanges(hasUnconfirmedChanges);
		}
	}

	/**
	 * @deprecated Can't handle structural changes of views for example
	 * parent switching in drag and drop operations. <br>
	 * Use addTransactionAbortedListener instead to rebuild the entire
	 * view structure when needed.
	 */
	private void notifiyViewsOutOfSync()
	{
		for (Iterator iter = getCommandIterator(); iter.hasNext();)
		{
			Command command = (Command) iter.next();
			TransactionalObject targetObject = command.getTargetObject();
			if (targetObject != null)
				targetObject.notifiyViewsOutOfSync();
		}
	}
	
	public void addCommand(Command command)
	{
		commands.add(command);
	}

	protected Iterator getCommandIterator()
	{
		List commandList = new ArrayList(commands);
		if (finalCommand != null)
			commandList.add(finalCommand);
		return commandList.iterator();
	}
	
	public Frame getDialogParent()
	{
		return transactionManager.getDialogParent();
	}

	public void setFinalCommand(Command finalCommand)
	{
		this.finalCommand = finalCommand;		
	}

	/**
	 * When the command is created the command refers to the non writable version
	 * as target object. This method find the corresponding writable object. This
	 * procedure is known as Canonicalize.
	 */
	public TransactionalObject getObject(TransactionalObject transactionalObject)
	{
		return (TransactionalObject)isolatedObjects.get(transactionalObject.getOid());
	}
	
	protected abstract void isolateTargetObjects();
	
	protected abstract void commit();

	public void execute()
	{
		System.out.println("WMTransaction.execute transactionId: " + hashCode());
		if (failedIsolateTargetObjects())
			return;		
		if (failedExecuteCommands())
			return;
		// The call to commit is handled by subclasses
	}

	private boolean failedIsolateTargetObjects()
	{
		if (aborted)
			throw new RuntimeException("The transaction is aborted and execute should not be called");
		try {
			isolateTargetObjects();
		} catch (RuntimeException e) 
		{
			System.out.println("Unable to isolate target objects of " + listCommands() + " due to " + e);
			abort();
			return true;
		}
		return false;		
	}
	
	private boolean failedExecuteCommands()
	{
		try {
			executeCommands();
		} catch (RuntimeException e) 
		{
			System.out.println("Unable to perform " + listCommands() + " due to " + e);
			abort();
			return true;
		}
		return false;
	}
	
	protected void executeFinalCommand(Command.CompletedListener completedListener)
	{
		if (finalCommand !=  null)
			finalCommand.execute(this, completedListener);
		else
			completedListener.completed();
	}
	
	protected void validate()
	{
		try
		{
			for (Iterator iter = isolatedObjects.values().iterator(); iter.hasNext();)
			{
				TransactionalObject object = (TransactionalObject)iter.next();
				object.validate();			
			}
		}
		catch (TransactionalObject.InvalidStateException e)
		{
			System.out.println("Unable to perform " + listCommands() + " due to " + e);
			abort();			
		}
	}


}
