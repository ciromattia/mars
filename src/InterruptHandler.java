/**
 *  This interface contains the entry point for the the interrupt handler.
 *
 *  @author Alberto Montresor
 */
public interface InterruptHandler
{

  /**
   *  This method is invoked by the MARS processor whenever an
   *  interrupt is invoked.
   */
  public void interrupt(int irqn, int data1, int data2);

} // END InterruptHandler

