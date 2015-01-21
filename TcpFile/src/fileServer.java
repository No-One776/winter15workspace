import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.file.*;

class tcpServer{

	private static ServerSocket listenSocket;
	private static BufferedReader inFromClient;
	private static Socket connectionSocket;
	private static DataOutputStream outToClient;
	private static String fileName;
	private static File file;

	public static void main(String[] args) throws Exception{
		
		listenSocket = new ServerSocket(9876);
		while(true){
			connectionSocket = listenSocket.accept();
			inFromClient = new BufferedReader(
			   new InputStreamReader(connectionSocket.getInputStream()));

			outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			   
			fileName = inFromClient.readLine();
			file = new File(fileName);
			
			try{
				byte[] content = Files.readAllBytes(Paths.get(fileName));
				outToClient.writeInt(content.length);
				outToClient.write(content, 0, content.length);
			} catch (FileNotFoundException e) {
				System.out.println("Could not find file");
			}
			connectionSocket.close();
		}
	}
}
