package hw3;

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
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;



public class Client implements Runnable {

	private int portNum;
	private int pid;
	public String dicName;
	public String logName;
	public File dicFile;
	public File logFile;
	private int ballotNum;

	public Client(int pid, int portNum) {
		this.portNum = portNum;
		new Thread(this).start();
		this.pid = pid;
		this.dicName = "Dic" + pid;
		this.dicFile = new File(this.dicName);
		this.logName = "Log" + pid;
		this.logFile = new File(this.logName);
		this.ballotNum = 1;
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
					.println("Enter the value that has to be written to the Dictionary: ");
			
			try {
				msg = br.readLine();
			} catch (IOException e1) {
				System.out.println("Failed to read message");
				e1.printStackTrace();
			}
			
			Message msgToSend = Message.msgToLeader(this.pid, Constants.LEADER, msg);
			
			sendMessage(msgToSend, requestSocket);
		
		requestSocket.close();
		// sendMessage(msg, requestSocket);

	}


	private void writeToDictionary(Message m) throws IOException {
		BufferedWriter file = null;
	
		file = new BufferedWriter(new FileWriter(this.dicFile, true));
		file.append(m.value+"\n");
		
		file.close();
		
	}
	
	private void writeToLog(Message m) throws IOException {
		BufferedWriter file = null;
		
		file = new BufferedWriter(new FileWriter(this.logFile, true));
		file.append(m.toString());
		
		file.close();
		
		
	}

	private void sendMessage(Message msg,
			DatagramSocket requestSocket) throws IOException {


		byte buf[] = new byte[10000];

		try {
			System.out.println("Sending message --> " + msg.value);

			DatagramPacket datapkt = new DatagramPacket(buf, buf.length,
					InetAddress.getByName(Constants.routerName),
					this.portNum);
					
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
			System.out.println("Socket creation failed on the client");
			e.printStackTrace();
		}
		while (true) {
			try {
				thisConnection.receive(recvdpkt);
				// String msg = new String(recvdpkt.getData());

				Message msg = toObject(recvdpkt.getData());
				System.out.println("Recvd in client :"+msg.value+" "+msg.msgType);
				switch(msg.msgType){
				case TOLEADER:
								Message newmsg = Message.acceptMsg(msg.srcID, Constants.BROADCAST, this.ballotNum, msg.value);
								DatagramSocket requestSocket = null;
								try {
									requestSocket = new DatagramSocket();
								} catch (IOException e) {
									e.printStackTrace();
								}
								sendMessage(newmsg, requestSocket);
					
				case ACCEPT:
								writeToDictionary(msg);
								writeToLog(msg);
				}
				
				System.out.println("Value : " + msg.value);
				}
			catch (IOException e) {

				e.printStackTrace();
			}

		}

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

		while(true){
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
				System.out.println("Key : "+ key+" Value: "+ lineParts[1]);
			}
	}
	}

	private static void listValues(Client client) throws IOException {
		
		BufferedReader file1;
		String thisLine;
		String[] lineParts = new String[4];
		
		file1 = new BufferedReader(new FileReader(client.dicFile));
		System.out.println("List of Keys:");
		while ((thisLine = file1.readLine()) != null) {
			lineParts = thisLine.split(" ", 4);
			System.out.println(lineParts[0]);
				
	}

	}
}
