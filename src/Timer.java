/**
 *  This interface represents a timer. The task of a Timer is to
 *  emit ticks to all the components of the system, to keep them
 *  synchronized. In particular, it emits a tick to the processor(s)
 *  and to the DMA. The components of the system complete the task
 *  that have to be handled during that tick, and then invoke method
 *  tickCompleted() on the timer. When the timer receives a 
 *  tickCompleted invocation for each of the components in the 
 *  system, it emits the next tick.
 *  
 *  Moreover, Timer enables the operating system to set timeouts;
 *  this facility is used to awake the operating system after
 *  a quantum has expired.
 *
 *  @author Alberto Montresor
 */
public interface Timer
{

  /**
   *  Set a timeouts that will expire after the specified number of
   *  ticks, awaking the specified interrupt handler.
   *  
   */
  public void setTimeout(int ticks, InterruptHandler ehandler);
  
  
  /**
   *  Notify the timer that the specified component has completed
   *  its task for the current tick.
   */
  public void tickCompleted(int component);

} // END Timer

