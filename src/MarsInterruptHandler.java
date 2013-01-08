/**
 * This is the implementation of the InterruptHandler interface.
 * -------------------------------------------------------------
 *  @author Ciro Mattia Gonano <CODE>&lt;gonano@CS.UniBO.It&gt;</CODE>
 *  @author Jacopo Saporetti <CODE>&lt;saporet3@CS.UniBO.It&gt;</CODE>
 *  @author Mauro Seno <CODE>&lt;mseno@CS.UniBO.It&gt;</CODE>
 *  ····························································
 */

public class MarsInterruptHandler implements InterruptHandler
{
	private MarsProcessor processor;
	private MarsTimer timer;
	private MarsDMA dma;
	private MarsSystemConstant constant;
	private MarsSyscallHandler syscall;
	private MarsCell cell;
	private MarsScheduler sched;
	

	public MarsInterruptHandler ( MarsProcessor acpu, MarsDMA adma, MarsSyscallHandler asys,
			MarsScheduler scheduler,MarsTimer atimer)
	{
		processor=acpu;
		this.dma=adma;
		syscall=asys;
		sched=scheduler;
		timer=atimer;
		constant=new MarsSystemConstant();
	}

	/**
	 *  This method is invoked by the MARS processor whenever an
	 *  interrupt is invoked.
	 *
	 *  @param irqn Interrupt Request Number
	 *  @param data1 auxiliary parameter 1
	 *  @param data2 auxiliary parameter 2
	 */
	public synchronized void interrupt ( int irqn, int data1, int data2 )
	{
		System.out.println("==============================");
		System.out.println(" <<<<<<<< INTERRUPT >>>>>>>>> ");
		//System.out.println("==============================");
		//System.out.println("        IRQN: " + irqn);
		//System.out.println("");

		switch (irqn)
		{
			case 0:
				{	/* SYSCALL - call syscall() method */
					/* data1 resembles the called syscall, data2 as its parameter
					 * other parameters are to be read from registers */
					System.out.println("       IRQ #"+irqn+" -> SYSCALL");
					System.out.println("==============================");
					//System.out.println("SYSCALL -> calling syscall handler...");
					this.syscall(data1,data2);
					break;
				}
			case 1: 
				{	/* TIMER - call timeout() method */
					// the temporal quantum assigned for the task has expired
					System.out.println("       IRQ #"+irqn+" -> TIMEOUT");
					System.out.println("==============================");
					//System.out.println("TEMPORAL QUANTUM EXPIRED -> calling timeout() method...");
					this.timeout(data1,data2);
					break;
				}
			case 2:
				{	/* INVALID OPCODE - call invOpcode() method to kill calling thread */
					System.out.println("       IRQ #"+irqn+" -> INVALID OPCODE");
					System.out.println("==============================");
					//System.out.println("INVALID OPCODE -> killing calling thread...");
					this.invOpcode(data1);
					break;
				}
			case 3:
				{	/* DMA trap - call dmaTrap() method */
					// Read or write completed; data1 resembles the pid, data2 the tid
					System.out.println("       IRQ #"+irqn+" -> DMA TRAP");
					System.out.println("==============================");
					//System.out.println("READ/WRITE OPERATION COMPLETED -> calling dmaTrap() method...");
					this.dmaTrap(data1,data2);
					break;
				}
			default:
				{	/* invalid IRQ number */
					System.out.println("Wrong IRQ no.");
					throw new SecurityException("Wrong IRQ number");
				}
		}
	}

 	private int syscall ( int data1, int data2 )
	{
		switch (data1)
		{
			case 0:
				{	/* STARTTHREAD - Add a thread to the warriors list */
					/* data2 is the starting address
					 * priority is to be found in register 32
					 */
					int PC = data2;
					int prior;
					prior = processor.getRegister(31);
					System.out.println(" .STARTTHREAD: ADD THREAD TO WARRIOR LIST.");
					System.out.println("  - starting address: " + PC);
					System.out.println("  - priority: " + prior);
					timer.stopTimer();
					if (sched.addThread(prior,PC))
						System.out.println(" ==> new thread CREATED!");
					else
						System.out.println(" ==> new thread NOT CREATED!");
					if (sched.testPriority(prior)) {
						System.out.println(" ==> new thread's priority's higher: let's run this one!");
						sched.swapOutCurrentThread();
						sched.runNextThread();
					}
					else
						System.out.println(" ==> running thread's priority's higher:" +
								"it retains its state!");
					timer.restartTimer();
					break;
				}
			case 1:
				{	/* YIELD - Preempts the running thread and schedule another thread */
					int oldTID = sched.getTID();
					int newTID;
					System.out.println(" .YIELD: LEAVE CPU FOR ANOTHER PROCESS.");
					timer.stopTimer();
					sched.swapOutCurrentThread();
					sched.runNextThread();
					newTID=sched.getTID();
					if ( oldTID == newTID ) {
						System.out.println(" ==> thread continues running!");
						sched.ageCurrentThread();
					}
					else
						System.out.println(" ==> thread yields to thread "+newTID);
					timer.restartTimer();
					break;
				}
			case 2:
				{	/* STOP - interrupts the running thread */
					timer.stopTimer();
					int oldTID = sched.getTID();
					System.out.println(" .STOP: STOP RUNNING THREAD.");
					sched.killCurrentThread();
					System.out.println(" ==> thread "+oldTID+" succesfully kill'd");
					if (!sched.runNextThread()) {
						sched.contextSwitch();
						timer.setTimeout(constant.getQuantum(),(InterruptHandler)this);
					}
					else {
						int newTID = sched.getTID();
						System.out.println(" ==> thread "+newTID+" gains run!");
					}
					timer.restartTimer();
					break;
				}
			case 3:
				{	/* OPEN - opens a file 
					 * data2 is the file index
					 * mode is in register 32
					 */
					int fileIndex = data2;
					int mode = processor.getRegister(31);
					int pid = sched.getPID();
					int tid = sched.getTID();
					boolean truemode = ( mode==0 ? false : true );
					System.out.println("OPEN FILE STORED IN STORAGE");
					System.out.println("  - file index in storage: " + fileIndex );
					System.out.println("  - modality: " + mode );
					System.out.println("  - pid: " + pid );
					System.out.println("  - tid: " + tid );
					return syscall.open(fileIndex,truemode,pid,tid);
				}
			case 4:
				{	/* READ - reads "size" cell from the file "fd",
					 * putting them in memory address "address".
					 * data2 is the fd
					 * address is in register 32
					 * size is in register 31
					 */
					int fd = data2;
					int address = processor.getRegister(31);
					int size = processor.getRegister(30);
					int pid = sched.getPID();
					int tid = sched.getTID();
					System.out.println(" .READ. ");
					System.out.println("  - fd: " + fd);
					System.out.println("  - memory address: " + address);
					System.out.println("  - number of location to read: " + size);
					System.out.println("  - pid: " + pid);
					System.out.println("  - tid: " + tid);
					int ret=0;
					timer.stopTimerUser(processor.getMarsProcessorTimerID());
					if ((ret=syscall.read(fd,address,size,pid,tid))<0) {
						sched.killCurrentThread();
						if (!sched.runNextThread()) {
							sched.contextSwitch();
							timer.setTimeout(constant.getQuantum(),(InterruptHandler)this);
						}
					}
					else if (ret==0) { // file in use, please wait... yield!
						sched.swapOutCurrentThread();
						sched.runNextThread();
					}
					sched.holdCurrentThread();
					if (!sched.runNextThread()) {
						sched.contextSwitch();
						timer.setTimeout(constant.getQuantum(),(InterruptHandler)this);
					}
					timer.restartTimerUser(processor.getMarsProcessorTimerID());
					break;
				}
			case 5:
				{	/* WRITE - writes "size" cells from the memory
					 * "address" to the file "fd".
					 * data2 is the fd
					 * address is in the register 32
					 * size is in the register 31
					 */
					int fd = data2;
					int address = processor.getRegister(31);
					int size = processor.getRegister(30);
					int pid = sched.getPID();
					int tid = sched.getTID();
                                        System.out.println(" .WRITE. ");
					System.out.println("  - fd: " + fd);
					System.out.println("  - memory address: " + address);
					System.out.println("  - number of cells to write: " + size);
					System.out.println("  - pid: " + pid);
					System.out.println("  - tid: " + tid);
					int ret=0;
					timer.stopTimerUser(processor.getMarsProcessorTimerID());
					if ((ret=syscall.write(fd,address,size,pid,tid))<0) {
						sched.killCurrentThread();
						if (!sched.runNextThread()) {
							sched.contextSwitch();
							timer.setTimeout(constant.getQuantum(),(InterruptHandler)this);
						}
					}
					sched.holdCurrentThread();
					if (!sched.runNextThread()) {
						sched.contextSwitch();
						timer.setTimeout(constant.getQuantum(),(InterruptHandler)this);
					}
					timer.restartTimerUser(processor.getMarsProcessorTimerID());
					break;
				}
			case 6:
				{	/* CLOSE - closes file identified by "fd"
					 * data2 is the fd
					 */
					int fd = data2;
					int pid = sched.getPID();
					int tid = sched.getTID();
					System.out.println(" .CLOSE.");
					System.out.println("  - fd of file to close: " + fd);
					System.out.println("  - pid: " + pid);
					System.out.println("  - tid: " + tid);
					syscall.close(fd,pid,tid);
					break;
				}
			case 7:
				{	/* SYSCONF - returns the value of one of the following
					 * runtime variables: CoreSize, ContextSwitchBeforeTie,
					 * Quantum, MaximumNumberOfFile, MaxReadWrite,
					 * MaximumNumberOfTask, MinimumSeparation, WarriorsNumber.
					 * data2 is the requested paramid
					 */
					System.out.println(" SYSCONF: read runtime variables ");
					System.out.println("------------------------------------");
					switch (data2)
					{
						case 0:	/* CoreSize */
							return constant.getCoreSize();
						case 1:	/* TickBeforeTie */
							return constant.getContextSwitchBeforeTie();
						case 2:	/* Quantum */
							return constant.getQuantum();
						case 3:	/* MaximumNumOfFile */
							return constant.getMaximumNumberOfFile();
						case 4: /* MaxReadWrite */
							return constant.getMaxReadWrite();
						case 5:	/* MaximumNumberOfTask */
							return constant.getMaximumNumberOfTask();
						case 6:	/* MinimumSeparation */
							return constant.getMinimumSeparation();
						case 7: /* WarriorsNumber */
							return constant.getWarriorsNumber();
					}
				}
		}
		return 0;
	}

	/* TIMEOUT - Here we have a context switch! */
	private void timeout ( int data1, int data2 )
	{
		timer.stopTimer();
		sched.ageCurrentThread();
		sched.contextSwitch();
		timer.setTimeout(constant.getQuantum(),(InterruptHandler)this);
		timer.restartTimer();
	}

	/* data1 is the given opcode */
	private void invOpcode ( int data1 )
	{
		timer.stopTimer();
		sched.killCurrentThread();
		if (!sched.runNextThread()) {
			sched.contextSwitch();
			timer.setTimeout(constant.getQuantum(),(InterruptHandler)this);
		}
		timer.restartTimer();
	}

	private void dmaTrap ( int data1, int data2 )
	{
		timer.stopTimer();
		int res;
		if ((res=sched.restartThread(data1,data2))<0)
			throw new SecurityException("dmaTrap: something went wrong!!!");
		int curPID = sched.getPID();
		if ( (curPID==data1) && (sched.testPriority(res)) ) {	// awakened thread has higher priority!
			sched.swapOutCurrentThread();
			sched.runNextThread();	// runs the awakened thread
		}
		timer.restartTimer();
	}
}

