import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

public class DNSPacketBuilder {

	public static final short CLASS_IN = 1;
	public static final short TYPE_A = 1;

	public ByteArrayOutputStream baos = new ByteArrayOutputStream();
	public DataOutputStream dataOutputStream = new DataOutputStream(baos);
	public String forDomain;

	public DNSPacketBuilder(String forDomain) {
		this.forDomain = forDomain;
	}

	public void buildPacket() throws IOException {
		buildHeaders();
		buildQuestion();
		// TODO: If we are responding with the FINAL answer, build the answer
		// section, otherwise stop
		if (true) {
			// buildAnswers(ArrayList<Answer>);
		}
	}

	public void buildHeaders() throws IOException {
		Random r = new Random();
		short id = (short) r.nextInt();
		short flags = 0;
		flags |= (1 << 8);
		short qcount = 1;
		short ancount = 0;
		short nscount = 0;
		short arcount = 0;
		dataOutputStream.writeShort(id);
		dataOutputStream.writeShort(flags);
		dataOutputStream.writeShort(qcount);
		dataOutputStream.writeShort(ancount);
		dataOutputStream.writeShort(nscount);
		dataOutputStream.writeShort(arcount);
	}

	public void buildQuestion() throws IOException {

		String[] labels = forDomain.split("\\.");

		for (String label : labels) {
			dataOutputStream.writeByte(label.length());
			dataOutputStream.writeBytes(label);
		}

		dataOutputStream.writeByte(0);
		dataOutputStream.writeShort(TYPE_A);
		dataOutputStream.writeShort(CLASS_IN);
	}

	public void buildAnswers(ArrayList<Answer> answers) throws IOException {

		for (Answer answer : answers) {

			String[] labels = answer.getName().split("\\.");

			for (String label : labels) {
				dataOutputStream.writeByte(label.length());
				dataOutputStream.writeBytes(label);
			}

			dataOutputStream.writeShort(answer.getType());
			dataOutputStream.writeShort(answer.getiClass());
			dataOutputStream.writeInt(answer.getTimeToLive());
			dataOutputStream.writeShort(answer.getDataLength());
			String address = answer.getAddress();

			InetAddress ipAddress = InetAddress.getByName(address);
			byte[] addressBytes = ipAddress.getAddress();
			dataOutputStream.write(addressBytes);
		}
	}

	public byte[] gePacketContents() {
		return baos.toByteArray();
	}
}