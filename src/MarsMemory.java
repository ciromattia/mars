/**
 * This class contains the implementation of the interface "Memory"
 * ----------------------------------------------------------------
 *  @author Ciro Mattia Gonano <CODE>&lt;gonano@CS.UniBO.It&gt;</CODE>
 *  @author Jacopo Saporetti <CODE>&lt;saporet3@CS.UniBO.It&gt;</CODE>
 *  @author Mauro Seno <CODE>&lt;mseno@CS.UniBO.It&gt;</CODE>
 * ································································
 */

public class MarsMemory implements Memory
{
	private MarsSystemConstant sysconst;
	private MarsCell[] memory;
	private int CoreSize;

	public MarsMemory()
	{
		sysconst = new MarsSystemConstant();
		CoreSize = sysconst.getCoreSize();
		memory = new MarsCell[CoreSize];
		for (int i=0; i<CoreSize;i++)
			memory[i]=new MarsCell();
	}

	/**
	 * Reads the MarsCell pointed by index
	 * @param index specifies the Cell wanted
	 * @return the requested MarsCell
	 */
	public synchronized Cell readCell ( int index )
	{
		return memory[index%CoreSize];
	}

	/**
	 * Write in the MarsCell pointed by <CODE>index</CODE>
	 * into the memory the contents specified in <CODE>cell</CODE>.
	 * @param index the offset into the array "memory"
	 * @param cell an object "MarsCell" specifing what to write
	 */
	public synchronized void writeCell ( int index, Cell cell )
	{
		memory[index%CoreSize] = (MarsCell) cell;
	}
}

