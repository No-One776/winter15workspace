import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class DNSResolver {
	private static int port;
	private static ArrayList<Client> connections;

	public static void main(String[] args) throws IOException {

		InetAddress IPAddress = InetAddress.getByName("8.8.8.8");
		DatagramSocket clientSocket = null;
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
				System.in));

		if (args.length < 1)
			port = 9876;
		else
			port = Integer.parseInt(args[1]);
		try {
			clientSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.out.println("Can't create DatagramSocket");
		}
		clientSocket.setSoTimeout(2000);

		// TODO Main Thread: Read request/response, check cache/add to cache,
		// send any other needed requests, then once all the information is
		// gotten, send the response back to the client
		while (true) {

			System.out.print("Enter a domain: ");
			String domain = inFromUser.readLine();

			DNSPacketBuilder dnsBuilder = new DNSPacketBuilder(domain);
			dnsBuilder.buildPacket();
			byte[] request = dnsBuilder.gePacketContents();

			DatagramPacket sendPacket = new DatagramPacket(request,
					request.length, IPAddress, 53);
			clientSocket.send(sendPacket);

			System.out.println("Sent our query\n");

			// Receive packet and put it into a bytebuffer and parse
			byte[] recvData = new byte[5000];
			DatagramPacket recvPacket = new DatagramPacket(recvData,
					recvData.length);
			try {
				clientSocket.receive(recvPacket);
			} catch (Exception e) {
				System.out.println("Error getting data");
			}
			recvData = recvPacket.getData();
			ByteBuffer bb = ByteBuffer.wrap(recvData).order(
					ByteOrder.BIG_ENDIAN);

			DNSPacketParser dnsParser = new DNSPacketParser(bb);
			dnsParser.parsePacket();
			DNSResponse response = dnsParser.getParsedResponse();
			System.out.println(response + "\n");

		}

		// TODO: At first, your resolver will have nothing in its cache.
		// Assuming nothing is in the resolver cache, when your resolver
		// receives a request for a name, it should unset the recursion desired
		// bit in the request, and forward the request to a root name server
		// (see the root hints file).

		// TODO: When your resolver receives a reply, it should examine the
		// reply for the answer to its query. If the answer is contained, it
		// should be forwarded to the client. Otherwise, if there is a NS record
		// indicating a better server to ask, the query
		// should be sent to that server as it was sent to the root.

		// TODO:DNS records specify a TTL (time to live). You should cache each
		// record received for its TTL.

		// TODO:Whenever any record in your cache is appropriate for bypassing
		// part of the resolution process, do so. The DNS specification, in
		// RFC1034, RFC1035, and other related RFCs, is large. You are only
		// required to support queries in the IN class and for the
		// A type. You will probably also have to be able to make use of NS and
		// CNAME records.

		// TODO: Your server should print out basic information about each query
		// and reply it receives or makes. It should also have an interface to
		// inspect the contents of its cache.
	}
}

class Client {
	public int port;
	public InetAddress ip;
	public int id;
	public String domain;
}