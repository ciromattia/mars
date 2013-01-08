/**
 * This is the implementation of the interface "Storage"
 * -----------------------------------------------------
 * @author Ciro Mattia Gonano <CODE>&lt;gonano@CS.UniBO.It&gt;</CODE>
 * @author Jacopo Saporetti <CODE>&lt;saporet3@CS.UniBO.It&gt;</CODE>
 * @author Mauro Seno <CODE>&lt;mseno@CS.UniBO.It&gt;</CODE>
 * ····················································
 */

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class MarsStorage implements Storage
{
	private ArrayList storage;

	public MarsStorage()
	{
		storage = new ArrayList();
	}

	/**
	 * Returns the actual number of files contained in the storage.
	 *
	 * @return int
	 */
	public int getMarsStorageSize()
	{
		return storage.size();
	}

	/**
	 * Returns the file with ID <CODE>fileIndex</CODE>.
	 *
	 * @param fileIndex the index of the file wanted
	 * 
	 * @return a MarsFile object
	 */
	public MarsFile getMarsStorageFile ( int fileIndex )
	{
		MarsFile file;
		try { file = (MarsFile) storage.get(fileIndex);
		} catch (NoSuchElementException e) {
			file = (MarsFile) null;
		}
		return file;
	}

	/**
	 * Sets a file which ID is fileIndex.
	 *
	 * @param fileIndex the index of the new file
	 * @param file the file to be set
	 */
	public void setMarsStorageFile ( int fileIndex, MarsFile file )
	{
		storage.add(fileIndex,file);
	}

	/**
	 * Reads <CODE>size</CODE> cells from the file specified by <CODE>file</CODE>,
	 * starting from the position specified by <CODE>offset</CODE>.
	 * 
	 * @param pid the process id.
	 * @param file the file id where to read cells.
	 * @param offset where to begin to read cells.
	 * @param size number of cell to be read.
	 * @return an array of <CODE>MarsCell</CODE> elements.
	 */
	public Cell[] read ( int pid, int fileID, int offset, int size )
	{
		MarsFile file;
		try { file = (MarsFile) storage.get(fileID);
		} catch ( NoSuchElementException e) {
			throw new SecurityException("File not found."); }
		if ( (file.getMarsFileOwner()) != pid )
			throw new SecurityException("read: process does not own the file!");
		else
			return (Cell[])file.getMarsFileCells(offset,size);
	}

	/**
	 * Writes <CODE>size</CODE> cells from file identified by <CODE>file</CODE>,
	 * starting from the position specified by <CODE>offset</CODE>.
	 * 
	 * @param pid the process id.
	 * @param file the file id where to write cells.
	 * @param offset where to begin to write cells.
	 * @param towrite cells to be written
	 */
	public void write ( int pid, int fileID, int offset, Cell[] towrite )
	{
		MarsFile file;
		try { file = (MarsFile) storage.get(fileID);
		} catch ( NoSuchElementException e) {
			throw new SecurityException("File not found."); }
		if ( (file.getMarsFileOwner()) != pid )
			throw new SecurityException("write: process does not own the file!");
		else
			file.setMarsFileCells(offset,towrite.length,(MarsCell[])towrite);
	}
}

