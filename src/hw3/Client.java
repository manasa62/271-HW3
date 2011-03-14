package hw3;

import hw3.Constants.OPERATION;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

public class Client implements Runnable {

	private int portNum;
	private int pid;
	public String dicName;
	public String logName;
	public File dicFile;
	public File logFile;
	private String currentMsg;
	private int ballotNum;
	private int acceptedBallotNum;
	private String acceptedValue;
	private int ackCount;
	private int highestBallotNum;
	private int currentLeader;
	private int prepareId;
	private boolean acceptSent;
	private OPERATION currentOp;
	public HashMap<String, String> dictionary;
	private Logger logger;
	private static final String LOG_FILE_PREFIX = Constants.LOG_DIR + "client";
	private static final String LOG_FILE_SUFFIX = ".log";

	private void initLogger() throws IOException {
		this.logger = hw3Logger.getLogger("client", LOG_FILE_PREFIX
				+ LOG_FILE_SUFFIX);

	}

	public Client(int pid, int portNum) throws IOException {
		this.portNum = portNum;
		new Thread(this).start();
		this.pid = pid;
		this.dicName = "Dic" + pid;
		this.dicFile = new File(this.dicName);
		this.logName = "Log" + pid;
		this.logFile = new File(this.logName);
		this.ballotNum = 1;
		this.currentLeader = Constants.NULL;
		this.currentOp = null;
		this.dictionary = new HashMap<String, String>();
		resetValues();
		initLogger();
	}

	public void request() throws IOException {

		DatagramSocket requestSocket = null;

		String msg = null;

		try {

			requestSocket = new DatagramSocket();

		} catch (IOException e) {
			e.printStackTrace();
		}

		msg = "dummy";

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String[] msgparts = msg.split(":", 2);

		System.out
				.println("For an insert operation to be performed enter: I:<key>-<value>");
		System.out
				.println("To delete a key enter the value to be be deleted as : D:<value>");

		try {
			msg = br.readLine();
		} catch (IOException e1) {
			logger.warning("Failed to read message");
			System.out.println("Failed to read message");
			e1.printStackTrace();
		}

		String msgParts[] = msg.split(":", 2);
		if (msgParts[0].toString().equals("I")) {
			this.currentOp = Constants.OPERATION.INSERT;
		} else {
			this.currentOp = Constants.OPERATION.DELETE;
		}

		this.currentMsg = msgParts[1].toString();
		System.out.println("Current message " + this.currentMsg);
		Message msgToSend = null;
		if (this.currentLeader != Constants.NULL) {

			msgToSend = new Message(Constants.MESSAGE_TYPES.TOLEADER, this.pid,
					this.currentLeader, ++ballotNum, this.currentMsg,
					this.currentOp);
			logger.info("Sending message: " + msgToSend.toString());
		} else {
			msgToSend = new Message(Constants.MESSAGE_TYPES.PREPARE, this.pid,
					Constants.BROADCAST, ++ballotNum, Constants.NULL_STRING,
					null);
			logger.info("Sending message: " + msgToSend.toString());
		}
		sendMessage(msgToSend, requestSocket);

		requestSocket.close();
		// sendMessage(msg, requestSocket);

	}

	private void writeToDictionary(Message m) throws IOException {
		/*
		 * BufferedWriter file = null;
		 * 
		 * file = new BufferedWriter(new FileWriter(this.dicFile, true));
		 * file.append(m.value+"\n");
		 * 
		 * file.close();
		 */
		String kv[] = m.value.split("-", 2);
		this.dictionary.put(kv[0], kv[1]);
		System.out.println("Writing " + kv[0] + "-" + kv[1]
				+ " to the Dictionary");
		logger.info("Writing " + kv[0] + "-" + kv[1] + " to the Dictionary");

	}

	private synchronized void removeFromDic(Message msg) throws IOException {

		LinkedList<String> keys = new LinkedList<String>();
		Set<Map.Entry<String, String>> set = this.dictionary.entrySet();

		for (Map.Entry<String, String> e : set) {
			if (e.getValue().equals(msg.value)) {
				keys.add(e.getKey());
				System.out.println("Removing entry " + e.getKey() + "-"
						+ e.getValue() + " from the dictionary ");
				logger.info("Removing entry " + e.getKey() + "-" + e.getValue()
						+ " from the dictionary ");

			}

		}
		while (!keys.isEmpty()) {
			this.dictionary.remove(keys.remove());
		}

	}

	private void writeToLog(Message m) throws IOException {
		BufferedWriter file = null;

		file = new BufferedWriter(new FileWriter(this.logFile, true));
		file.append(m.toString());

		file.close();

	}

	private void sendMessage(Message msg, DatagramSocket requestSocket)
			throws IOException {

		byte buf[] = new byte[10000];

		try {

			DatagramPacket datapkt = new DatagramPacket(buf, buf.length,
					InetAddress.getByName(Constants.routerName), this.portNum);

			datapkt.setData(msg.getBytes());
			requestSocket.send(datapkt);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

	}

	public void run() {

		DatagramSocket thisConnection = null;
		byte[] buf = new byte[100000];
		String[] msgparts = new String[2];

		DatagramPacket recvdpkt = new DatagramPacket(buf, buf.length);

		try {
			thisConnection = new DatagramSocket(Constants.ClientPort);

		} catch (IOException e) {
			logger.warning("Socket creation failed on the client");
			System.out.println("Socket creation failed on the client");
			e.printStackTrace();
		}

		DatagramSocket requestSocket = null;
		try {
			requestSocket = new DatagramSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (true) {
			try {

				thisConnection.receive(recvdpkt);
				// String msg = new String(recvdpkt.getData());

				Message msg = toObject(recvdpkt.getData());
				logger.info("Received message in client :" + msg.toString());
				System.out.println("Recvd in client :" + msg.toString());
				switch (msg.msgType) {
				case TOLEADER:
					Message newmsg = new Message(
							Constants.MESSAGE_TYPES.ACCEPT, msg.srcID,
							Constants.BROADCAST, this.ballotNum, msg.value,
							msg.op);
					sendMessage(newmsg, requestSocket);
					logger.info("Sending message: " + newmsg.toString());
					break;

				case ACCEPT:
					if (this.prepareId == msg.srcID) {
						this.currentLeader = msg.srcID;
					}
					logger.info("The current leader is: " + this.currentLeader);
					System.out.println("The current leader is: "
							+ this.currentLeader);
					if (msg.op != null && msg.op == Constants.OPERATION.INSERT) {
						writeToDictionary(msg);
					} else {
						removeFromDic(msg);
					}

					resetValues();

					break;

				case PREPARE:
					if (msg.ballotNum >= this.acceptedBallotNum) {
						this.prepareId = msg.srcID;
						this.acceptedBallotNum = msg.ballotNum;
						newmsg = new Message(Constants.MESSAGE_TYPES.ACK,
								this.pid, msg.srcID, this.acceptedBallotNum,
								this.acceptedValue, msg.op);
						sendMessage(newmsg, requestSocket);
						logger.info("Sending message: " + newmsg.toString());
					}
					break;

				case ACK:

					this.ackCount++;
					if (msg.ballotNum > this.highestBallotNum) {
						this.highestBallotNum = msg.ballotNum;
						this.acceptedValue = msg.value;
						logger.info("Value of highest ballot num : "
								+ this.highestBallotNum + " "
								+ this.acceptedValue);
						System.out.println("Value of highest ballot num : "
								+ this.highestBallotNum + " "
								+ this.acceptedValue);
					}
					if (this.ackCount > Constants.NO_OF_NODES / 2
							&& this.acceptSent == false) {
						if (this.acceptedValue.equals(Constants.NULL_STRING)) {
							System.out.println("Accepted values recvd is null");
							newmsg = new Message(
									Constants.MESSAGE_TYPES.ACCEPT, this.pid,
									Constants.BROADCAST, this.ballotNum,
									this.currentMsg, this.currentOp);
							System.out.println("Sending accept message : "
									+ newmsg.toString());
							sendMessage(newmsg, requestSocket);
							logger.info("Sending message: " + newmsg.toString());
							this.acceptSent = true;
						} else {
							System.out
									.println("Accepted values recvd is not null");
							newmsg = new Message(
									Constants.MESSAGE_TYPES.ACCEPT, this.pid,
									Constants.BROADCAST, this.ballotNum,
									this.acceptedValue, this.currentOp);
							System.out.println("Sending accept message : "
									+ newmsg.toString());
							sendMessage(newmsg, requestSocket);
							logger.info("Sending message: " + newmsg.toString());
							this.acceptSent = true;
						}
						/*
						 * writeToDictionary(newmsg); writeToLog(newmsg);
						 * this.currentLeader = this.pid;
						 */
					}

					break;

				case NOHOST:
					newmsg = new Message(Constants.MESSAGE_TYPES.PREPARE,
							this.pid, Constants.BROADCAST, ++ballotNum,
							Constants.NULL_STRING, null);
					sendMessage(newmsg, requestSocket);
					logger.info("Sending message: " + newmsg.toString());

				}

			}

			catch (IOException e) {

				e.printStackTrace();
			}

		}

	}

	private void resetValues() {
		this.currentMsg = Constants.NULL_STRING;
		this.acceptedBallotNum = Constants.NULL;
		this.acceptedValue = Constants.NULL_STRING;
		this.ackCount = 0;
		this.highestBallotNum = Constants.NULL;
		this.prepareId = Constants.NULL;
		this.acceptSent = false;
	}

	private Message toObject(byte[] data) {

		Message obj = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bis);
			obj = (Message) ois.readObject();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return obj;

	}

	public static void main(String args[]) throws IOException {

		int pid = Integer.parseInt(args[0]);
		int ans = 1;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		Client client = new Client(pid, Constants.RouterSendPort);

		while (true) {
			System.out.println("The available clients are: 0, 1, 2, 3, 4");
			// System.out.println("The File Server is : FS");
			System.out.println("Choose one of the following");
			System.out.println("1: Send a message");
			System.out.println("2: List values in the local Dictionary");
			System.out.println("3: Exit");
			try {
				ans = Integer.parseInt(br.readLine());
			} catch (IOException e1) {

				e1.printStackTrace();
			}

			switch (ans) {
			case 1:
				client.request();
				break;

			case 2:
				listValues(client);
				break;

			case 3:
				System.out.println("You are going to exit!!");
				System.exit(0);

			default:
				client.request();
				break;

			}
		}
	}

	private static void getValue(String key, Client client) throws IOException {
		BufferedReader file1;
		String thisLine;
		String[] lineParts = new String[4];

		file1 = new BufferedReader(new FileReader(client.dicFile));
		while ((thisLine = file1.readLine()) != null) {
			lineParts = thisLine.split(" ", 4);
			if (lineParts[0].equals(key)) {
				System.out.println("Key : " + key + " Value: " + lineParts[1]);
			}
		}
	}

	private static void listValues(Client client) throws IOException {

		Iterator it = client.dictionary.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			System.out.println(pairs.getKey() + " : " + pairs.getValue());
		}
	}

}
