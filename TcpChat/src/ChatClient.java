import java.io.*;
import java.net.*;

public class ChatClient {

	static boolean THREAD_EXIT = false;

	public static void main(String args[]) throws Exception {
		Socket clientSocket = null;
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
				System.in));
		try {
			System.out.println("Give me an IP Address: ");
			String ip = inFromUser.readLine();
			System.out.println("Give me a Port: ");
			int port = Integer.parseInt(inFromUser.readLine());
			clientSocket = new Socket(ip, port);
			Runnable r = new ServerHandler(clientSocket);
			Thread t = new Thread(r);
			t.start();
		} catch (Exception e) {
			System.out.println("Error connecting to server");
			THREAD_EXIT = true;
		}

		while (!THREAD_EXIT) {
			DataOutputStream outToServer = new DataOutputStream(
					clientSocket.getOutputStream());

			System.out.println("Enter a message: ");
			String message = inFromUser.readLine();
			try {
				outToServer.writeBytes(message + '\n');
			} catch (Exception e) {
				System.out.println("Server Disconnected - Exiting Now");
				THREAD_EXIT = true;
			}

			if (message.equalsIgnoreCase("/exit")) {
				THREAD_EXIT = true;
				clientSocket.close();
			}
		}
	}
}

class ServerHandler implements Runnable {

	Socket listenSocket;

	ServerHandler(Socket connection) {
		listenSocket = connection;
	}

	@Override
	public void run() {

		while (!ChatClient.THREAD_EXIT) {
			try {
				BufferedReader inFromServer = new BufferedReader(
						new InputStreamReader(listenSocket.getInputStream()));
				String serverMessage = inFromServer.readLine();
				if (serverMessage == null)
					ChatClient.THREAD_EXIT = true;
				else
					System.out.println(serverMessage);
			} catch (IOException e) {
			}
		}
		// Close the server listener socket
		try {
			listenSocket.close();
		} catch (IOException e) {
			System.out
					.println("Failed to close connection. Guess you're screwed.");
		}
	}

}