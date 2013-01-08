/**
 * This is the implementation of the Syscall interface
 * -----------------------------------------------------
 * @author Ciro Mattia Gonano <CODE>&lt;gonano@CS.UniBO.It&gt;</CODE>
 * @author Jacopo Saporetti <CODE>&lt;saporet3@CS.UniBO.It&gt;</CODE>
 * @author Mauro Seno <CODE>&lt;mseno@CS.UniBO.It&gt;</CODE>
 * ····················································
 */

import java.util.LinkedList;

public class MarsSyscallHandler
{
	int maxfiles;		// to avoid calling .getMaximumNumberOfFile() method each time we need it
	FTElement[][] filetable;	// the file table, one for each process
	MarsSystemConstant cons;
	MarsDMA DMA;
	MarsScheduler scheduler;
	MarsStorage storage;

	MarsSyscallHandler ( MarsDMA newDMA, MarsScheduler newsched, MarsStorage newstor )
	{
		cons = new MarsSystemConstant();
		int procno;
		procno = cons.getProcessNum();
		maxfiles = cons.getMaximumNumberOfFile();
		/* init filetable for each process */
		filetable = new FTElement[procno][maxfiles];
		for (int i=0; i<procno; i++)
			for (int j=0; j<maxfiles; j++)
				filetable[i][j] = new FTElement(-1);
		DMA=newDMA;
		scheduler=newsched;
		storage=newstor;
	}

	/**
	 * Returns the first free file descriptor.
	 *
	 * @param PID the calling process' id
	 * 
	 * @return a positive fd if a free fd is found, -1 if no fds are available
	 */
	public int getFirstFreeFD ( int PID )
	{
		for (int i=0; i<maxfiles; i++)
			if (filetable[PID][i].fileIndex == -1)
				return i;
		return -1;
	}

	/**
	 * OPEN: opens a file.
	 * 
	 * @param fileIndex the file's index in storage
	 * @param mode 0: read-only; 1: read-write
	 * @param PID the running process' ID
	 * @param TID the calling thread's ID 
	 * 
	 * @return fd greater than 0 if the file exists and it has been opened
	 * @return -1 if the file does not exist
	 * @return -2 if the maximum open files number per warrior has already been reached
	 * @return -3 if the file is already open in write mode
	 */
	public int open ( int fileIndex, boolean mode, int PID, int TID )
	{
		boolean alreadyopen;
		int freefd;
		MarsFile file = storage.getMarsStorageFile(fileIndex);
		alreadyopen = file.getMarsFileIsOpen();
		if ( ((freefd=getFirstFreeFD(PID))==-1) && (mode||(!alreadyopen)) ) {
			System.out.println("Maximum number of open files reached for process " + PID + ".");
			return -2;
		}
		if ( file.equals((MarsFile)null) ) {
			System.out.println("Process " + PID + " trying to open a nonexisting file!");
			return -1;
		}
		else if ( file.getMarsFileOwner() != PID )
			throw new SecurityException("Process " + PID + " trying to open a file in which it does not belong!");
		else if (!file.setMarsFileIsOpen(true,mode)) {
			System.out.println("Process " + PID + " trying to open an already opened in write mode file!");
			return -3;
		}
		if ( mode || (!alreadyopen) ) {	// it doesn't exist a valid fd for the file, yet
			filetable[PID][freefd].fileIndex=fileIndex;
			filetable[PID][freefd].filesize=file.getMarsFileSize();
			if (mode)
				filetable[PID][freefd].writerID=TID;
			else
				filetable[PID][freefd].readersID.add(new Integer(TID));
			return freefd;
		}
		else {	// there is already an fd open for that file: let's use the same...
			int i;
			for (i=0; i<maxfiles; i++)
				if (filetable[PID][i].fileIndex==fileIndex)
					break;
			(filetable[PID][i].readersID).add(new Integer(TID));
			return i;
		}
	}

	/**
	 * Reads <CODE>size</CODE> cells from the file specified by <CODE>file</CODE>,
	 * starting from the current position, placing
	 * them in the memory address specified by <CODE>address</CODE>.
	 * This syscall is BLOCKING.
	 * 
	 * @param fd the file descriptor that identifies the file where to read cells
	 * @param address where to place read cells
	 * @param size number of cell to be read
	 * @param PID the calling process' ID
	 * @param TID the calling thread's ID
	 *
	 * @return number of read cells, or -1 if the call was invalid
	 */
	public int read ( int fd, int address, int size, int PID, int TID )
	{
		if ( (fd<0) ||
				(fd>=maxfiles) ||
				( ! filetable[PID][fd].readersID.contains(new Integer(TID)) ) ) {
			System.out.println("read: attempt to read an invalid file!");
			System.out.println("=====>>> KILLING calling thread...");
			return -1;
		}
		int fileIndex;
		int offset;
		int readcells = 0;
		fileIndex = filetable[PID][fd].fileIndex;
		offset = filetable[PID][fd].currentPos;
		if ( (offset+size)>filetable[PID][fd].filesize ) {
			System.out.println("read: attempt to read out of bounds.");
			System.out.println("=====>>> KILLING calling thread...");
			return -1;
		}
		DMA.read(fileIndex,offset,size,address);
		return size;
	}

	/** 
	 * Writes <CODE>size</CODE> cells from memory address <CODE>address</CODE>
	 * in the file identified by <CODE>fileID</CODE>.
	 * This syscall is BLOCKING.
	 * 
	 * @param fd the file descriptor that identifies the file where to write cells
	 * @param address where to read cells
	 * @param size number of cell to be written
	 * @param PID the calling process' ID
	 * @param TID the calling thread's ID
	 *
	 * @return the number of written cells, or -1 elsewhere
	 */
	public int write ( int fd, int address, int size, int PID, int TID )
	{
		if ( (fd<0) ||
				(fd>=maxfiles) ||
				(filetable[PID][fd].writerID!=TID) ) {
			System.out.println("write: attempt to write an invalid file!");
			System.out.println("=====>>> KILLING calling thread...");
			return -1;
		}
		int fileIndex;
		int offset;
		int readcells = 0;
		fileIndex = filetable[PID][fd].fileIndex;
		offset = filetable[PID][fd].currentPos;
		DMA.write(fileIndex,offset,size,address);
		return size;
	}

	/**
	 * Close a file. 
	 *
	 * @param fd the file to close's descriptor
	 *
	 * @return -1 if fd isn't used, 0 elsewhere
	 */
	public synchronized int close ( int fd, int PID, int TID )
	{
		if ( (fd<0) ||
				(fd>=maxfiles) ||
				(filetable[PID][fd].fileIndex<0) ) {
			System.out.println("close: invalid file descriptor");
			return -1;
		}
		int fileIndex;
		fileIndex = filetable[PID][fd].fileIndex;
		MarsFile file = storage.getMarsStorageFile(fileIndex);
		if ( (filetable[PID][fd].writerID>0 ) ||	// file was open in write mode by only one thread OR
				(filetable[PID][fd].readersID.size()==1)	// there's only a reader (the calling)
		   ) {
			if ( (filetable[PID][fd].writerID!=TID) || 
				( !(filetable[PID][fd].readersID.get(1).equals(new Integer(TID))) )
				) {
				System.out.println("close: attempt to close a file in which thread doesn't belongs");
				return -1;	//C. nota: chiamare eccezione? uccidere thread?
			}
			if (!file.setMarsFileIsClosed())
				throw new RuntimeException("close: something went wrong...");
			filetable[PID][fd].fileIndex=-1;
			filetable[PID][fd].writerID=-1;
			filetable[PID][fd].currentPos=0;
			filetable[PID][fd].filesize=0;
			filetable[PID][fd].readersID=new LinkedList();
		}
		else {	// calling thread is not the last to close the file...
			if (file.setMarsFileIsClosed())
				throw new RuntimeException("close: something went wronger...");
			filetable[PID][fd].readersID.remove(new Integer(TID));
		}
		return 0;
	}
}

class FTElement
{
	public int fileIndex;
	public int writerID;
	public LinkedList readersID;
	public int currentPos;
	public int filesize;

	FTElement ( int newFileIndex )
	{
		fileIndex = newFileIndex;
		writerID = -1;
		readersID = new LinkedList();
		currentPos = 0;
	}
}

