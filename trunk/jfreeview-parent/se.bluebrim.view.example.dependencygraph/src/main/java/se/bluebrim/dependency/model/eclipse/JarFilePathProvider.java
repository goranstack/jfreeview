package se.bluebrim.dependency.model.eclipse;

/**
 * Implemented by objects that can provide a jar file path to be used in a include element in a file set in an Ant build
 * file
 * 
 * @author G�ran St�ck
 *  
 */
public interface JarFilePathProvider
{
	public String getJarFilePath();
}
