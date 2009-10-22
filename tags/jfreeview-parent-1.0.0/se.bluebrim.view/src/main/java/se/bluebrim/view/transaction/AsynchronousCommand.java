package se.bluebrim.view.transaction;

/**
 * Abstract super class to asynchronous commands.
 * 
 * @author G Stack
 *
 */
public abstract class AsynchronousCommand extends AbstractCommand
{

	public AsynchronousCommand(TransactionalObject targetObject)
	{
		super(targetObject);
	}
	
	public void execute(Transaction transaction)
	{
		throw new UnsupportedOperationException();
	}


}
