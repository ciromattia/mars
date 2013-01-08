/**
 *  Implementation constants.
 * 
 *  @author Giovanni Melaga
 *  ·······································································*/


public class SystemConstant
{
  private final int CoreSize = 8192;
  private final int ContextSwitchBeforeTie = 20000;
  private final int Quantum = 10;
  private final int MaximumNumberOfFile = 16;
  private final int MaxReadWrite = 8;
  private final int MaximumNumberOfTask = 8;
  private final int MinimumSeparation = 2048;
  private final int WarriorsNumber = 3;
  
  public int getCoreSize()
  {
    return CoreSize;
  }
  
  public int getContextSwitchBeforeTie()
  {
    return ContextSwitchBeforeTie;
  }
  
  public int getQuantum()
  {
    return Quantum;
  }
  
  public int getMaximumNumberOfFile()
  {
    return MaximumNumberOfFile;
  }
  
  public int getMaxReadWrite()
  {
    return MaxReadWrite;
  }
  
  public int getMaximumNumberOfTask()
  {
    return MaximumNumberOfTask;
  }
  
  public int getMinimumSeparation()
  {
    return MinimumSeparation;
  }
  
  public int getWarriorsNumber()
  {
    return WarriorsNumber;
  }
}

