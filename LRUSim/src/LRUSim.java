import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author rohrj
 * @description: A system that uses memory management and page replacement to
 *               implement virtual memory.
 */
public class LRUSim {
	private BufferedReader fileToRead;
	private final int numProcesses = 6, physicalMemSize = 16;
	private Records[] processInfo = new Records[numProcesses];
	private PageTable[] pageTable = new PageTable[numProcesses];
	private PageFrameTable[] frameTable = new PageFrameTable[physicalMemSize];
	private int framePointer = 0, pID = 0, pageRef = 0, victimPID = -1,
			victimRef = -1;

	public int getVictimPID() {
		return victimPID;
	}

	public int getVictimRef() {
		return victimRef;
	}

	private boolean faulted = false;

	public LRUSim(String string) {
		initialize(string);
	}

	public int getpID() {
		return pID;
	}

	public int getPageRef() {
		return pageRef;
	}

	public void runNextLine() {
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
		}
	}

	public void runTillNextFault() {
		faulted = false;
		String in = null;
		while (!faulted) { // While not faulted, read new lines
			try {
				in = fileToRead.readLine();
			} catch (IOException e) {
				System.out.println("Error reading file");
			}
			if (in != null) {
				parseData(in);
				storeRecords();
				lruImplementation();
			}
		}
	}

	public void runTillEnd() {
		String in;
		try {
			while ((in = fileToRead.readLine()) != null) {
				parseData(in);
				storeRecords();
				lruImplementation();
			}
		} catch (IOException e) {
			System.out.println("Error reading file");
		}
		printStatus(-1);
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

	// Print the Page Table of id, if -1, all and the Page Frame Table
	private void printStatus(int id) {
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

	// Initialize the file reader and arrays for use
	private void initialize(String fileName) {
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

	public Records[] getProcessInfo() {
		return processInfo;
	}

	public PageTable[] getPageTable() {
		return pageTable;
	}

	public PageFrameTable[] getFrameTable() {
		return frameTable;
	}

	// Update the record information for the overall simulation statistics
	private void storeRecords() {
		System.out.println("P" + pID + " accessing page #" + pageRef);
		if (pageRef >= processInfo[pID].totalPages)
			processInfo[pID].totalPages = pageRef + 1;
		processInfo[pID].memRefs++;
	}

	// Parse the string into the int id and the page reference number
	private void parseData(String in) {
		String[] parts = in.split(":");
		pageRef = Integer.parseInt(parts[1].substring(1), 2);
		parts = parts[0].split("P");
		pID = Integer.parseInt(parts[1]);
	}

	// Algorithm to find a free frame to put a frame or clear one to use
	private void lruImplementation() {
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

	// Takes the given spot and updates the frame table & process page table
	private void updatePageFrameTable(int pointer) {
		faulted = true;
		victimPID = frameTable[pointer].procId;
		victimRef = frameTable[pointer].pageRef;
		frameTable[pointer].procId = pID;
		frameTable[pointer].pageRef = pageRef;
		frameTable[pointer].recentlyUsed = true;
		pageTable[pID].pagesFrame[pageRef] = pointer;
		processInfo[pID].pageFaults++;
	}
}

class Records {
	// If the pages number is greater than or equal this, make this pageNum + 1;
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
