import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * A simulation system that uses memory management and page replacement to
 * implement virtual memory with demand paging page replacement.
 * 
 * @author rohrj
 * @version 1.0
 */
public class LRUSim {
	private static BufferedReader fileToRead;
	private final static int numProcesses = 6;
	private final static int physicalMemSize = 16;
	private static Records[] processInfo = new Records[numProcesses];
	private static PageTable[] pageTable = new PageTable[numProcesses];
	private static PageFrameTable[] frameTable = new PageFrameTable[physicalMemSize];
	private static int framePointer = 0;
	private static int pID = 0;
	private static int pageRef = 0;
	private static int victimPID = -1;
	private static int victimRef = -1;
	private static boolean faulted = false;
	private static boolean done = false;

	/**
	 * Constructor that initializes the program with the given String fileName
	 * for the program to read input from.
	 * 
	 * @param fileName
	 *            The name of the file to read from.
	 */
	public LRUSim(String fileName) {
		initialize(fileName);
	}

	/**
	 * This allows the program to be run in a terminal and have a text display.
	 * 
	 * @param args
	 *            The file for the program to read input from.
	 */
	public static void main(String[] args) {
		if (args.length > 0)
			initialize(args[0]);
		runTillEnd();
		printStatus(-1);
		printStats();
	}

	/**
	 * Method to be invoked to run the next line of the simulation from the text
	 * file and update necessary information for displaying or printing.
	 */
	public static void runNextLine() {
		String in = null;
		try {
			in = fileToRead.readLine();
		} catch (IOException e) {
			System.out.println("Error reading file");
		}
		if (in != null) {
			parseData(in);
			storeRecords();
			lruImplementation();
		} else
			done = true;
	}

	/**
	 * Method to be invoked to run through more lines in the simulation until a
	 * fault happens.
	 */
	public void runTillNextFault() {
		faulted = false;
		while (!faulted)
			// While not faulted, read new lines
			runNextLine();

	}

	/**
	 * Method to be invoked to read through the file until the simulation is
	 * done (reached EOF).
	 */
	public static void runTillEnd() {
		while (!done)
			runNextLine();
	}

	/**
	 * Prints the statistics for all the processes simulation run information.
	 * This includes the total pages per process, the number of memory
	 * references, the amount of page faults, and the amount of non page faults.
	 */
	private static void printStats() {
		for (int x = 1; x < numProcesses; x++) {
			System.out.print("Process #" + x + " Total Page Size: "
					+ processInfo[x].totalPages + "\tTotal Memory References: "
					+ processInfo[x].memRefs);
			System.out.print("\tTotal Page Faults for P" + x + ": "
					+ processInfo[x].pageFaults);
			System.out.println("\tTotal Non-Page Faults for P" + x + ": "
					+ processInfo[x].nonPageFaults);
		}

	}

	/**
	 * Print the Page Table of the input id, if it's -1, then print all and the
	 * Page Frame Table (Physical Memory)
	 * 
	 * @param id
	 *            The Process page table to print
	 */
	private static void printStatus(int id) {
		System.out.println("Page Tables");
		for (int x = 1; x < numProcesses; x++) {
			if (id == x || id == -1) {
				System.out.println("Process " + x + ":\nPage Frame");
				for (int y = 0; y < physicalMemSize; y++)
					if (pageTable[x].pagesFrame[y] != -1)
						System.out.println(y + "\t"
								+ pageTable[x].pagesFrame[y]);
			}
		}
		System.out.println("Page Frame Table\nFrame# ProcID Page#");
		for (int x = 0; x < physicalMemSize; x++)
			if (frameTable[x].procId != -1)
				System.out.println(x + "\t" + frameTable[x].procId + "\t"
						+ frameTable[x].pageRef);
	}

	/**
	 * Initialize the FileReader and the data arrays for use.
	 * 
	 * @param fileName
	 *            The file name to read from
	 */
	private static void initialize(String fileName) {
		try { // Create Readers
			FileReader file = new FileReader(fileName);
			fileToRead = new BufferedReader(file);
		} catch (FileNotFoundException e) {
			System.out.println("Error Creating Read File...");
		}
		// Instantiate Arrays for use
		for (int r = 0; r < numProcesses; r++) {
			processInfo[r] = new Records();
			pageTable[r] = new PageTable();
		}
		for (int m = 0; m < physicalMemSize; m++) {
			for (int r = 0; r < numProcesses; r++)
				pageTable[r].pagesFrame[m] = -1;
			frameTable[m] = new PageFrameTable();
		}
	}

	/**
	 * Update the record information for the overall simulation statistics of a
	 * processes total page size and the number of memory references.
	 */
	private static void storeRecords() {
		System.out.println("P" + pID + " accessing page #" + pageRef);
		if (pageRef >= processInfo[pID].totalPages)
			processInfo[pID].totalPages = pageRef + 1;
		processInfo[pID].memRefs++;
	}

	/**
	 * Parses the given string into the Process ID and the Page the process is
	 * referencing.
	 * 
	 * @param in
	 *            String data to parse
	 */
	private static void parseData(String in) {
		String[] parts = in.split(":");
		pageRef = Integer.parseInt(parts[1].substring(1), 2);
		parts = parts[0].split("P");
		pID = Integer.parseInt(parts[1]);
	}

	/**
	 * The Least Recently Used algorithm for page replacement is implemented by
	 * having each page frame table entry have a bit set for keeping track of
	 * how recently used it is. It is implemented with a pointer to the next
	 * spot in physical memory to be checked where if it is empty, place the
	 * request there. If the position isn’t and the LRU bit is set to true, then
	 * set it to false and move to the next spot; if it is set to false, then
	 * clear the victim process’ page accordingly and place the new request in
	 * that spot.
	 */
	private static void lruImplementation() {
		if (pageTable[pID].pagesFrame[pageRef] != -1) { // Already Paged
			frameTable[pageTable[pID].pagesFrame[pageRef]].recentlyUsed = true;
			processInfo[pID].nonPageFaults++;
			victimPID = -1;
			victimRef = -1;
		} else { // Not already paged, find a spot to place it
			boolean notPlaced = true;
			while (notPlaced) {
				if (frameTable[framePointer].procId == -1) {
					updatePageFrameTable(framePointer);
					notPlaced = false;
				} else if (!frameTable[framePointer].recentlyUsed) {
					pageTable[frameTable[framePointer].procId].pagesFrame[frameTable[framePointer].pageRef] = -1;
					updatePageFrameTable(framePointer);
					notPlaced = false;
					System.out
							.println("No free frames, replacing physical page at: "
									+ framePointer);
				} else if (frameTable[framePointer].recentlyUsed)
					frameTable[framePointer].recentlyUsed = false;
				framePointer++;
				if (framePointer == physicalMemSize)
					framePointer = 0;
			}
			printStatus(pID);

		}
	}

	/**
	 * Takes the given location pointer to update the physical memory page to
	 * the new process and page. It sets victim information for display use and
	 * also sets a boolean for the simulation to know when it's had a page fault
	 * occur.
	 * 
	 * @param pointer
	 *            The physical memory location to update
	 */
	private static void updatePageFrameTable(int pointer) {
		faulted = true;
		victimPID = frameTable[pointer].procId;
		victimRef = frameTable[pointer].pageRef;
		frameTable[pointer].procId = pID;
		frameTable[pointer].pageRef = pageRef;
		frameTable[pointer].recentlyUsed = true;
		pageTable[pID].pagesFrame[pageRef] = pointer;
		processInfo[pID].pageFaults++;
	}

	/**
	 * Used for telling when the simulation has read all available lines of
	 * input.
	 * 
	 * @return true if simulation has read all the lines in the input file,
	 *         false if not
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * Gives the value of the victim process id for the display to use.
	 * 
	 * @return The victim process id.
	 */
	public int getVictimPID() {
		return victimPID;
	}

	/**
	 * Gives the value of the victim process page reference for the display to
	 * use.
	 * 
	 * @return The victim page.
	 */
	public int getVictimRef() {
		return victimRef;
	}

	/**
	 * Returns the current process ID to be displayed.
	 * 
	 * @return The current process ID.
	 */
	public int getpID() {
		return pID;
	}

	/**
	 * Returns the current page to be displayed.
	 * 
	 * @return The current page being referenced.
	 */
	public int getPageRef() {
		return pageRef;
	}

	/**
	 * Used to get current structure data for display purposes.
	 * 
	 * @return The processInfo Record data structure.
	 */
	public Records[] getProcessInfo() {
		return processInfo;
	}

	/**
	 * Used to get current page tables for every process to display.
	 * 
	 * @return The PageTable data structure.
	 */
	public PageTable[] getPageTable() {
		return pageTable;
	}

	/**
	 * Used to get current Page Frame Table (physical memory) data to display.
	 * 
	 * @return The PageFrameTable data structure.
	 */
	public PageFrameTable[] getFrameTable() {
		return frameTable;
	}

}

class Records {
	// If the pages number is greater than or equal than totalPages, make it
	// pageRef + 1;
	public int totalPages = 0;
	public int memRefs = 0;
	public int pageFaults = 0;
	public int nonPageFaults = 0;
}

class PageTable {
	// PageTable[ProcessID].pagesFrame[pageNumber] = the pages frame number
	public int[] pagesFrame = new int[16];
}

class PageFrameTable {
	public int pageRef = -1;
	public int procId = -1;
	public boolean recentlyUsed = false;
}
