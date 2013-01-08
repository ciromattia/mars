/**
 *  This interface contains the methods that must be implemented by
 *  the shared memory. Since the MARS memory is circular, if the
 *  indexes used to read and write the memory exceeds the memory
 *  bounds, a modulus operation is used to obtain the correct
 *  index.
 *
 *  @author Alberto Montresor
 */
public interface Memory
{

  /**
   *  Reads and returns a cell from the specified position of the
   *  memory. 
   *  
   *  @param index  the index of the cell to be read
   *  @returns a copy of the cell stored in the memory at the
   *    specified position
   */
  public Cell readCell(int index);
  
  /**
   *  Writes and returns a cell from the specified position of the
   *  memory.
   *  
   *  @param index the index of the cell to be stored
   *  @param
   */
  public void writeCell(int index, Cell cell);

} // END Memory

