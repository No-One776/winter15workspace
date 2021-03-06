import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

class OldFileServer {

	static int windowSize = 5;
	static int windowStartIndex = 0;
	static int nextSequenceNumber = 0;
	static byte[] socketBuffer = new byte[1024];
	static ArrayList<UDPPacket> packetsInProgress = new ArrayList<UDPPacket>();

	public static void updateWindow() {

		int startIndex = 0;

		for (int index = 0; index < packetsInProgress.size(); index++) {
			
			UDPPacket packet = packetsInProgress.get(index);
			System.out.println("Window packet: " + packet);
			System.out.println("Indexes: start: " + startIndex + " || for loop: " + index);
			if (index == startIndex && packet == null) {
				packetsInProgress.remove(index);
				startIndex = index + 1;
			}
		}
	}

	public static void main(String argv[]) throws Exception {

		final String fileDirectory = new File(".").getCanonicalPath() + "/";

		InetAddress clientAddress;
		int clientPort;

		DatagramSocket serverSocket = new DatagramSocket(9876);
		
		// Start a thread listening on the socket for ACKs from client
		//Runnable r = new ClientHandler(serverSocket);
		//Thread ackThread = new Thread(r);
		// TODO: Use thread wait and notify
		//ackThread.wait();

		while (true) {
			
			System.out.println("Waiting for new request...");
			nextSequenceNumber = 0;

			DatagramPacket recvPacket = new DatagramPacket(socketBuffer,
					socketBuffer.length);
			serverSocket.receive(recvPacket);
			
			String filePath = new String(socketBuffer);
			filePath = filePath.trim();
			String fullFilePath = fileDirectory + filePath;
			System.out.println(fullFilePath);

			clientAddress = recvPacket.getAddress();
			clientPort = recvPacket.getPort();
			
			System.out.println("Ready to read requested file...");
			// TODO: Use thread wait and notify
			//ackThread.notify();
			
			// 1016 + seqNum + checksum = 1024 (max packet data size)
			byte[] fileBuffer = new byte[1016];
			FileInputStream inputStream = new FileInputStream(fullFilePath);
			int read = inputStream.read(fileBuffer);

			// While still reading the file...
			while (read != -1) {

				byte[] filled = new byte[read];
				System.arraycopy(fileBuffer, 0, filled, 0, read);

				UDPPacket packet = new UDPPacket(nextSequenceNumber, filled);
				packetsInProgress.add(packet);
				
				byte[] contents = packet.getContents();

				DatagramPacket sendPacket = new DatagramPacket(contents,
						contents.length, clientAddress, clientPort);

				serverSocket.send(sendPacket);
				nextSequenceNumber++;
				
				System.out.println("Read: " + read + " || toSend size: " + packetsInProgress.size());
				
				read = inputStream.read(fileBuffer);

				// Sliding window
				// TODO: Fix sliding window!!! It's not moving
				while (packetsInProgress.size() == windowSize || (read == -1 && !packetsInProgress.isEmpty())) {
					//System.out.println("Waiting... " + toSend.size());
					serverSocket.receive(recvPacket);
					packet = new UDPPacket(recvPacket.getData());
					
					for (int i = 0; i < OldFileServer.packetsInProgress.size(); i ++) {
						UDPPacket pack = OldFileServer.packetsInProgress.get(i);
						if (pack.sequenceNumber == packet.sequenceNumber) {
							System.out.println("Packet ID " + pack.sequenceNumber + " matched");
							OldFileServer.packetsInProgress.set(i, null);
						}
					}

					OldFileServer.updateWindow();
				}
			}
			System.out.println("All packets acknowledged...");
			// TODO: Use thread wait and notify
			//ackThread.wait();
			inputStream.close();
		}
	}
}

class ClientHandler implements Runnable {

	DatagramSocket listenSocket;

	ClientHandler(DatagramSocket connection) throws Exception {
		listenSocket = connection;
	}

	@Override
	public void run() {

		while (true) {
			try {
				byte[] recvData = new byte[2048];
				DatagramPacket recvPacket = new DatagramPacket(recvData,
						recvData.length);

				try {
					listenSocket.receive(recvPacket);
					byte[] finalData = new byte[recvPacket.getLength()];
					System.arraycopy(recvData, 0, finalData, 0,
							finalData.length);
					UDPPacket packet = new UDPPacket(finalData);
					
					for (int i = 0; i < OldFileServer.packetsInProgress.size(); i ++) {
						UDPPacket pack = OldFileServer.packetsInProgress.get(i);
						if (pack.sequenceNumber == packet.sequenceNumber) {
							System.out.println("Packet ID " + pack.sequenceNumber + " matched");
							OldFileServer.packetsInProgress.set(i, null);
						}
					}

					OldFileServer.updateWindow();

				} catch (SocketTimeoutException e) {
					System.out.println("Sorry, I didn't seem to get anything");
					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error getting message from client");
			}
		}
	}
}