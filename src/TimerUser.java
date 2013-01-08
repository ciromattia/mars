/**
 *  The TimerUser interface is implemented by the component of the
 *  system that need to be synchronized by the Timer. Whenever a
 *  tick of the timer occurs, the timer invokes method tick on
 *  every component of the system. The component responds by
 *  performing its task for the tick and then by invoking method
 *  tickCompleted() on the Timer.
 *
 *  @author Alberto Montresor
 */
public interface TimerUser
{

  /**
   *  Invoked by the timer to notify the object that a new tick
   *  has been emitted.
   */
  public void tick();

  /**
   * Invoked by the timer to let all timeruser die.
   */
  public void die();

} // END TimerUser

