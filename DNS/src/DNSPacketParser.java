import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class DNSPacketParser {

	/*
	 * for (NameServer ns : nsCache) { if (ns.name.equalsIgnoreCase(name))
	 * inCache = true; } if (!inCache) { // Add response to nsCache NameServer
	 * ns = new NameServer(); ns.address = address; ns.dataLength = dataLength;
	 * ns.name = name; ns.TTL = timeToLive; ns.type = type; nsCache.add(ns); }
	 * inCache = false; }
	 */

	private ByteBuffer bufferToParse;
	private DNSResponse response;

	public DNSPacketParser(ByteBuffer bb) {
		bufferToParse = bb;
		response = new DNSResponse();
	}

	public void parsePacket() {

		parseHeaders();
		parseQuestions();
		// Only parse the answers/NS/additional records if the packet is a
		// response packet, not a question packet
		if (response.isQr()) {
			parseAnswers();
			parseNameServers();
			parseAdditionalRecords();
		}
	}

	public void parseHeaders() {

		int responseId = bufferToParse.getShort();
		responseId = responseId & 0x0000FFFF;
		response.setId(responseId);

		short responseFlags = bufferToParse.getShort();
		response.setQr(((responseFlags >> 15) & 0x1) == 1);
		response.setOpCode((responseFlags >> 11) & 0xF);
		response.setAa(((responseFlags >> 10) & 0x01) == 1);
		response.setTc(((responseFlags >> 9) & 0x01) == 1);
		response.setRd(((responseFlags >> 8) & 0x01) == 1);
		response.setRa(((responseFlags >> 7) & 0x01) == 1);
		response.setZ((responseFlags >> 4) & 0x7);
		response.setrCode(responseFlags & 0xF);
		response.setQdCount(bufferToParse.getShort());
		response.setAnCount(bufferToParse.getShort());
		response.setNsCount(bufferToParse.getShort());
		response.setArCount(bufferToParse.getShort());
	}

	public void parseQuestions() {

		String domain = readName(bufferToParse);
		response.setQueryDomain(domain);
		response.setQueryType(bufferToParse.getShort());
		response.setQueryClass(bufferToParse.getShort());
	}

	public void parseAnswers() {

		String name;
		int type;
		int iClass;
		int timeToLive;
		int dataLength;
		String address;

		for (int i = 0; i < response.getAnCount(); i++) {

			// TODO: Determine if type is CNAME; parse using getCNAME to keep
			// byte buffer in the right position
			name = readName(bufferToParse);
			type = bufferToParse.getShort();
			iClass = bufferToParse.getShort();
			timeToLive = bufferToParse.getInt();
			dataLength = bufferToParse.getShort();
			address = readAddress(bufferToParse, dataLength);

			// Build an answer object and add it to the response object (answer)
			Answer answer = new Answer(name, type, iClass, timeToLive,
					dataLength, address);
			response.addAnswer(answer);
		}
	}

	public void parseNameServers() {

		String name;
		int type;
		int iClass;
		int timeToLive;
		int dataLength;
		String nameServer;

		for (int i = 0; i < response.getNsCount(); i++) {
			name = readName(bufferToParse);
			type = bufferToParse.getShort();
			iClass = bufferToParse.getShort();
			timeToLive = bufferToParse.getInt();
			dataLength = bufferToParse.getShort();
			nameServer = readName(bufferToParse);

			AuthoritativeNS ns = new AuthoritativeNS(name, type, iClass,
					timeToLive, dataLength, nameServer);
			response.addAuthoritativeNS(ns);
		}
	}

	public void parseAdditionalRecords() {

		String name;
		int type;
		int iClass;
		int timeToLive;
		int dataLength;
		String address;

		for (int j = 0; j < response.getArCount(); j++) {

			name = readName(bufferToParse);
			type = bufferToParse.getShort();
			iClass = bufferToParse.getShort();
			timeToLive = bufferToParse.getInt();
			dataLength = bufferToParse.getShort();
			address = readAddress(bufferToParse, dataLength);

			// Build an answer object and add it to the response object
			// (additional records)
			Answer answer = new Answer(name, type, iClass, timeToLive,
					dataLength, address);
			response.addAdditionalRecord(answer);
		}
	}

	public String readName(ByteBuffer bb) {

		final int NAME_POINTER = 0xc0;
		final int END_OF_NAME = 0x00;
		boolean readNamePointer = false;
		int returnToPosition = 0;

		int labelLength;
		int nameMarker;
		StringBuilder nameBuilder = new StringBuilder();
		byte[] labelBytes;
		String label = null;

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
				// & with 00 & 14 1s to keep only the position offset (14 LSBs)
				pointerPosition = (short) (pointerPosition & 0x3FFF);
				bb.position(pointerPosition);
				// Update the label length after moving to the correct position
				labelLength = bb.get();
			}

			// Read the label normally & convert to positive number
			labelLength = labelLength & 0x000000FF;
			labelBytes = new byte[labelLength];

			for (int i = 0; i < labelLength; i++) {
				labelBytes[i] = bb.get();
			}

			try {
				label = new String(labelBytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
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

		// If we ever read a name pointer, set the buffer's position back
		if (readNamePointer)
			bb.position(returnToPosition);

		return nameBuilder.toString();
	}

	// TODO: Write CNAME parsing method

	public String readAddress(ByteBuffer bb, int dataLength) {

		StringBuilder addressBuilder = new StringBuilder();

		for (int i = 0; i < dataLength; i++) {
			int part = bb.get();
			// Set the most significant bits (except the last 2) to 0
			// for positive numbers
			part = part & 0x000000FF;
			addressBuilder.append(part);
			if (i != dataLength - 1)
				addressBuilder.append(".");
		}

		return addressBuilder.toString();
	}

	public DNSResponse getParsedResponse() {
		return response;
	}
}