/**
 *  This interface represents a single cell stored in the memory.
 *
 *  @author Alberto Montresor
 */
public interface Cell
{

  /**
   *  Returns the opcode stored in this cell. 
   */
  public int getOpcode();

  /**
   *  Returns the operand A stored in this cell.
   */
  public int getOperandA();

  /**
   *  Returns the operand B stored in this cell.
   */
  public int getOperandB();

  /**
   *  Changes the value of the opcode stored in this cell. 
   */
  public void setOpcode(int val);

  /**
   *  Changes the value of the operand A stored in this cell.
   */
  public void setOperandA(int val);

  /**
   *  Changes the value of the operand B stored in this cell.
   */
  public void setOperandB(int val);


} // END Cell

