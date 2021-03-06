import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

class FileServer2 {

	static final int maxWindowSize = 5;
	static int windowStartIndex = 0;
	static int nextSequenceNumber = 0;
	static ArrayList<UDPPacket> window = new ArrayList<UDPPacket>();

	static synchronized int getWindowSize() {
		return window.size();
	}

	public static void updateWindow() {

		int startIndex = 0;

		for (int index = 0; index < getWindowSize(); index++) {
			synchronized (window) {
				UDPPacket packet = window.get(index);
				if (index == startIndex && packet == null) {
					window.remove(index);
					windowStartIndex++;
					startIndex = index + 1;
				}
			}
		}
	}

	public static void main(String argv[]) throws Exception {

		final String fileDirectory = new File(".").getCanonicalPath() + "/";

		InetAddress clientAddress;
		int clientPort;

		DatagramSocket serverSocket = new DatagramSocket(9876);

		while (true) {

			windowStartIndex = 0;
			nextSequenceNumber = 0;

			System.out.println("Waiting for new request...");

			byte[] recvData = new byte[1024];

			DatagramPacket recvPacket = new DatagramPacket(recvData,
					recvData.length);

			// Server will wait 60 seconds for a file request
			try {
				serverSocket.setSoTimeout(60000);
				serverSocket.receive(recvPacket);
			} catch (SocketTimeoutException e) {
				System.out.println("No file request received, shutting down.");
				System.exit(0);
			}
			String filePath = new String(recvData);
			filePath = filePath.trim();
			String fullFilePath = fileDirectory + filePath;

			clientAddress = recvPacket.getAddress();
			clientPort = recvPacket.getPort();
			System.out
					.println("Ready to read requested file... waking up file thread...");
			FileHandler fileHandler = new FileHandler(serverSocket,
					clientAddress, clientPort, fullFilePath);
			fileHandler.start();
			fileHandler.startFileRead();

			// While the file reader thread has not finished reading the file
			// and we are still waiting for acks from client
			while (fileHandler.thread.isAlive()
					|| windowStartIndex < nextSequenceNumber) {

				System.out.println("Receiving... "
						+ fileHandler.thread.isAlive() + " || "
						+ windowStartIndex + " || " + nextSequenceNumber);

				byte[] recvData2 = new byte[2048];
				DatagramPacket recvPacket2 = new DatagramPacket(recvData2,
						recvData2.length);
				try {
					// TODO: Change this shitty timeout to FIN messages from
					// server, client will ack the FIN, then we know we're both
					// done
					serverSocket.setSoTimeout(2000);
					serverSocket.receive(recvPacket2);
					byte[] finalData = new byte[recvPacket2.getLength()];
					System.arraycopy(recvData2, 0, finalData, 0,
							finalData.length);
					UDPPacket packet = new UDPPacket(finalData);

					boolean checksumValid = packet.checksumValid();

					if (checksumValid) {

						System.out.println("Got Ack packet seq #: "
								+ packet.sequenceNumber);

						for (int i = 0; i < FileServer2.getWindowSize(); i++) {
							synchronized (window) {
								UDPPacket pack = FileServer2.window.get(i);
								if (pack != null
										&& pack.sequenceNumber == packet.sequenceNumber)
									FileServer2.window.set(i, null);
							}
						}

						System.out
								.println("Window start index, nextSequenceNumber: "
										+ windowStartIndex
										+ " || "
										+ nextSequenceNumber);

						FileServer2.updateWindow();

						// If there is room in the window and the file
						// reader is waiting (previously window full), start it
						// again
						if (getWindowSize() != maxWindowSize
								&& fileHandler.getWindowFull()) {
							// System.out
							// .println("Window has space and file reader waiting... starting file reader");
							fileHandler.startFileRead();
						}
					} else {
						System.out
								.println("Checksum for ack packet from client was invalid. Doing nothing!");
					}

				} catch (SocketTimeoutException e) {
					System.out
							.println("Client stopped sending acks, quitting file send");
					break;
				}
			}
		}
	}
}

class FileHandler implements Runnable {

	DatagramSocket clientSocket;
	InetAddress clientAddress;
	int clientPort;
	Thread thread;
	// 1016 + seqNum + checksum = 1024
	byte[] buffer = new byte[1016];
	FileInputStream fileInputStream;
	private int fileRead;
	private boolean windowFull = false;

	public boolean getWindowFull() {
		return windowFull;
	}

	FileHandler(DatagramSocket connection, InetAddress clientAddress,
			int clientPort, String filePath) throws Exception {

		this.fileInputStream = new FileInputStream(filePath);
		this.clientSocket = connection;
		this.clientAddress = clientAddress;
		this.clientPort = clientPort;
	}

	public void start() {

		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public void stopFileRead() {
		windowFull = true;
	}

	synchronized void startFileRead() {
		windowFull = false;
		// TODO: comment this out when we are done coding packet timeouts OR do
		// we still need it
		notify();
	}

	private void sendPacket(UDPPacket packet) {
		byte[] contents = packet.getContents();
		DatagramPacket sendPacket = new DatagramPacket(contents,
				contents.length, clientAddress, clientPort);
		try {
			clientSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		try {
			while ((fileRead = fileInputStream.read(buffer)) != -1) {

				synchronized (this) {
					while (windowFull) {
						System.out
								.println("Window full... checking packet timeouts now.");
						long currentTime = System.currentTimeMillis();
						synchronized (FileServer2.window) {
							for (UDPPacket packet : FileServer2.window)
								if (packet != null
										&& currentTime - packet.timestamp > 50) {
									// Resend packet
									sendPacket(packet);
									System.out.println("Resent packet seq #"
											+ packet.sequenceNumber);
								}
						}
						/*
						 * TODO This thread will no longer need wait() and
						 * notify(), just change windowFull accordingly
						 */
						try {
							wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				byte[] filled = new byte[fileRead];
				System.arraycopy(buffer, 0, filled, 0, fileRead);
				UDPPacket packet = new UDPPacket(
						FileServer2.nextSequenceNumber, filled);

				FileServer2.window.add(packet);
				sendPacket(packet);
				System.out.println("Sent Packet Seq #:" + packet.sequenceNumber
						+ " || " + Long.toBinaryString(packet.checksum));

				FileServer2.nextSequenceNumber++;

				// If the file thread filled up the window, pause
				if (FileServer2.getWindowSize() == FileServer2.maxWindowSize) {
					windowFull = true;
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// TODO: Send FIN Packet

		try {
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
