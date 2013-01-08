/**
 *  This is the implementation of a Mars File.
 *  ---------------------------------------------
 *  @author Ciro Mattia Gonano <CODE>&lt;gonano@CS.UniBO.It&gt;</CODE>
 *  @author Jacopo Saporetti <CODE>&lt;saporet3@CS.UniBO.It&gt;</CODE>
 *  @author Mauro Seno <CODE>&lt;mseno@CS.UniBO.It&gt;</CODE>
 *  _____________________________________________
 */

public class MarsFile
{
	private MarsCell[] content;
	private int ID;
	private int ownerID;
	private int howmanyopen;
	private boolean open;
	private boolean canwrite;

	/**
	 * Constructor with fileid, content and ownerid.
	 * 
	 * @param myID the id of the new file.
	 * @param myownerID the id of the owner of new file.
	 */
	public MarsFile ( int myID, int myownerID )
	{
		MarsCell[] mycontent = new MarsCell[1];
		mycontent[0] = new MarsCell();
		ID = myID;
		content = mycontent;
		ownerID = myownerID;
		howmanyopen=0;
		open = false;
	}

	/**
	 * Constructor with fileid, content and ownerid.
	 * 
	 * @param myID the id of the new file.
	 * @param newContent the file's content.
	 * @param myownerID the id of the owner of new file.
	 */
	public MarsFile ( int myID, MarsCell[] newContent, int myownerID )
	{
		ID = myID;
		ownerID = myownerID;
		howmanyopen=0;
		open = false;
		content=newContent;
	}

	/**
	 * Constructor with fileID and content.
	 * 
	 * @param myID the ID of the new file
	 * @param newContent the file's content
	 */
	public MarsFile ( int myID, MarsCell[] newContent )
	{
		ID = myID;
		howmanyopen=0;
		open = false;
		content=newContent;
		/*  Vedi commento primo costruttore */
	}

	/**
	 * Gets the file's ID.
	 * @return the file id.
	 */
	public int getMarsFileId()
	{
		return this.ID;
	}

	/**
	 * Gets the file's owner's ID.
	 * @return the file owner.
	 */
	public int getMarsFileOwner()
	{
		return this.ownerID;
	}

	/**
	 * Reads the whole file's content.
	 * @return an array of <CODE>MarsCell</CODE> objects.
	 */
	public MarsCell[] getMarsFileContent()
	{
		return content;
	}

	/**
	 * Gets the file's size (i.e.: no. of Cells)
	 * @return number of Cells
	 */
	public int getMarsFileSize()
	{
		return content.length;
	}

	/**
	 * Returns the status of a file.
	 * @return true if the file's open, false elsewhere.
	 */
	public boolean getMarsFileIsOpen()
	{
		return open;
	}

	/**
	 * Sets the ID of the file.
	 * @param newID the new file's ID.
	 */
	public void setMarsFileId ( int newID )
	{
		this.ID = newID;
	}

	/**
	 * Sets the new file's ownerID.
	 * @param newownerID the new file's owner's ID.
	 */
	public void setMarsFileOwner ( int newownerID )
	{
		this.ownerID = newownerID;
	}

	/**
	 * Sets the new file's content.
	 * @param newcontent an array of <CODE>MarsCell</CODE> objects.
	 */
	public void setMarsFileContent ( MarsCell[] newcontent )
	{
		content = newcontent;
	}

	/**
	 * Opens the file.
	 * 
	 * @param isopen state
	 * @param mode 0 if read-only, 1 if read-write
	 *
	 * @return true if file is successfully opened, false elsewhere
	 */
	public boolean setMarsFileIsOpen ( boolean isopen, boolean mode )
	{
		if ( isopen && (canwrite||mode) )	// multiple readers or only a writer allowed
			return false;
		else {
			isopen=true;
			canwrite = mode;
			howmanyopen++;
		}
		return true;
	}

	/**
	 * Closes the file for the calling thread. If the calling thread is not
	 * the last reader/writer that has the file's "handle", the file is
	 * not really closed, but calling thread is removed from the readingthread
	 * list.
	 * 
	 * @return true if the file has been closed, false elsewhere
	 */
	public boolean setMarsFileIsClosed ()
	{
		if ((howmanyopen--)==0)
			open=false;
		return !open;
	}

	/**
	 * Reads <CODE>num</CODE> Cells in the file, beginning
	 * from the <CODE>pos</CODE>th.
	 * @param pos the offset in the file.
	 * @param num number of Cells to be read.
	 * @return an array of <CODE>MarsCell</CODE> with the requested cells.
	 */
	public MarsCell[] getMarsFileCells ( int pos, int num )
	{
		if ( (pos>=content.length) || ((pos+num)>content.length) )
			throw new SecurityException("read: out of file limits.");
		else
		{
			MarsCell[] retarray = new MarsCell[num];
			for ( int i=0; i<num; i++)
				retarray[i] = (MarsCell) (content[pos+i]);
			return retarray;
		}
	}

	private void cellCopy ( int pos, MarsCell towrite )
	{
		content[pos] = towrite;
	}

	/** Sets the content of a Cell in the file, at position <CODE>pos</CODE>.
	 * @param pos the position where to write
	 * @param towrite the <CODE>MarsCell</CODE> object to write down.
	 */
	public void setMarsFileCell ( int pos, MarsCell towrite )
	{
		if (pos>=content.length)
			throw new SecurityException("write: out of file size.");
		else
			cellCopy(pos,towrite);
	}

	/**
	 * Sets the content of <CODE>num</CODE> Cells in the file,
	 * beginning from the <CODE>pos</CODE>th.
	 * @param pos the offset in the file.
	 * @param num number of Cells to be written.
	 * @param towrite array of <CODE>MarsCell</CODE> with content to write.
	 */
	public void setMarsFileCells ( int pos, int num, MarsCell[] towrite )
	{
		if ( (pos>=content.length) || ((pos+num)>content.length) )
			throw new SecurityException("write: out of file size.");
		else
			for ( int i=0; i<num; i++ )
				cellCopy(pos+i,towrite[i]);
	}
}

