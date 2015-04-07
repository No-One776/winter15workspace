import java.util.ArrayList;


public class Main {

	// TODO: Use sliding window approach to send/receive packets:
	/*
	 * Choose window size (e.g. 5 packets)
	 * Any packets in the window, the Sener will send
	 * Client also has a same-size window (the Receiver is willing
	 * to receive X amount at a time)
	 * Sender should send X packets, then block on the socket
	 * Receiver should receive X packets, then block on the socket
	 * Receiver should send ACK to Sender, in which case the Sender increments
	 * window counter by 1, moving packet 0 out of the window and packet X+1
	 * into the window.
	 * ** Only receive on packet at index 0 in the window can move the window
	 *  - Have a counter for numReceivedInWindow
	 *  - Have a boolean for firstInWindowReceived
	 *  - When firstInWindowReceived becomes true, move window the maximum length
	 *  possible (up to numReceivedInWindow)
	 */
	
	// TODO: Insert sequence number into data for packet ordering (int)
	/*
	 * Use sequence number in sliding window
	 * - Collections.sort(packets).on(sequenceNumber)
	 */
	
	// TODO: Write packet checksum function and insert into packet
	/*
	 * Maybe use pre-existing checksum function, or use internet
	 * checksum pattern from lecture
	 * - int calculateChecksum(byte[])
	 * - boolean verifyChecksum(byte[], checksum)
	 */
	
	// TODO: Configure Mininet VM
	/*
	 * Username for Mininet vm is: 'mininet'
	 * Password for Mininet is: 'mininet'
	 * 
	 * sudo apt-get update before installing anything
	 * apt-get install default-jdk
	 * 
	 * - Set packet loss rate for the switch:
	 * tc 	qdisc 	add 	dev s1-eth1	root	netem	loss	20%
	 *  		 	change		s1-eth2
	 * 													reorder	20%
	 * 
	 */
}
