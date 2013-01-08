/**
 * This is the implementation of the DMA interface.
 * ------------------------------------------------
 *  @author Ciro Mattia Gonano <CODE>&lt;gonano@CS.UniBO.It&gt;</CODE>
 *  @author Jacopo Saporetti <CODE>&lt;saporet3@CS.UniBO.It&gt;</CODE>
 *  @author Mauro Seno <CODE>&lt;mseno@CS.UniBO.It&gt;</CODE>
 *  ···············································
 */

import java.util.*;

public class MarsDMA extends Thread implements DMA, TimerUser
{
	private MarsScheduler sched;
	private MarsStorage storage;
	private MarsMemory memory;
	private MarsProcessor processor;
	private MarsInterruptHandler handler;
	private MarsTimer timer;
	private int myID;
	private boolean ticked;
	private List readq;
	private List writeq;
	private boolean write_turn;
	private boolean dead;
	private int maxrw;

	public MarsDMA ( MarsProcessor proc, MarsMemory mem, MarsStorage stor, MarsTimer tim, MarsScheduler scheduler )
	{
		MarsSystemConstant cons = new MarsSystemConstant();
		maxrw=cons.getMaxReadWrite();
		processor=proc;
		memory=mem;
		storage=stor;
		timer=tim;
		dead=false;
		myID=timer.addTimerUser((TimerUser)this);
		System.out.println("DMA: assigned timerID: " +myID);
		sched=scheduler;
		readq=Collections.synchronizedList(new LinkedList());
		writeq=Collections.synchronizedList(new LinkedList());
	}

	/**
	 * The entry point for the thread
	 */
	public synchronized void run()
	{
		System.out.println("Starting DMA...");
		while(!dead)
			doIO();
		System.out.println("Shutting down DMA...");
	}

	/**
	 * Define a new ID for the DMA by the timer.
	 *
	 * @param the new ID.
	 */
	public void setTimerID ( int newID )
	{
		myID = newID;
	}

	/**
	 * Get the TimerID for the DMA.
	 *
	 * @return the timerID
	 */
	public int getMarsDMATimerID()
	{
		return myID;
	}

	/**
	 * Set the interrupt handler to be used when an interrupt
	 * request with number irqn is generated.
	 * @param irqn number of interrupt request.
	 * @param handler handler to use
	 */
	public void setInterruptHandler ( int irqn, InterruptHandler handler )
	{
		this.handler = (MarsInterruptHandler) handler;
		/* irqn is unused, as it's always 3 */
	}

	/**
	 * Gets the interrupt handler set to be used when
	 * an I/O operation ends.
	 */
	public MarsInterruptHandler getInterruptHandler()
	{
		return handler;
	}

	/**
	 * When the DMA gets a tick from the timer, completes a read operation
	 * and a write one, and returns.
	 */
	public synchronized void tick()
	{
		ticked=true;
		this.notify();
	}

	/**
	 * Only when DMA is ticked, try to exec a write operation; if it's not
	 * possible (i.e.: write queue is empty) then completes a read operation.
	 */
	/* la variabile write_turn e` stata inserita per dar modo al DMA di dare
	 * alternativamente spazio alle read e alle write, prevenendo cosi`
	 * i casi di starvation che potevano generarsi dando priorita` all'una
	 * o all'altra coda
	 */
	private void doIO()
	{
		boolean ret;
		while(!ticked)
			try { this.wait();
			} catch (InterruptedException e) {}
		if (dead)
			return;
		if (write_turn && performWrite(false))
			write_turn=false;
		else {
			ret=performRead();
			write_turn=true;
		}
		timer.tickCompleted(myID);
		ticked=false;
		return;
	}

	/**
	 * Sets current thread dead.
	 */
	public void die()
	{
		System.out.println("Death request ackieved");
		flushPendingWriteRequests();
		dead=true;
	}

	/**
	 * Flushes all pending requests to avoid leaving the storage in an
	 * uncoherent state.
	 */
	private void flushPendingWriteRequests()
	{
		while (writeq.size()>0)
			performWrite(true);
	}

	/**
	 * Moves <CODE>size</CODE> cells from the file identified
	 * by <CODE>file</CODE> starting from the file position
	 * specified by <CODE>offset</CODE> to the memory position
	 * specified by <CODE>address</CODE>.
	 * This method doesn't perform the transfer really: it only
	 * enqueues the requested operation, and the DMA's executes
	 * it on behalf of the OS.
	 *
	 * @param file the file from which move the cells.
	 * @param offset the file position from which to begin reading cells.
	 * @param size the number of cells to be moved.
	 * @param address the memory address where to store the cells.
	 */
	public synchronized void read ( int file, int offset, int size, int address )
	{
		int pid = sched.getPID();
		int tid = sched.getTID();
		while (size>0) {
			int tmpsize = ( size > maxrw ? maxrw : size );
			int fin = ( size == tmpsize ? 0 : 1 );
			int[] listel = {file,address,offset,tmpsize,pid,tid,fin};
			readq.add(listel);
			offset += tmpsize;
			address += tmpsize;
			size -= tmpsize;
		}
	}

	/**
	 * Moves <CODE>size</CODE> cells from the memory postion
	 * specified by <CODE>address</CODE>, to the file identified
	 * by <CODE>file</CODE>, starting from the file position
	 * <CODE>offset</CODE>.
	 * This method doesn't perform the transfer really: it only
	 * enqueues the requested operation, and the DMA's executes
	 * it on behalf of the OS.
	 *
	 * @param file the file to which store read cells.
	 * @param offset the file position to which to begin writing cells.
	 * @param size the number of cells to be moved.
	 * @param address the memory address where to begin reading cells.
	 */
	public synchronized void write ( int file, int offset, int size, int address )
	{
		int pid = sched.getPID();
		int tid = sched.getTID();
		while (size>0) {
			int tmpsize = ( size > maxrw ? maxrw : size );
			int fin = ( size == tmpsize ? 0 : 1 );
			int[] listel = {address,file,offset,size,pid,tid,fin};
			writeq.add(listel);
			offset += tmpsize;
			address += tmpsize;
			size -= tmpsize;
		}
	}

	private synchronized boolean performRead()
	{
		int[] element;
		if (readq.size()<1) {
			//System.out.println("Read queue is empty!");
			return false;
		}
		element = (int[])readq.remove(0);
		MarsCell[] tmp = new MarsCell[element[3]];
		tmp=(MarsCell[])storage.read(element[4],element[0],element[2],element[3]);
		for ( int i=0; i<element[3]; i++,element[1]++)
			memory.writeCell(element[1],tmp[i]);
		if (element[6]==0)
			handler.interrupt(3,element[4],element[5]);
		return true;
	}

	private synchronized boolean performWrite (boolean flush)
	{
		int[] element;
		if (writeq.size()<1) {
			//System.out.println("Write queue is empty!");
			return false;
		}
		element = (int[])writeq.remove(0);
		MarsCell[] tmp = new MarsCell[element[3]];
		for ( int i=0; i<element[3]; i++,element[0]++)
			tmp[i]=(MarsCell)memory.readCell(element[0]);
		storage.write(element[4],element[1],element[2],tmp);
		if (!flush && element[6]==0)
			handler.interrupt(3,element[4],element[5]);
		return true;
	}
}
