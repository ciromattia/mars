/**
 *  This is the main class used as a bootstrap program to run the
 *  simulated MARS processor, the operating system and the n warriors.
 *  ----------------------------------------------------------------
 *  @author Ciro Mattia Gonano <CODE>&lt;gonano@CS.UniBO.It&gt;</CODE>
 *  @author Jacopo Saporetti <CODE>&lt;saporet3@CS.UniBO.It&gt;</CODE>
 *  @author Mauro Seno <CODE>&lt;mseno@CS.UniBO.It&gt;</CODE>
 *  ________________________________________________________________
 */

public class Run
{
	public static void main(String[] args)
	{
		/* load constants/variables */
		MarsSystemConstant constants = new MarsSystemConstant();

		/* init memory, storage and syscalls for the storage */
		MarsMemory memory = new MarsMemory();
		MarsStorage storage = new MarsStorage();
		putWarriorsInStorage(storage);

		/* now, init processor, timer, DMA, and then scheduler and handlers */
		MarsTimer timer = new MarsTimer();
		MarsProcessor processor = new MarsProcessor(memory,constants,timer);
		MarsScheduler scheduler = new MarsScheduler(constants,processor,timer);
		MarsDMA DMA = new MarsDMA(processor,memory,storage,timer,scheduler);
		MarsSyscallHandler syscalls = new MarsSyscallHandler(DMA,scheduler,storage);
		MarsInterruptHandler inthandler = new MarsInterruptHandler(processor,DMA,syscalls,scheduler,timer);

		/* ..then, add a thread for each process, and load its warrior */
		int curPC;
		/*
		 * A solution like this one:
		 * 
		for (int i=0; i<constants.getProcessNum(); i++) {
			System.out.println("Bootstrapping warrior "+i);
			scheduler.setCurProc(i);
			scheduler.addThread(2,0+(curPC=(i*constants.getMinimumSeparation())));
			MarsCell[] tmp = (storage.getMarsStorageFile(i)).getMarsFileContent();
		}
		 *
		 * would had offered much more scalability and universality.
		 * Because the bootstrap protocol is not the main purpose of this
		 * project, we choosed for the solution below, that's strictly bound
		 * to constants defined at compile-time.
		 */
		for (int i=0; i<constants.getProcessNum(); i++) {
			System.out.println("Bootstrapping warrior "+i);
			scheduler.setCurProc(i);
			scheduler.addThread(2,0+(curPC=(i*constants.getMinimumSeparation())));
			System.out.println("curPC is now "+curPC);
			switch (i)
			{
				case 0:
					new datamon(memory,curPC);
					break;
				case 1:
					new devimon(memory,curPC);
					break;
				case 2:
					new angemon(memory,curPC);
					break;
			}
		}
		/* setup the processor... */
		processor.setPC(0);
		for (int i=0; i<constants.getInterruptNum(); i++)
			processor.setInterruptHandler(i,inthandler);
		/* ..the timer... */
		timer.setTimeout(constants.getQuantum(), inthandler);
		/* ..and the DMA. */
		DMA.setInterruptHandler(3,inthandler);

		/* Well, all ready, let's start the dances */
		processor.start();
		DMA.start();
		timer.start();
	}

	private static void putWarriorsInStorage ( MarsStorage storage )
	{
		MarsSystemConstant constants = new MarsSystemConstant();
		int WARNUMB=constants.getWarriorsNumber();
		/* quickly load warriors in storage
		 * This solution is thinked to be simply, rather than scalable.
		 * You may have to modify this part of the code, to modify
		 * warrior number and type */
		new datamon(storage,0);
		new devimon(storage,1);
		new angemon(storage,2);
	}
}

class warrior
{
	private MarsCell[] warrior;

	/**
	 * Returns the warrior array
	 */
	public MarsCell[] getWarrior()
	{
		return warrior;
	}
}

class datamon extends warrior
{
	private MarsCell[] warrior = {
		new MarsCell(0,10,9),
		new MarsCell(0,11,0),
		new MarsCell(0,12,3),
		new MarsCell(32,0,0),
		new MarsCell(7,10,12),
		new MarsCell(0,14,-8),
		new MarsCell(1,11,10),
		new MarsCell(13,12,14),
	};

	/**
	 * Constructor for the storage
	 */
	datamon ( MarsStorage storage, int owner )
	{
		storage.setMarsStorageFile(owner,new MarsFile(owner,this.getWarrior()));
	}
	
	/**
	 * Constructor for the memory
	 */
	datamon ( MarsMemory memory, int startingaddress )
	{
		for (int i=0; i<8; i++)
		{
			System.out.println("adding values "+warrior[i].getOpcode()+"."+
					warrior[i].getOperandA()+"."+warrior[i].getOperandB()+
					" to memory address "+startingaddress+i);
			memory.writeCell(startingaddress+i,warrior[i]);
		}
	}
}

class devimon extends warrior
{
	private MarsCell[] warrior = {
		new MarsCell(0,10,1),
		new MarsCell(0,11,5),
		new MarsCell(0,12,3),
		new MarsCell(32,0,0),
		new MarsCell(7,10,12),
		new MarsCell(10,14,-7),
		new MarsCell(13,12,14),
	};

	/**
	 * Constructor for the storage
	 */
	devimon ( MarsStorage storage, int owner )
	{
		storage.setMarsStorageFile(owner,new MarsFile(owner,this.getWarrior()));
	}

	/**
	 * Constructor for the memory
	 */
	devimon ( MarsMemory mem, int startingaddress )
	{
		for (int i=0; i<7; i++) {
			System.out.println("adding values "+warrior[i].getOpcode()+"."+
					warrior[i].getOperandA()+"."+warrior[i].getOperandB()+
					" to memory address "+startingaddress+i);
			mem.writeCell(startingaddress+i,warrior[i]);
		}
	}
}

class angemon extends warrior
{
	private MarsCell[] warrior = {
		new MarsCell(0,0,0),		//100
		new MarsCell(0,1,1),		//101
		new MarsCell(32,103,0),		//102
		new MarsCell(1,0,1),		//103
		new MarsCell(0,7,0),		//1000
		new MarsCell(0,8,3),		//1001
		new MarsCell(32,1003,0),	//1002
		new MarsCell(1,0,8),		//1003
		new MarsCell(0,11,7),		//2001
		new MarsCell(32,2003,0),	//2002
		new MarsCell(1,0,11),		//2003
	};

	/**
	 * Constructor for the storage
	 */
	angemon ( MarsStorage storage, int owner )
	{
		storage.setMarsStorageFile(owner,new MarsFile(owner,this.getWarrior()));
	}

	/**
	 * Constructor for the memory
	 */
	angemon ( MarsMemory mem, int staddr )
	{
		for (int i=0; i<11; i++)
		{
			System.out.println("adding values "+warrior[i].getOpcode()+"."+
					warrior[i].getOperandA()+"."+warrior[i].getOperandB()+
					" to memory address "+staddr+i);
			switch (i)
			{
				case 0:
				case 1:
				case 2:
				case 3:
					mem.writeCell(staddr+i+100,warrior[i]);
					break;
				case 4:
				case 5:
				case 6:
				case 7:
					mem.writeCell(staddr+i+996,warrior[i]);
					break;
				default:
					mem.writeCell(staddr+i+1993,warrior[i]);
					break;
			}
		}
	}
}

