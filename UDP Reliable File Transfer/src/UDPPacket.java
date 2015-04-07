import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UDPPacket {

	public int sequenceNumber;
	public long checksum;
	// Timestamp used locally for server window upkeep
	public long timestamp;
	public byte[] data;

	// For server when sending packets
	public UDPPacket(int seqNumber, byte[] data) {
		this.timestamp = System.currentTimeMillis();
		this.sequenceNumber = seqNumber;
		this.data = data;
		this.checksum = 0;
		setChecksum();
		// System.out.println("Server checksum: " +
		// Long.toBinaryString(this.checksum));
	}

	// For client when receiving packets, server when receiving acks from client
	public UDPPacket(byte[] data) {
		ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
		this.sequenceNumber = bb.getInt();
		this.checksum = bb.getLong();
		this.data = new byte[data.length - 12];
		bb.get(this.data, 0, data.length - 12);
		// System.out.println("Client checksum: " +
		// Long.toBinaryString(this.checksum));
	}

	// For client when sending ack
	public UDPPacket(int seqNumber) {
		this.sequenceNumber = seqNumber;
		this.checksum = 0;
		this.data = new byte[0];
		setChecksum();
	}

	// TODO: Write comparator method for Collections.sort()

	// TODO: Write checksum forward/reverse using Internet checksum from lecture
	private long calculateChecksum(byte[] buf, boolean forward) {
		int length = buf.length;
		int i = 0;
		long sum = 0;
		while (length > 0) {
			sum += (buf[i++] & 0xff) << 8;
			if ((--length) == 0)
				break;
			sum += (buf[i++] & 0xff);
			--length;
		}
		// If the forward calculation, flip the bits at the end of the summation
		if (forward)
			sum = (~((sum & 0xFFFF) + (sum >> 16))) & 0xFFFF;
		// Otherwise, leave them in tact for validation
		else
			sum = ((sum & 0xFFFF) + (sum >> 16)) & 0xFFFF;
		return sum;
	}

	public boolean checksumValid() {

		// Temp local variable for holding the packet checksum while we calculate
		// the real checksum with a 0 instead of data
		long packetChecksum = this.checksum;
		this.checksum = 0;
		long validationChecksum = calculateChecksum(getContents(), false);
		this.checksum = packetChecksum;
		
//		String bin = String.format("%16s",
//				Long.toBinaryString(packetChecksum)).replace(' ', '0');
//		System.out.println("Forward: \t" + bin);
//		bin = String.format("%16s",
//				Long.toBinaryString(validationChecksum)).replace(' ', '0');
//		System.out.println("Validation: \t" + bin);
//		
//		bin = Long.toBinaryString(validationChecksum ^ packetChecksum);
//		System.out.println("Final : \t" + bin);

		return ((packetChecksum ^ validationChecksum) == 0xFFFF);
	}

	public void setChecksum() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream daos = new DataOutputStream(baos);
		try {
			daos.writeInt(sequenceNumber);
			daos.writeLong(checksum);
			daos.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.checksum = calculateChecksum(baos.toByteArray(), true);
	}

	public byte[] getContents() {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream daos = new DataOutputStream(baos);

		try {
			daos.writeInt(sequenceNumber);
			daos.writeLong(checksum);
			daos.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return baos.toByteArray();
	}
}
