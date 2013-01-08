/**
 *  This class contains the specifies for Mars Threads
 *  -------------------------------------------------------------
 *  @author Ciro Mattia Gonano <CODE>&lt;gonano@CS.UniBO.It&gt;</CODE>
 *  @author Jacopo Saporetti <CODE>&lt;saporet3@CS.UniBO.It&gt;</CODE>
 *  @author Mauro Seno <CODE>&lt;mseno@CS.UniBO.It&gt;</CODE>
 *  ·····························································
 */

public class MarsThread
{
	private int ID;
	private int PC;		/* program counter */
	private int[] reg;
	private int state;	/* could be:
					0: thread is "dead"
					1: thread is "not runnable"
					2: thread is "runnable"	*/
	private int priority;	/* could range from 0 (highest priority) to 4 (lowest priority) */

	/**
	 * Constructor.
	 * Sets a dead thread with the lowest priority.
	 */
	public MarsThread ( MarsSystemConstant cons, int newID)
	{
		ID = newID;
		PC = 0;
		reg = new int[cons.getRegistersNum()];
		for ( int i=0; i<cons.getRegistersNum(); i++ )
			reg[i]=0;
		state=0;
		priority=4;
	}

	/**
	 * Constructor for a fake thread.
	 */
	public MarsThread()
	{
		ID=-1;
		PC=-1;
		reg=new int[0];
		state=-1;
		priority=-1;
	}

	/**
	 * Gets ID of the thread.
	 *
	 * @return the ID.
	 */
	public int getThreadID()
	{
		return ID;
	}

	/**
	 * Gets the PC.
	 * 
	 * @return the PC.
	 */
	public int getPC()
	{
		return PC;
	}

	/**
	 * Gets the value in register <CODE>n</CODE>.
	 * 
	 * @param n the position of register
	 * @return the value of register
	 */
	public int getReg ( int n )
	{
		return reg[n];
	}

	/**
	 * Gets the state of thread.
	 *
	 * @return the state of thread.
	 */
	public int getState()
	{
		return state;
	}

	/**
	 * Gets the priority of thread.
	 *
	 * @return the priority of thread.
	 */
	public int getPriority()
	{
		return priority;
	}

	/**
	 * Sets the PC to value specified by <CODE>value</CODE>.
	 *
	 * @param value the new value the PC is to be set.
	 */
	public void setPC ( int value )
	{
		PC=value;
	}

	/**
	 * Set the value of the register <CODE>n</CODE>
	 * to the value specified in <CODE>value</CODE>.
	 *
	 * @param n the number of register.
	 * @param value the new value of register.
	 */
	public void setReg ( int n, int value )
	{
		reg[n]=value;
	}

	/**
	 * Set the state of thread to the value specified
	 * by <CODE>n</CODE>.
	 *
	 * @param n the new value of thread's state.
	 */
	public void setState ( int n )
	{
		state = n;
	}

	/**
	 * Sets thread's priority to the value specified by
	 * <CODE>n</CODE>.
	 *
	 * @param n the new value of thread's priority.
	 */
	public void setPriority ( int n )
	{
		priority = n;
	}
}

