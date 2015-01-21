import java.io.*;
import java.net.*;
import java.nio.file.*;

class tcpClient {

	private static BufferedReader inFromUser;
	private static String ip;
	private static int port;
	private static Socket clientSocket;
	private static String fileRequested;
	private static String saveFileAs;
	private static DataOutputStream outToServer;
	private static DataInputStream inFromServer;

	public static void main(String[] args) throws Exception {

		inFromUser = new BufferedReader(new InputStreamReader(
				System.in));

		System.out.println("Give me an IP Address: ");
		ip = inFromUser.readLine();

		System.out.println("Give me a Port: ");
		port = Integer.parseInt(inFromUser.readLine());

		clientSocket = null;

		try {
			clientSocket = new Socket(ip, port);

		} catch (Exception e) {
			System.out.println("There is an error creating the socket.");
		}

		System.out.println("What file are you looking for?");
		fileRequested = inFromUser.readLine();
		System.out.println("Name the file as: ");
		saveFileAs = inFromUser.readLine();

		outToServer = new DataOutputStream(clientSocket.getOutputStream());

		inFromServer = new DataInputStream(clientSocket.getInputStream());

		outToServer.writeBytes(fileRequested + "\n");
		
		int fileSize = inFromServer.readInt();
		byte[] data = new byte[fileSize];

		inFromServer.readFully(data);
		Files.write(Paths.get(saveFileAs), data);

		clientSocket.close();
	}
}
