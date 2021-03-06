import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class PrefixMatch {
	private static String routes, addresses;
	private static trieNode head = new trieNode();

	public static void main(String[] args) {
		if (args.length > 1) {
			routes = args[0];
			addresses = args[1];
		} else {
			System.out.println("No Files Given!\n");
			System.exit(0);
		}
		FileReader r = null;
		try {
			r = new FileReader(routes);
		} catch (FileNotFoundException e1) {
			System.out.println("Could not find routes file!");
			System.exit(0);
		}
		BufferedReader routeReader = new BufferedReader(r);
		try {
			String data;
			while ((data = routeReader.readLine()) != null) {
				parseRoute(data);
			}
		} catch (IOException e) {
		}
		try {
			routeReader.close();
		} catch (IOException e) {
		}
		
		try {
			r = new FileReader(addresses);
		} catch (FileNotFoundException e) {
			System.out.println("Could not find address ip file!");
			System.exit(0);
		}
		BufferedReader ipReader = new BufferedReader(r);
		String ip;
		try {
			while ((ip = ipReader.readLine()) != null) {
				find(ip);
			}
		} catch (IOException e) {
		}
	}

	private static void find(String ip) {
		String p[] = ip.split("\\.");
		String binary = binary(p[0]) + binary(p[1]) + binary(p[2])
				+ binary(p[3]);
		String forwardIP = searchPrefix(binary, 0, head, "No Match");
		System.out.println(ip + "\t" + forwardIP);
	}

	private static String searchPrefix(String binary, int position,
			trieNode next, String lastFIP) {
		if (next.data.fIP != null)
			lastFIP = next.data.fIP;
		if (binary.substring(position, position + 1).equalsIgnoreCase("0")) {
			if (next.zero == null)
				return lastFIP;
			return searchPrefix(binary, position + 1, next.zero, lastFIP);
		} else {
			if (next.one == null)
				return lastFIP;
			return searchPrefix(binary, position + 1, next.one, lastFIP);
		}
	}

	private static void parseRoute(String data) {
		// System.out.println(data);
		Node node = new Node();
		String parts[] = data.split("\\|");
		String partA[] = parts[0].split("/");
		int mask = Integer.parseInt(partA[1]);
		node.asPathsLength = parts[1].split(" ").length;
		node.fIP = parts[2];
		String p[] = partA[0].split("\\.");
		String addr = binary(p[0]) + binary(p[1]) + binary(p[2]) + binary(p[3]);
		buildTrie(addr, 0, node, mask, head);
	}

	private static String binary(String ipPart) {
		String binString = Integer.toBinaryString(Integer.parseInt(ipPart));
		int length = 8 - binString.length();
		char[] padArray = new char[length];
		Arrays.fill(padArray, '0');
		String padString = new String(padArray);
		binString = padString + binString;
		return binString;
	}

	private static void buildTrie(String array, int position, Node node,
			int mask, trieNode next) {
		if (mask == position) {
			if (next.data.asPathsLength == -1)
				next.data = node;
			else if (next.data.asPathsLength > node.asPathsLength)
				next.data = node;
			return;
		} else {
			if (array.substring(position, position + 1).equalsIgnoreCase("0")) {
				if (next.zero == null)
					next.zero = new trieNode();
				buildTrie(array, position + 1, node, mask, next.zero);
			} else {
				if (next.one == null)
					next.one = new trieNode();
				buildTrie(array, position + 1, node, mask, next.one);
			}
		}
	}
}

class Node {
	String fIP;
	int asPathsLength = -1;
}

class trieNode {
	Node data = new Node();
	trieNode zero = null, one = null;
}
