/**
 *  This interface represents the DMA mechanism whose task is to
 *  perform read/write operations from/to the storage and the
 *  memory on behalf of the operatin system.
 *
 *  @author Alberto Montresor
 */
public interface DMA extends TimerUser
{

  /**
   *  Set the interrupt handler to be used when an interrupt 
   *  request with number irqn is generated.
   */
  public void setInterruptHandler(int irqn, InterruptHandler handler);


  /**
   *  Moves <CODE>size</CODE> cells from the file identified by
   *  <CODE>file</CODE> starting from the file position specified 
   *  by <CODE>offset</CODE>, to the memory position specified
   *  by <CODE>address</CODE>.
   */
  public void read(int file, int offset, int size, int address);


  /**
   *  Moves <CODE>size</CODE> cells from the memory position specified
   *  by <CODE>address</CODE>, to the file identified by 
   *  <CODE>file</CODE> starting from the file position specified 
   *  by <CODE>offset</CODE>
   */
  public void write(int file, int offset, int size, int address);


} // END MarsProcessor

