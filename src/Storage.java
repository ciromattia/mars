/**
 *  This interface represents a storage, whose task is to store a 
 *  set of files and enable reading-writing of them.
 *  
 *  @author Alberto Montresor
 */
public interface Storage
{
  
  /**
   *  Reads <CODE>size</CODE> cells from file <CODE>file</CODE>
   *  starting from offset <CODE>offset</CODE>. Returns the
   *  cells read in an array. If less cells than requested can
   *  be read (end of file), the cell array returned contains
   *  these cells. If the file belongs to a different
   *  process id, a security exception is thrown.
   *  
   *  @param pid process id of the process reading the file
   *  @param file file identifier
   *  @param offset starting position
   *  @param size number of cells to be read
   *  @return an array containing the cells that have been read.
   */
  public Cell[] read(int pid, int file, int offset, int size)
    throws SecurityException;
  
  /**
   *  Writes <CODE>size</CODE> cells from file <CODE>file</CODE>
   *  starting from offset <CODE>offset</CODE>. If the file 
   *  belongs to a different process id, a security exception is 
   *  thrown. 
   *  
   *  @param pid process id of the process reading the file
   *  @param file file identifier
   *  @param offset starting position
   *  @param cells the cells to be written
   */
  public void write(int pid, int file, int offset, Cell[] cells)
    throws SecurityException;

} // END Storage

