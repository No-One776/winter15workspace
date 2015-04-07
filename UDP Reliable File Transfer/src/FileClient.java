import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileClient {

	static boolean THREAD_EXIT = false;
	static String fileNameRequested;
	public static ArrayList<UDPPacket> receivedPackets = new ArrayList<UDPPacket>();
	public static InetAddress serverIp;
	public static int serverPort;

	public static void main(String args[]) throws Exception {

		DatagramSocket clientSocket = null;
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
				System.in));

		byte[] sendData = null;
		DatagramPacket sendPacket = null;

		System.out.print("What is the IP address of the chat server?: ");
		try {
			serverIp = InetAddress.getByName(inFromUser.readLine());
		} catch (Exception e) {
			System.err.println("IP address must be a valid IP; using default");
			serverIp = InetAddress.getByName("127.0.0.1");
		}
		System.out.print("\nWhat port would you like to connect on?: ");
		try {
			serverPort = Integer.parseInt(inFromUser.readLine());
		} catch (NumberFormatException e) {
			System.err.println("Port must be an integer; using default");
			serverPort = 9876;
		}

		clientSocket = new DatagramSocket();

		Runnable r = new ServerHandler(clientSocket);
		Thread t = new Thread(r);
		t.start();

		while (true) {

			System.out.println("\nEnter a file name: ");
			fileNameRequested = inFromUser.readLine();

			sendData = fileNameRequested.getBytes();
			sendPacket = new DatagramPacket(sendData, sendData.length,
					serverIp, serverPort);
			clientSocket.send(sendPacket);
		}
	}
}

class ServerHandler implements Runnable {

	DatagramSocket listenSocket;
	String fileNameToSave;

	ServerHandler(DatagramSocket connection) throws Exception {
		listenSocket = connection;
		listenSocket.setSoTimeout(10000);
	}

	@Override
	public void run() {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(baos);

		while (!FileClient.THREAD_EXIT) {
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

					boolean checksumValid = packet.checksumValid();
					System.out.println("Checksume valid?: " + checksumValid);

					if (checksumValid) {
						// Check received packets for a matching sequence number
						boolean found = false;
						for (UDPPacket pack : FileClient.receivedPackets) {
							if (pack.sequenceNumber == packet.sequenceNumber) {
								found = true;
							}
						}
						// Add packet to final packets if it wasn't already
						// there
						if (!found) {
							System.out
									.println("Packet not in list yet... adding ["
											+ packet.sequenceNumber + "]");
							FileClient.receivedPackets.add(packet);
						}

						// Send ACK to server
						UDPPacket ack = new UDPPacket(packet.sequenceNumber);
						byte[] ackBytes = ack.getContents();
						System.out.println("ACK SEQ#: " + ack.sequenceNumber);
						DatagramPacket sendPacket = new DatagramPacket(
								ackBytes, ackBytes.length, FileClient.serverIp,
								FileClient.serverPort);

						listenSocket.send(sendPacket);
					} else {
						System.out
								.println("Checksum for packet from server was invalid. Doing nothing!");
					}

				} catch (SocketTimeoutException e) {
					System.out.println("Sorry, I didn't seem to get anything");
					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error getting message from server");
			}
		}

		// Write all the packets to file
		System.out.println("Received all packets, saving data...");
		try {
			for (UDPPacket pack : FileClient.receivedPackets) {
				dataOutputStream.write(pack.data);
			}
			byte[] endResult = baos.toByteArray();
			Files.write(Paths.get("recv-" + FileClient.fileNameRequested),
					endResult);
		} catch (IOException e) {
			e.printStackTrace();
		}
		listenSocket.close();
	}
}