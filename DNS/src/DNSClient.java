import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class DNSClient {

	public static final short CLASS_IN = 1;
	public static final short TYPE_A = 1;

	public byte[] buildDNSRequest(String domain) throws IOException {

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream d = new DataOutputStream(b);
		Random r = new Random();
		short id = (short) r.nextInt();
		short flags = 0;
		flags |= (1 << 8);
		short qcount = 1;
		short ancount = 0;
		short nscount = 0;
		short arcount = 0;
		d.writeShort(id);
		d.writeShort(flags);
		d.writeShort(qcount);
		d.writeShort(ancount);
		d.writeShort(nscount);
		d.writeShort(arcount);
		String[] labels = domain.split("\\.");
		for (String label : labels) {
			d.writeByte(label.length());
			d.writeBytes(label);
		}
		d.writeByte(0);
		d.writeShort(TYPE_A);
		d.writeShort(CLASS_IN);
		d.flush();
		byte[] request = b.toByteArray();
		return request;
	}

	public String readName(ByteBuffer bb) throws UnsupportedEncodingException {

		final int NAME_POINTER = 0xc0;
		final int END_OF_NAME = 0x00;
		boolean readNamePointer = false;
		int returnToPosition = 0;

		int labelLength;
		int nameMarker;
		StringBuilder nameBuilder = new StringBuilder();
		byte[] labelBytes;
		String label;

		labelLength = bb.get();
		nameMarker = labelLength & 0xc0;

		while (true) {

			if (nameMarker == NAME_POINTER) {
				// We read a byte to get the label length, and determined
				// it was a pointer. Back up 1 byte to get short for offset
				bb.position(bb.position() - 1);
				readNamePointer = true;
				short pointerPosition = bb.getShort();
				// If returnToPosition was already set, leave it alone so that
				// we can return to the very first location we jumped from
				if (returnToPosition == 0)
					returnToPosition = bb.position();
				// AND with 00 & 14 1s to keep only the position offset (14
				// LSBs)
				pointerPosition = (short) (pointerPosition & 0x3FFF);
				bb.position(pointerPosition);
				// Update he label length after moving to the correct position
				labelLength = bb.get();
			}

			// Read the label normally
			// convert to positive number
			labelLength = labelLength & 0x000000FF;

			labelBytes = new byte[labelLength];

			for (int i = 0; i < labelLength; i++) {
				labelBytes[i] = bb.get();
			}

			label = new String(labelBytes, "UTF-8");
			nameBuilder.append(label);

			// Get the new label length and recalculate the marker value
			labelLength = bb.get();
			nameMarker = labelLength & 0xc0;

			if (labelLength != END_OF_NAME) {
				nameBuilder.append(".");
			} else {
				break;
			}
		}

		// I we ever read a name pointer, set the buffer's position back
		if (readNamePointer)
			bb.position(returnToPosition);

		return nameBuilder.toString();
	}

	public String readAdress(ByteBuffer bb, int dataLength) {

		StringBuilder addressBuilder = new StringBuilder();

		for (int k = 0; k < dataLength; k++) {
			int part = bb.get();
			// Set the most significant bits (except the last 2) to 0
			// for positive numbers
			part = part & 0x000000FF;
			addressBuilder.append(part);
			if (k != dataLength - 1)
				addressBuilder.append(".");
		}

		return addressBuilder.toString();
	}

	public static void main(String args[]) throws Exception {

		InetAddress IPAddress = InetAddress.getByName("8.8.8.8");
		DNSClient dnsClient = new DNSClient();

		DatagramSocket clientSocket = new DatagramSocket();
		clientSocket.setSoTimeout(2000);

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
				System.in));

		// If no name server IP was supplied, look in /etc/resolv.conf
		/*if (args.length < 1) {
			String resolv = new String(Files.readAllBytes(Paths
					.get("/etc/resolv.conf")));
			String[] data = resolv.split("nameserver ");
			IPAddress = InetAddress.getByName(data[1]);
		} else {
			try {
				IPAddress = InetAddress.getByName(args[1]);
			} catch (Exception e) {
				System.err.println("IP address must be a valid IP");
				System.exit(0);
			}
		}*/

		while (true) {

			System.out.print("Enter a domain name: ");
			String domain = inFromUser.readLine();

			byte[] request = dnsClient.buildDNSRequest(domain);

			DatagramPacket sendPacket = new DatagramPacket(request,
					request.length, IPAddress, 53);
			clientSocket.send(sendPacket);

			System.out.println("\nSent our query\n");

			byte[] recvData = new byte[5000];
			DatagramPacket recvPacket = new DatagramPacket(recvData,
					recvData.length);

			try {
				clientSocket.receive(recvPacket);
			} catch (SocketTimeoutException e) {
				System.out
						.println("Socket timed out after 2 seconds with no response.");
				System.exit(0);
			}

			recvData = recvPacket.getData();

			ByteBuffer bb = ByteBuffer.wrap(recvData).order(
					ByteOrder.BIG_ENDIAN);

			int responseId = bb.getShort();
			responseId = responseId & 0x0000FFFF;
			short responseFlags = bb.getShort();
			boolean qr = (((responseFlags >> 15) & 0x1) == 1);
			int opCode = (responseFlags >> 11) & 0xF;
			boolean aa = (((responseFlags >> 10) & 0x01) == 1);
			boolean tc = (((responseFlags >> 9) & 0x01) == 1);
			boolean rd = (((responseFlags >> 8) & 0x01) == 1);
			boolean ra = (((responseFlags >> 7) & 0x01) == 1);
			int z = (responseFlags >> 4) & 0x7;
			int rCode = responseFlags & 0xF;
			short qdCount = bb.getShort();
			short anCount = bb.getShort();
			short nsCount = bb.getShort();
			short arCount = bb.getShort();

			System.out.println("Response ID: " + responseId);
			System.out.println("Response QR: " + qr);
			System.out.println("Response OPCODE: " + opCode);
			System.out.println("Response AA: " + aa);
			System.out.println("Response TC: " + tc);
			System.out.println("Response RD: " + rd);
			System.out.println("Response RA: " + ra);
			System.out.println("Response Z: " + z);
			System.out.println("Response RCODE: " + rCode);
			System.out.println("Response QDCOUNT: " + qdCount);
			System.out.println("Response ANCOUNT: " + anCount);
			System.out.println("Response NSCOUNT: " + nsCount);
			System.out.println("Response ARCOUNT: " + arCount);

			String name = null;
			int type;
			int iClass;
			int timeToLive;
			int dataLength;

			System.out.println();
			name = dnsClient.readName(bb);
			System.out.println("Query name: " + name);
			type = bb.getShort();
			System.out.println("Query type: " + type);
			iClass = bb.getShort();
			System.out.println("Query class: " + iClass);
			System.out.println();

			// Loop through number of answers times and get each answer's data
			for (int j = 0; j < anCount; j++) {

				name = dnsClient.readName(bb);

				type = bb.getShort();
				iClass = bb.getShort();
				timeToLive = bb.getInt();
				dataLength = bb.getShort();
				String address = dnsClient.readAdress(bb, dataLength);

				System.out.printf("Response answer [%d] name: %s\n", j, name);
				System.out.printf("Response answer [%d] type: %s\n", j, type);
				System.out
						.printf("Response answer [%d] class: %d\n", j, iClass);
				System.out.printf(
						"Response answer [%d] time to live: %d seconds\n", j,
						timeToLive);
				System.out.printf("Response answer [%d] data length: %d\n", j,
						dataLength);
				System.out.printf("Response answer [%d] address: %s\n", j,
						address);

				System.out.println();
			}

			for (int j = 0; j < nsCount; j++) {
				name = dnsClient.readName(bb);
				type = bb.getShort();
				iClass = bb.getShort();
				timeToLive = bb.getInt();
				dataLength = bb.getShort();
				String nameServer = dnsClient.readName(bb);

				System.out.printf("Response NS [%d] name: %s\n", j, name);
				System.out.printf("Response NS [%d] type: %s\n", j, type);
				System.out.printf("Response NS [%d] class: %d\n", j, iClass);
				System.out.printf(
						"Response NS [%d] time to live: %d seconds\n", j,
						timeToLive);
				System.out.printf("Response NS [%d] data length: %d\n", j,
						dataLength);
				System.out.printf("Response NS [%d] address: %s\n", j,
						nameServer);

				System.out.println();
			}

			for (int j = 0; j < arCount; j++) {
				name = dnsClient.readName(bb);
				type = bb.getShort();
				iClass = bb.getShort();
				timeToLive = bb.getInt();
				dataLength = bb.getShort();
				String address = dnsClient.readAdress(bb, dataLength);

				System.out.printf("Response AR [%d] name: %s\n", j, name);
				System.out.printf("Response AR [%d] type: %s\n", j, type);
				System.out.printf("Response AR [%d] class: %d\n", j, iClass);
				System.out.printf(
						"Response AR [%d] time to live: %d seconds\n", j,
						timeToLive);
				System.out.printf("Response AR [%d] data length: %d\n", j,
						dataLength);
				System.out.printf("Response [%d] address: %s\n", j, address);
				System.out.println();
			}
		}
	}
}
