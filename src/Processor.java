/**
 *  This interface contains the method of the MARS processor that can be 
 *  invoked by the operating system. In particular, it enables the OS
 *  to set the interrupt handler for a particular interrupt; to set and get
 *  the program counter, to set and get the registers.
 *
 *  @author Alberto Montresor
 */
public interface Processor extends TimerUser
{

  /**
   *  Set the interrupt handler to be used when an interrupt 
   *  request with number <CODE>irqn</CODE> is generated.
   * 
   *  @param irqn the interrupt number identifying the interrupt
   *    type handled by the specified handler.
   *  @param the interrupt handler for the specified interrupt
   *    number.
   */
  public void setInterruptHandler(int irqn, InterruptHandler handler);


  /**
   *  Changes the value of the program counter.
   * 
   *  @param address the new value for the program counter
   */
  public void setPC(int address);


  /**
   *  Returns the value of the program counter.
   */
  public int getPC();

  /**
   *  Returns the value contained in the specified register.
   * 
   *  @param index the index of the registered to be returned
   */
  public int getRegister(int index);

  /**
   *  Changes the value of the specified register.
   * 
   *  @param index the index of the register to be changed
   *  @param value the new value for the register
   */
  public void setRegister(int index, int value);


} // END MarsProcessor

