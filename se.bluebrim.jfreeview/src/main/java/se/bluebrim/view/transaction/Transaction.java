package se.bluebrim.view.transaction;

import java.awt.Frame;

/**
 * Implemented by objects that executes a list of commands in a transactional manner.
 * A Transaction must guarantee atomicity, consistency, isolation, and durability
 * <a href=http://en.wikipedia.org/wiki/ACID>(ACID)</a> of the modifications that the executed commands perform.<br>
 * 
 * @author G Stack
 */
public interface Transaction
{
	/**
	 * Add a commando to the transaction that will be executed when the execute
	 * method of the transaction is called
	 */
	public abstract void addCommand(Command command);
	
	/**
	 * The final command will be executed after all other commands. Can be
	 * used for executing code that updates objects based on values in objects
	 * updated by the commands added in the addCommand method.
	 */
	public abstract void setFinalCommand(Command command);

	/**
	 * Execute all commands that has been added to the transaction.
	 */
	public abstract void execute();

	/**
	 * Sometimes it's necessary to execute code without using a command but
	 * the code is to be executed in a transaction context. If an exception
	 * occurs this method is called to throw away all changes.<br>
	 * For exemple when PlannedPrintingJob is changed a helper method is
	 * called to update PrintingJob. If that call fails this method is called. 
	 */
	public void abort();
	
	/**
	 * In case you need to show a dialog use this as dialog parent argument.
	 */
	public Frame getDialogParent();
	
	/**
	 * When the command is created the command refers to the non writable version
	 * as target object. This method find the corresponding writable object. This
	 * procedure is known as Canonicalize.
	 */
	public TransactionalObject getObject(TransactionalObject transactionalObject);


}