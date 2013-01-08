/** 
 * This class is the implementation of the class "Timer"
 * -----------------------------------------------------
 * @author Ciro Mattia Gonano <CODE>&lt;gonano@CS.UniBO.It&gt;</CODE>
 * @author Jacopo Saporetti <CODE>&lt;saporet3@CS.UniBO.It&gt;</CODE>
 * @author Mauro Seno <CODE>&lt;mseno@CS.UniBO.It&gt;</CODE>
 * ·························································
 */

import java.util.ArrayList;

public class MarsTimer extends Thread implements Timer
{
	
	private int comptosynch;
	private int tasktocomplete;
	private int totalticks;
	private MarsInterruptHandler handler;
	private ArrayList timerusers;
	private boolean dead;
	private boolean stopped;
	private ArrayList stoppedtu;

	public MarsTimer()
	{
		comptosynch=0;
		timerusers=new ArrayList();
		stoppedtu=new ArrayList();
		stopped=false;
		dead=false;
	}

	/**
	 * Entry point for the thread.
	 */
	public synchronized void run()
	{
		System.out.println("Starting timer...");
		handler.interrupt(1,1,1);
		while(!dead)
		{
			while (totalticks>0) {
				tasktocomplete=comptosynch;
				while (!stopped && totalticks>0) {
					for (int i=0; i<comptosynch; i++)
						if (stoppedtu.get(i).equals(new Boolean(false)))
							((TimerUser)timerusers.get(i)).tick();
					totalticks--;
					while (tasktocomplete>0)
						;
					//System.out.println("Task completed for all the component to synchronize");
				}
			}
			//System.out.println("Timeout expired for this Task");
			handler.interrupt(1,1,1);
		}
		System.out.println("Shutting down timer...");
	}

	/**
	 * Set a timeouts that will expire after the specified number of
	 * ticks, awaking the specified interrupt handler.
	 * @param ticks, an integer, set the timeout
	 * @param ehandler set the proper interrupt handler
	 */
	public void setTimeout ( int ticks, InterruptHandler ehandler )
	{
		totalticks = ticks;
		handler = (MarsInterruptHandler) ehandler; //C. questo dovrebbe essere sempre interrupt 1, se non vado errato...
	}

	/**
	 * Notify the timer that the specified component has completed
	 * its task for the current tick.
	 *  
	 * @param component the id of the component that has completed the task
	 */
	public void tickCompleted ( int component )
	{
		tasktocomplete--;
	}

	/**
	 * Adds a new TimerUser to the TimerUsers list.
	 *
	 * @param tu a TimerUser object
	 *
	 * @return the ID of the added TimerUser
	 */
	public int addTimerUser ( TimerUser tu )
	{
		timerusers.add(tu);
		stoppedtu.add(new Boolean(false));
		comptosynch++;
		return (timerusers.size()-1);
	}

	/**
	 * Stops the timer.
	 */
	public void stopTimer()
	{
		stopped=true;
	}

	/**
	 * Restarts the timer.
	 */
	public void restartTimer()
	{
		stopped=false;
	}

	/**
	 * Stops only a TimerUser.
	 *
	 * @param tuID the timer user's ID
	 */
	public synchronized void stopTimerUser ( int tuID )
	{
		stoppedtu.set(tuID,new Boolean(true));
	}

	/**
	 * Restart a previously stopped TimerUser.
	 *
	 * @param tuID the timer user's ID
	 */
	public synchronized void restartTimerUser ( int tuID )
	{
		stoppedtu.set(tuID,new Boolean(false));
	}

	/**
	 * Shutdown every timeruser, then die.
	 */
	public void shutdown()
	{
		for (int i=0; i<comptosynch; i++)
			((TimerUser)timerusers.get(i)).die();
		for (int i=0; i<comptosynch; i++)
			((TimerUser)timerusers.get(i)).tick();
		dead=true;
		stopped=false;
	}
}
