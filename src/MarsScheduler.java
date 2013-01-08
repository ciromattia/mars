/**
 * This is the implementation of the OS' scheduler.
 * ------------------------------------------------
 * Ciro Mattia Gonano <CODE>&lt;gonano@CS.UniBO.It&gt;</CODE>
 * Jacopo Saporetti <CODE>&lt;saporet3@CS.UniBO.It&gt;</CODE>
 * Mauro Seno <CODE>&lt;mseno@CS.UniBO.It&gt;</CODE>
 * ________________________________________________
 */

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class MarsScheduler
{
	private MarsTimer timer;
	private MarsProcessor CPU;
	private int numprocesses;
	private int maxtasks;
	private int prioritylevels;
	private int csbeforetie;
	private MarsThread[][] threads;
	private LinkedList[][] queues;
	private int PC;
	private int curproc;
	private int curth;

	/**
	 * Constructor
	 */
	public MarsScheduler ( MarsSystemConstant cons, MarsProcessor myCPU, MarsTimer tim )
	{
		numprocesses = cons.getProcessNum();
		maxtasks = cons.getMaximumNumberOfTask();
		prioritylevels = cons.getPriorityLevels();
		csbeforetie = cons.getContextSwitchBeforeTie();
		threads = new MarsThread[numprocesses][maxtasks];
		for ( int i=0; i<numprocesses; i++ )
			for ( int j=0; j<maxtasks; j++ )
				threads[i][j]=new MarsThread(cons,j);
		queues = new LinkedList[numprocesses][prioritylevels];
		for ( int i=0; i<numprocesses; i++ )
			for ( int j=0; j<prioritylevels; j++ )
				queues[i][j]=new LinkedList();
		curproc=0;
		CPU = myCPU;
		timer = tim;
	}

	/**
	 * Adds a thread, if there's enough space.
	 * 
	 * @param priority
	 * @param PC
	 *
	 * @return true if new thread creation was successfull, false elsewhere
	 */
	public boolean addThread ( int priority, int PC )
	{
		int i;
		for (i=0; i<maxtasks; i++)
			if ( threads[curproc][i].getState() == 0 )
				break;
		if (i == maxtasks)
			return false;
		else {
			threads[curproc][i].setState(2);
			threads[curproc][i].setPC(PC);
			threads[curproc][i].setPriority(priority);
			queues[curproc][priority].addLast(threads[curproc][i]);
			System.out.println("Added thread #"+i+" to process "+curproc);
			System.out.println("queue for the current process has now "+queues[curproc][priority].size()+
					" in priority "+priority);
			return true;
		}
	}

	/**
	 * Sets current process.
	 * (this method is only used for bootstrapping purposes)
	 *
	 * @param pid the new current process
	 */
	public void setCurProc ( int pid )
	{
		curproc=pid;
	}

	/**
	 * Tests if a process is wether dead or not runnable.
	 *
	 * @param proc the ID of the process
	 *
	 * @return 0 if the process is dead, 1 if it's not runnable, 2 elsewhere
	 */
	public int testProcessState ( int proc )
	{
		int state = 0;
		int tmp;
		for ( int i=0; i<maxtasks; i++)
			if ( (tmp = threads[proc][i].getState()) > 0 )
					state = ( tmp > state ? tmp : state );
		return state;
	}

	/**
	 * Tests if the current thread's priority is lesser than the
	 * value specified
	 *
	 * @param priority
	 *
	 * @return true if the given priority is higher (lesser) than the current thread's one,
	 * false elsewhere
	 */
	public boolean testPriority ( int priority )
	{
		return priority < threads[curproc][curth].getPriority();
	}

	/**
	 * Returns the current processID.
	 *
	 * @return a unique PID
	 */
	public int getPID()
	{
		return curproc;
	}

	/**
	 * Returns the current threadID.
	 *
	 * @return a unique TID
	 */
	public int getTID()
	{
		return curth;
	}

	/**
	 * Saves current thread status.
	 */
	public synchronized void swapOutCurrentThread()
	{
		threads[curproc][curth].setPC(CPU.getPC());
		for (int i=0; i<32; i++)
			threads[curproc][curth].setReg(i,CPU.getRegister(i));
	}

	/**
	 * Loads thread identified by <CODE>threadID</CODE>.
	 *
	 * @param threadID the ID of the thread to load
	 */
	private void swapInThread ( int threadID )
	{
		CPU.setPC(threads[curproc][threadID].getPC());
		for (int i=0; i<32; i++)
			CPU.setRegister(i,threads[curproc][threadID].getReg(i));
	}

	/**
	 * Runs the next runnable thread with the highest priority.
	 * Schedule within threads with the same priority is executed with a
	 * FIFO algorithm.
	 * Please make attention that this method doesn't do any check
	 * with the current thread... it's a programmer job to swap out
	 * this one, before calling this method.
	 *
	 * @return false if there aren't runnable threads in the process, true elsewhere
	 */
	public synchronized boolean runNextThread()
	{
		MarsThread nextthread = new MarsThread();
		int i;
		int j;
		nextthreadfound:
		for (i=0; i<prioritylevels; i++)
			for (j=0; j<queues[curproc][i].size(); j++)
				if ( (nextthread = ((MarsThread)queues[curproc][i].removeFirst())).getState() == 2 ) {
					break nextthreadfound; }
				else
					queues[curproc][i].addLast(nextthread);
		if (i==prioritylevels)	//no runnable threads found, let's call context switch
		{
			return false;
		}
		else {
			curth=nextthread.getThreadID();
			swapInThread(curth);
			queues[curproc][i].addLast(nextthread);	// current thread is always the last in its
								// priority queue.
			return true;
		}
	}

	/**
	 * Holds current thread.
	 */
	public synchronized void holdCurrentThread()
	{
		swapOutCurrentThread();
		threads[curproc][curth].setState(1);
		int pri;
		if ((pri=threads[curproc][curth].getPriority())>0) {
			queues[curproc][pri].remove(threads[curproc][curth]);
			threads[curproc][curth].setPriority(--pri);	// an I/O bound thread gets higher priority
			queues[curproc][pri].addLast(threads[curproc][curth]);
		}
	}

	/**
	 * Restarts current thread.
	 *
	 * @param pid the owner process' ID
	 * @param tid the id of the thread to restart
	 *
	 * @return thread's priority if thread has been restarted, -1 elsewhere
	 */
	public synchronized int restartThread ( int pid, int tid )
	{
		if ( (pid<0) || (pid>=numprocesses) || (tid<0) || (tid>=maxtasks) )
			return -1;
		threads[pid][tid].setState(2);
		int pri;
		if ((pri=threads[pid][tid].getPriority())>0) {
			queues[curproc][pri].remove(threads[pid][tid]);
			threads[pid][tid].setPriority(--pri);	// an I/O bound thread gets higher priority
			queues[curproc][pri].addLast(threads[pid][tid]);
		}
		return threads[pid][tid].getPriority();
	}

	/**
	 * Kills current thread. This istruction is used when thread calls wrong
	 * opcodes, or protection faultes.
	 */
	public synchronized void killCurrentThread()
	{
		swapOutCurrentThread();
		threads[curproc][curth].setState(0);
	}

	/**
	 * Ages current thread decreasing by one its priority.
	 *
	 * @return the new priority of the thread
	 */
	public synchronized int ageCurrentThread()
	{
		int pri;
		if ((pri=threads[curproc][curth].getPriority())<(prioritylevels-1)) {
			queues[curproc][pri].remove(threads[curproc][curth]);
			threads[curproc][curth].setPriority(++pri);	// an I/O bound thread gets higher priority
			queues[curproc][pri].addLast(threads[curproc][curth]);
		}
		return pri;
	}

	/**
	 * Selects the next process, setting it as the current process.
	 * Selection mechanism is a round-robin circular list.
	 *
	 * @return the PID of the next process.
	 */
	private int selectProc(int curp)
	{
		return (int) ((curp+1)%numprocesses);
	}

	/**
	 * Context switch: test if there's a winner process, then tests the other processes
	 * to see wether they are in a running state; schedules the first running
	 * process. 
	 *
	 * @return true if there's a winner, false if the battle continues
	 */
	public boolean contextSwitch()
	{
		System.out.println(" <<<=== CONTEXT SWITCH ===>>>");
		System.out.println("     Remaining before tie: "+csbeforetie);

		boolean winner=true;
		boolean found=false;
		int i;
		swapOutCurrentThread();
		do {
			//System.out.println("Starting test loop...");
			for (i=0; i<(numprocesses-1); i++)
			{
				//System.out.println("Test loop #"+i);
				int k = selectProc(curproc+i);
				int state=testProcessState(k);
				//System.out.println("State of process "+k+" is: "+state);
				if (winner && state > 0 )
					winner=false;
				if (state==2)	// we have our man!
				{
					found=true;
					curproc=k;
					if (!runNextThread())
						throw new RuntimeException("running process but no thread is running!");
					System.out.println(" ==> scheduled running process - ID: "+curproc);
					System.out.println(" ==> scheduled thread "+curth);
					break;
				}
			}
		}
		while ((!winner) && (!found));

		if (winner) {
			System.out.println(" ***********************************");
			System.out.println("");
			System.out.println("               |||||||");
			System.out.println("               |     |");
			System.out.println("              {| ^.^ |}");
			System.out.println("               |  v  |");
			System.out.println("                \\___/");
			System.out.println("");
			System.out.println(" ******* WE HAVE A WINNER!!! *******");
			System.out.println("         ...the winner is...");
			System.out.println("");
			System.out.println("         +-----------------+");
			System.out.println("         |        "+curproc+"        |");
			System.out.println("         +-----------------+");
			System.out.println("");
			System.out.println(" Let's shutdown timer, DMA and CPU...");
			timer.shutdown();
		}
		else if (csbeforetie==0) {
			System.out.println(" ************************************");
			System.out.println("");
			System.out.println("               (-----)");
			System.out.println("               |     |");
			System.out.println("              {| \".\" |}");
			System.out.println("               |  _  |");
			System.out.println("                \\___/");
			System.out.println("");
			System.out.println(" *** SORRY, BATTLE HAS NO WINNERS ***");
			System.out.println("");
			System.out.println(" Number of context switches before tie");
			System.out.println("       reached: no winners found.");
			System.out.println("");
			System.out.println(" Let's shutdown timer, DMA and CPU...");
			timer.shutdown();
		}
		else
			csbeforetie--;
		return winner;
	}
}

