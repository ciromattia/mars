/**
 *  This class contains the extension of the system constants
 *  -------------------------------------------------------------
 *  @author Ciro Mattia Gonano <CODE>&lt;gonano@CS.UniBO.It&gt;</CODE>
 *  @author Jacopo Saporetti <CODE>&lt;saporet3@CS.UniBO.It&gt;</CODE>
 *  @author Mauro Seno <CODE>&lt;mseno@CS.UniBO.It&gt;</CODE>
 *  ·····························································
 */

public class MarsSystemConstant extends SystemConstant
{
	/* for the processor */
	private final int RegistersNum = 32;
	private final int InterruptNum = 4;

	/* for the Scheduler */
	private final int ProcessNum = 3;
	private final int PriorityLevels = 5;

	public int getRegistersNum() {return RegistersNum;}
	public int getInterruptNum() {return InterruptNum;}
	public int getProcessNum() {return ProcessNum;}
	public int getPriorityLevels() {return PriorityLevels;}
}
