import java.io.*;
import java.net.*;
import java.util.*;

class ChatServer {

	public static void main(String argv[]) throws Exception {
		ServerSocket listenSocket = new ServerSocket(9876);

		while (true) {
			Socket s = listenSocket.accept();
			Runnable r = new ClientHandler(s);
			Thread t = new Thread(r);
			t.start();
		}
	}
}

class ClientHandler implements Runnable {
	Socket connectionSocket;
	String ip;
	int port;
	MessageDispatcher dispatcher = MessageDispatcher.getInstance();
	BufferedReader inFromClient;
	String clientMessage;

	ClientHandler(Socket connection) {
		connectionSocket = connection;
	}

	public void run() {
		ip = connectionSocket.getInetAddress().toString();
		port = connectionSocket.getPort();
		System.out.println("Client @" + ip + ":" + port + " connected");
		dispatcher.addClientToPool(connectionSocket);

		while (true) {

			try {
				inFromClient = new BufferedReader(new InputStreamReader(
						connectionSocket.getInputStream()));
				clientMessage = inFromClient.readLine();

				if (clientMessage.equalsIgnoreCase("/exit")) {
					break;
				}

				dispatcher.sendToAllClients(clientMessage, connectionSocket);
			} catch (Exception e) {
				System.out.println("Got an Exception");
			}
		}

		try {
			System.out.println("Client @ " + ip + ":" + port + " disconnected");
			connectionSocket.close();
			dispatcher.removeClientFromPool(connectionSocket);
		} catch (IOException e) {
			System.out
					.println("Failed to close connection. Guess you're screwed.");
		}
	}
}

class MessageDispatcher {

	private static MessageDispatcher instance = null;

	ArrayList<Socket> clientSockets = new ArrayList<Socket>();
	DataOutputStream outToClient;

	protected MessageDispatcher() {
		// Prevent 'new'
	}

	public static MessageDispatcher getInstance() {
		if (instance == null) {
			instance = new MessageDispatcher();
		}
		return instance;
	}

	public boolean addClientToPool(Socket s) {
		return clientSockets.add(s);
	}

	public boolean removeClientFromPool(Socket s) {
		return clientSockets.remove(s);
	}

	public void sendToAllClients(String message, Socket sender)
			throws IOException {

		for (int i = 0; i < clientSockets.size(); i++) {

			Socket client = clientSockets.get(i);

			outToClient = new DataOutputStream(client.getOutputStream());
			String ip = sender.getInetAddress().toString();
			int port = sender.getPort();

			outToClient.writeChars(ip + ":" + port + " wrote: " + message
					+ '\n');
		}
	}

}