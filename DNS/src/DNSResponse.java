import java.util.ArrayList;

public class DNSResponse {

	private int id;
	private boolean qr;
	private int opCode;
	private boolean aa;
	private boolean tc;
	private boolean rd;
	private boolean ra;
	private int z;
	private int rCode;
	private short qdCount;
	private short anCount;
	private short nsCount;
	private short arCount;

	private String queryDomain;
	private int queryType;
	private int queryClass;

	private ArrayList<Answer> answers;
	private ArrayList<AuthoritativeNS> authoritativeNameServers;
	private ArrayList<Answer> additionalRecords;

	public DNSResponse() {
		this.id = 0;
		this.qr = false;
		this.opCode = 0;
		this.aa = false;
		this.tc = false;
		this.rd = false;
		this.ra = false;
		this.z = 0;
		this.rCode = 0;
		this.qdCount = 0;
		this.anCount = 0;
		this.nsCount = 0;
		this.arCount = 0;
		this.queryDomain = "";
		this.queryType = 0;
		this.queryClass = 0;
		this.answers = new ArrayList<Answer>();
		this.authoritativeNameServers = new ArrayList<AuthoritativeNS>();
		this.additionalRecords = new ArrayList<Answer>();
	}

	public int getId() {
		return id;
	}

	public void setId(int responseId) {
		this.id = responseId;
	}

	public boolean isQr() {
		return qr;
	}

	public void setQr(boolean qr) {
		this.qr = qr;
	}

	public int getOpCode() {
		return opCode;
	}

	public void setOpCode(int opCode) {
		this.opCode = opCode;
	}

	public boolean isAa() {
		return aa;
	}

	public void setAa(boolean aa) {
		this.aa = aa;
	}

	public boolean isTc() {
		return tc;
	}

	public void setTc(boolean tc) {
		this.tc = tc;
	}

	public boolean isRd() {
		return rd;
	}

	public void setRd(boolean rd) {
		this.rd = rd;
	}

	public boolean isRa() {
		return ra;
	}

	public void setRa(boolean ra) {
		this.ra = ra;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public int getrCode() {
		return rCode;
	}

	public void setrCode(int rCode) {
		this.rCode = rCode;
	}

	public short getQdCount() {
		return qdCount;
	}

	public void setQdCount(short qdCount) {
		this.qdCount = qdCount;
	}

	public short getAnCount() {
		return anCount;
	}

	public void setAnCount(short anCount) {
		this.anCount = anCount;
	}

	public short getNsCount() {
		return nsCount;
	}

	public void setNsCount(short nsCount) {
		this.nsCount = nsCount;
	}

	public short getArCount() {
		return arCount;
	}

	public void setArCount(short arCount) {
		this.arCount = arCount;
	}

	public String getQueryDomain() {
		return queryDomain;
	}

	public void setQueryDomain(String queryDomain) {
		this.queryDomain = queryDomain;
	}

	public int getQueryType() {
		return queryType;
	}

	public void setQueryType(int queryType) {
		this.queryType = queryType;
	}

	public int getQueryClass() {
		return queryClass;
	}

	public void setQueryClass(int queryClass) {
		this.queryClass = queryClass;
	}

	public ArrayList<Answer> getAnswers() {
		return answers;
	}

	public void addAnswer(Answer answer) {
		this.answers.add(answer);
	}

	public void addAuthoritativeNS(AuthoritativeNS ns) {
		this.authoritativeNameServers.add(ns);
	}

	public ArrayList<Answer> getAdditionalRecords() {
		return additionalRecords;
	}

	public void addAdditionalRecord(Answer answer) {
		this.additionalRecords.add(answer);
	}

	@Override
	public String toString() {
		return "DNSResponse [id=" + id + ", qr=" + qr + ", opCode=" + opCode
				+ ", aa=" + aa + ", tc=" + tc + ", rd=" + rd + ", ra=" + ra
				+ ", z=" + z + ", rCode=" + rCode + ", qdCount=" + qdCount
				+ ", anCount=" + anCount + ", nsCount=" + nsCount
				+ ", arCount=" + arCount + ", queryDomain=" + queryDomain
				+ ", queryType=" + queryType + ", queryClass=" + queryClass
				+ ", answers=" + answers + ", authoritativeNameServers="
				+ authoritativeNameServers + ", additionalRecords="
				+ additionalRecords + "]";
	}
}

/*
 * The Answer class models the Answers section in the DNS response packet. It
 * also models the Additional Records section, as they share the same format.
 */

class Answer {

	private String name;
	private int type;
	private int iClass;
	private int timeToLive;
	private int dataLength;
	private String address;

	public Answer(String name, int type, int iClass, int timeToLive,
			int dataLength, String address) {

		this.name = name;
		this.type = type;
		this.iClass = iClass;
		this.timeToLive = timeToLive;
		this.dataLength = dataLength;
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getiClass() {
		return iClass;
	}

	public void setiClass(int iClass) {
		this.iClass = iClass;
	}

	public int getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}

	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "Answer [name=" + name + ", type=" + type + ", iClass=" + iClass
				+ ", timeToLive=" + timeToLive + ", dataLength=" + dataLength
				+ ", address=" + address + "]";
	}
}

/*
 * The AuthoritativeNS class models the Authoritative Name Servers section in
 * the DNS response packet.
 */

class AuthoritativeNS {

	private String name;
	private int type;
	private int iClass;
	private int timeToLive;
	private int dataLength;
	private String nameServer;

	public AuthoritativeNS(String name, int type, int iClass, int timeToLive,
			int dataLength, String nameServer) {
		this.name = name;
		this.type = type;
		this.iClass = iClass;
		this.timeToLive = timeToLive;
		this.dataLength = dataLength;
		this.nameServer = nameServer;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getiClass() {
		return iClass;
	}

	public void setiClass(int iClass) {
		this.iClass = iClass;
	}

	public int getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}

	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public String getNameServer() {
		return nameServer;
	}

	public void setNameServer(String nameServer) {
		this.nameServer = nameServer;
	}

	@Override
	public String toString() {
		return "AuthoritativeNS [name=" + name + ", type=" + type + ", iClass="
				+ iClass + ", timeToLive=" + timeToLive + ", dataLength="
				+ dataLength + ", nameServer=" + nameServer + "]";
	}
}