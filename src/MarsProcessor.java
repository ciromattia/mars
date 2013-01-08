/**
 *  This class contains the implementation of the simulated MARS processor. 
 * 
 *  @author Giovanni Melaga
 *  @author Alberto Montresor
 */
public class MarsProcessor extends Thread implements Processor, TimerUser
{
	///////////////////////////////////////////////////////////////////////////
	// Internal constants
	///////////////////////////////////////////////////////////////////////////

	/** Number of different interrupt types that can be generated */
	public static final int NINTERRUPTS = 4;

	/** Number of registers */
	public static final int NREGISTERS = 32;

	///////////////////////////////////////////////////////////////////////////
	// Opcodes
	///////////////////////////////////////////////////////////////////////////

	public final int IMM      = 0;
	public final int COPY     = 1;
	public final int LOADA    = 2;
	public final int LOADB    = 3;
	public final int STOREA   = 4;
	public final int STOREB   = 5;
	public final int MOVE     = 6;
	public final int ADD      = 7;
	public final int SUB      = 8;
	public final int AND      = 9;
	public final int OR       = 10;
	public final int NOT      = 11;
	public final int JUMP     = 12;
	public final int BGT      = 13;
	public final int BLT      = 14;
	public final int BEQ      = 15;
	public final int BNE      = 16;
	public final int SYSCALL  = 32;

	///////////////////////////////////////////////////////////////////////////
	// Fields
	///////////////////////////////////////////////////////////////////////////

	/** Registered interrupt handlers */
	public MarsInterruptHandler[] handlers;

	/** Program counter */
	public int PC;

	/** Register array */
	public int[] registers;

	/** Memory */
	public MarsMemory memory;

	/** Timer */
	public MarsTimer timer;

	/** Constants */
	public MarsSystemConstant constants;

	/** Ticked? */
	public boolean ticked;

	/** Timer ID */
	public int myID;

	/** Aliveness */
	public boolean dead;

	///////////////////////////////////////////////////////////////////////////
	// Constructor
	///////////////////////////////////////////////////////////////////////////

	public MarsProcessor (MarsMemory mem, MarsSystemConstant consta, MarsTimer tim)
	{
		memory = mem;
		constants = consta;
		registers = new int[NREGISTERS];
		PC = 0;
		handlers = new MarsInterruptHandler[NINTERRUPTS];
		timer = tim;
		dead=false;
		ticked=false;
		myID = timer.addTimerUser((TimerUser)this);
		System.out.println("CPU: assigned timerID: "+myID);
	}

	///////////////////////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////////////////////

	/**
	 * This method registers an interrupt handler for the specified
	 * interrupt number. Once registered, the specific interrupt
	 * handler will be invoked whenever the interrupt is generated.
	 * The semantic of this invocation is similar to that of the
	 * POSIX system call <CODE>signal</CODE>; clearly, while 
	 * <CODE>signal</CODE> is a system call (and thus is done at 
	 * the operating system level, setInterruptHandler is done
	 * at the (simulated) hardware level.
	 */
	public void setInterruptHandler(int irqn, InterruptHandler handler)
	{
		if (irqn < 0 || irqn > NINTERRUPTS)
			throw new IllegalArgumentException();
		handlers[irqn] = (MarsInterruptHandler) handler;
	}

	/**
	 * Changes the value of the program counter.
	 * 
	 * @param address the new value for the program counter
	 */
	public void setPC(int address)
	{
		PC = address % constants.getCoreSize();
	}

	/**
	 * Returns the value of the timerID.
	 *
	 * @return timerID
	 */
	public int getMarsProcessorTimerID()
	{
		return myID;
	}

	/**
	 * Returns the value of the program counter.
	 */
	public int getPC()
	{
		return PC;
	}

	/**
	 * Returns the value contained in the specified register.
	 * 
	 * @param index the index of the registered to be returned
	 * @throws IllegalArgumentException if the specified index
	 * is outside 0 and <CODE>NREGISTERS</CODE>.
	 */
	public int getRegister(int index)
	{
		if (index < 0 || index >= NREGISTERS)
			throw new IllegalArgumentException();
		return registers[index];
	}

	/**
	 * Changes the value of the specified register.
	 * 
	 * @param index the index of the registers to be changed
	 * @param value the new value for the registers
	 * @throws IllegalArgumentException if the specified index
	 * is outside 0 and <CODE>NREGISTERS</CODE>.
	 */
	public void setRegister(int index, int value)
	{
		if (index < 0 || index >= NREGISTERS) 
			throw new IllegalArgumentException();
		registers[index] = value;
	}

	/**
	 * This is the entry point for the thread.
	 */
	public synchronized void run()
	{
		System.out.println("Starting CPU...");
		while(!dead)
		{
			while(!ticked)
				try { this.wait();
				} catch (InterruptedException e) {}
			if (dead)
				break;
			step();
			timer.tickCompleted(myID);
			ticked=false;
		}
		System.out.println("Shutting down CPU...");
	}

	/**
	 * Sets thread dead.
	 */
	public void die()
	{
		System.out.println("Death request ackieved");
		dead=true;
	}

	/**
	 * This method is invoked whenever a timer tick expires. Note that
	 * opportune synchronization mechanisms are missing. You may have
	 * to modify this class to enforce synchronization between timers,
	 * dma and memory.
	 */
	public synchronized void tick()
	{
		ticked=true;
		this.notify();
	}

	/**
	 * This executes a single step of the system call.
	 */
	public void step()
	{
		MarsCell cell = (MarsCell)memory.readCell(PC);
		int opcode =  cell.getOpcode();
		int A = cell.getOperandA();
		int B = cell.getOperandB();

		setPC(PC+1);
		//System.out.println("Letto cella "+PC+"\t- opcode = "+opcode);
		//System.out.println("                 \t- operand A = "+A);
		//System.out.println("                 \t- operand B = "+B);

		if (A < 0 || A >= NREGISTERS || B < 0 || B >= NREGISTERS)
		{
			handlers[2].interrupt(2, 0, 0);
			return;
		}
		switch (opcode)
		{
			case IMM:
				Imm(A, B);
				break;
			case COPY:
				Copy(A, B);
				break;
			case LOADA:
				LoadA(A, B);
				break;	
			case LOADB:
				LoadB(A, B);
				break;
			case STOREA:
				StoreA(A, B);
				break;
			case STOREB:
				StoreB(A, B);
				break;
			case MOVE:
				Move(A, B);
				break;
			case ADD:
				Add(A, B);
				break;
			case SUB:
				Sub(A, B);
				break;
			case AND:
				And(A, B);
				break;
			case OR:
				Or(A, B);
				break;
			case NOT:
				Not(A, B);
				break;
			case JUMP:
				Jump(A);
				break;
			case BGT:
				Bgt(A, B);
				break;
			case BLT:
				Blt(A, B);
				break;
			case BEQ:
				Beq(A, B);
				break;
			case BNE:
				Bne(A, B);
				break;
			case SYSCALL:
				Syscall(A, B);
				break;
			default:
				handlers[2].interrupt(2, 0, 0);
				break;
		}
	}

	private void Imm(int Rx, int value)
	{
		registers[Rx] = value;
	}

	private void Copy(int Rx, int Ry)
	{
		memory.writeCell((PC+registers[Rx]), memory.readCell(PC+registers[Ry]));
	}

	private void LoadA(int Rx, int Ry)
	{
		registers[Rx] = memory.readCell(PC+registers[Ry]).getOperandA();
	}

	private void LoadB(int Rx, int Ry)
	{
		registers[Rx] = memory.readCell(PC+registers[Ry]).getOperandB();
	}

	private void StoreA(int Rx, int Ry)
	{
		memory.readCell(PC+registers[Ry]).setOperandA(registers[Rx]);
	}

	private void StoreB(int Rx, int Ry)
	{
		memory.readCell(PC+registers[Ry]).setOperandB(registers[Rx]);
	}

	private void Move(int Rx, int Ry)
	{
		registers[Ry] = registers[Rx];
	}

	private void Add (int Rx, int Ry)
	{
		registers[Ry] += registers[Rx];
	}

	private void Sub (int Rx, int Ry)
	{
		registers[Ry] -= registers[Rx];
	}

	private void And (int Rx, int Ry)
	{
		registers[Ry] = registers[Ry] & registers[Rx];
	}

	private void Or (int Rx, int Ry)
	{
		registers[Ry] = registers[Ry] | registers[Rx];
	}

	private void Not (int Rx, int Ry)
	{
		registers[Ry] = ~registers[Rx];
	}

	private void Jump (int Ry)
	{
		setPC(PC + registers[Ry]);
	}

	private void Bgt (int Rx, int Ry)
	{
		if (registers[Rx] > 0)
			setPC(PC + registers[Ry]);
	}

	private void Blt (int Rx, int Ry)
	{
		if (registers[Rx] < 0)
			setPC(PC + registers[Ry]);
	}

	private void Beq (int Rx, int Ry)
	{
		if (registers[Rx] == 0)
			setPC(PC + registers[Ry]);
	}

	private void Bne (int Rx, int Ry)
	{
		if (registers[Rx] != 0)
			setPC(PC + registers[Ry]);
	}

	public void Syscall(int A, int B)
	{
		handlers[0].interrupt(0, A, B);
	}
} // END MarsProcessor
