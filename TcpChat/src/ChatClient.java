import java.io.*;
import java.net.*;

public class ChatClient {

	static boolean THREAD_EXIT = false;

	public static void main(String args[]) throws Exception {

		Socket clientSocket = new Socket("127.0.0.1", 9876);

		Runnable r = new ServerHandler(clientSocket);
		Thread t = new Thread(r);
		t.start();

		while (true) {

			DataOutputStream outToServer = new DataOutputStream(
					clientSocket.getOutputStream());

			BufferedReader inFromUser = new BufferedReader(
					new InputStreamReader(System.in));

			System.out.println("Enter a message: ");
			String message = inFromUser.readLine();

			outToServer.writeBytes(message + '\n');

			if (message.equalsIgnoreCase("/exit")) {
				break;
			}
		}
		THREAD_EXIT = true;
		clientSocket.close();
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
				System.out.println(serverMessage);
			} catch (IOException e) {
				System.out.println("Error getting message from server");
			}
		}
		try {
			listenSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}