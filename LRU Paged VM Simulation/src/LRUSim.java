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
	private static BufferedReader fileToRead;
	private static final int numProcesses = 6;
	private static final int physicalMemSize = 16;
	private static int pageFaults = 0, nonPageFaults = 0;
	private static Records[] processInfo = new Records[numProcesses];
	private static PageTable[] pageTable = new PageTable[numProcesses];
	private static PageFrameTable[] frameTable = new PageFrameTable[physicalMemSize];
	private static int framePointer = 0, pID = 0, pageRef = 0;

	public static void main(String[] args) {
		if (args.length > 0)
			initialize(args[0]);

		String in;
		try {
			while ((in = fileToRead.readLine()) != null) {
				parseData(in);
				System.out.println("P" + pID + " accessing page #" + pageRef);
				storeRecords();
				lruImplementation();
			}
		} catch (IOException e) {
			System.out.println("Error reading file");
		}
		System.out.println("Total Page Faults: " + pageFaults);
		System.out.println("Total Non-Page Faults: " + nonPageFaults);
		for (int x = 1; x < numProcesses; x++)
			System.out.println("Process #" + x + "\tTotal Page Size: "
					+ processInfo[x].totalPages + "\tTotal Memory References: "
					+ processInfo[x].memRefs);
	}

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

	// Update the record information for the overall simulation statistics
	private static void storeRecords() {
		if (pageRef >= processInfo[pID].totalPages)
			processInfo[pID].totalPages = pageRef + 1;
		processInfo[pID].memRefs++;
	}

	private static void parseData(String in) {
		String[] parts = in.split(":");
		pageRef = Integer.parseInt(parts[1].substring(1), 2);
		parts = parts[0].split("P");
		pID = Integer.parseInt(parts[1]);
	}

	private static void lruImplementation() {
		if (pageTable[pID].pagesFrame[pageRef] != -1) { // Already Paged
			frameTable[pageTable[pID].pagesFrame[pageRef]].recentlyUsed = true;
			nonPageFaults++;
		} else { // If there are free frames, just add it, else loop
			boolean notPlaced = true;
			while (notPlaced) {
				if (frameTable[framePointer].procId == -1) {
					updatePageFrameTable(framePointer);
					notPlaced = false;
				} else if (!frameTable[framePointer].recentlyUsed) {
					pageTable[frameTable[framePointer].procId].pagesFrame[frameTable[framePointer].pageRef] = -1;
					updatePageFrameTable(framePointer);
					notPlaced = false;
				} else if (frameTable[framePointer].recentlyUsed) {
					System.out.println("Clearing Recently Used #"
							+ framePointer);
					frameTable[framePointer].recentlyUsed = false;
				}
				framePointer++;
				if (framePointer == physicalMemSize)
					framePointer = 0;
			}
		}
	}

	private static void updatePageFrameTable(int pointer) {
		frameTable[pointer].procId = pID;
		frameTable[pointer].pageRef = pageRef;
		frameTable[pointer].recentlyUsed = true;
		pageTable[pID].pagesFrame[pageRef] = pointer;
		pageFaults++;
	}
}

class Records {
	// If the pages number is greater than or equal this, make this pageNum + 1;
	public int totalPages = 0;
	public int memRefs = 0;
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