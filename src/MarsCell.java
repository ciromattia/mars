/**
 *  This class contains the implementation of the interface "Cell"
 *  -------------------------------------------------------------
 *  @author Ciro Mattia Gonano <CODE>&lt;gonano@CS.UniBO.It&gt;</CODE>
 *  @author Jacopo Saporetti <CODE>&lt;saporet3@CS.UniBO.It&gt;</CODE>
 *  @author Mauro Seno <CODE>&lt;mseno@CS.UniBO.It&gt;</CODE>
 *  ·····························································
 */


public class MarsCell implements Cell
{
	private int opcode;
	private int firstf;
	private int secondf;
	
	/*
	 * contructor w/ parameters
	 */
	MarsCell ( int op, int a, int b )
	{
		opcode=op;
		firstf=a;
		secondf=b;
	}

	/*
	 * constructor w/out parameters, init to  all values
	 */
	MarsCell()
	{
		opcode=0;
		firstf=0;
		secondf=0;
	}

	/*
	 * clone a cell
	 */
	MarsCell ( MarsCell mc )
	{
		opcode=mc.getOpcode();
		firstf=mc.getOperandA();
		secondf=mc.getOperandB();
	}

	/**
	 * Returns the value of the opcode.
	 */
	public int getOpcode()
	{
		return opcode;
	}

	/**
	 * Returns the value of the first operand.
	 */
	public int getOperandA()
	{
		return firstf;
	}
	/** 
	 * Returns the value of the second operand.
	 */
	public int getOperandB()
	{
		return secondf;
	}

	/**
	 * Sets the value of the opcode.
	 * @param val the new value of the opcode.
	 */
	public void setOpcode ( int val )
	{
		opcode=val;
		System.out.println ("Opcode set to: " + val);
	}

	/** 
	 * Sets the value of the first operand.
	 * @param val the new value of the first operand.
	 */
	public void setOperandA ( int val )
	{
		firstf=val;
		System.out.println( "First operand set to: " + val);
	}

	/**
	 * Sets the value of the second operand.
	 * @param val the new value of teh second operand.
	 */
	public void setOperandB ( int val )
	{
		secondf=val;
		System.out.println( "Second operand set to: " + val);
	}
}
