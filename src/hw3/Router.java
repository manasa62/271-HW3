package hw3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class Router implements Runnable {

	public enum CLIENTSTATE {
		UP, BLOCKED, DOWN
	};

	public LinkedBlockingQueue<Message> msgQueue;
	public static HashMap<Integer, Router.CLIENTSTATE> clientStatus;
	public static HashMap<Integer, String> clientTable;

	private int portNum;

	static {
		clientStatus = new HashMap<Integer, Router.CLIENTSTATE>();
		clientTable = new HashMap<Integer, String>();
	}
	private Logger logger;
	private static final String LOG_FILE_PREFIX = Constants.LOG_DIR
			+ "router";
	private static final String LOG_FILE_SUFFIX = ".log";

	private void initLogger() throws IOException {
		this.logger = hw3Logger.getLogger("router", LOG_FILE_PREFIX
				+ LOG_FILE_SUFFIX);
	}

	public Router(int portNum) throws IOException {
		this.msgQueue = new LinkedBlockingQueue<Message>();
		this.portNum = portNum;
		new Thread(this).start();
		initLogger();
	}

	public LinkedBlockingQueue<Message> getMsgQueue() {
		return this.msgQueue;
	}

	public void setMsgQueue(LinkedBlockingQueue<Message> msgQueue) {
		this.msgQueue = msgQueue;
	}

	public HashMap<Integer, Router.CLIENTSTATE> getClientStatus() {
		return this.clientStatus;
	}

	public void setClientStatus(HashMap<Integer, Router.CLIENTSTATE> clientStatus) {
		this.clientStatus = clientStatus;
	}

	public HashMap<Integer, String> getClientTable() {
		return this.clientTable;
	}

	public void setClientTable(HashMap<Integer, String> clientTable) {
		this.clientTable = clientTable;
	}

	public void listen() throws IOException {

		RouterThread w;
		w = new RouterThread(this.portNum, this);
		Thread t = new Thread(w);
		t.start();

	}

	public void run() {
		while (true) {
			if (msgQueue.size() >= 1) {

				DatagramSocket requestSocket = null;
				String message = null;
				Message msg = null;

				try {
					requestSocket = new DatagramSocket();

				} catch (IOException e) {
					e.printStackTrace();
				}

				while (!this.msgQueue.isEmpty()) {
					msg = this.msgQueue.remove();
					logger.info("Received message: "+msg.toString());
					
					if(msg.destID == Constants.BROADCAST){
						logger.info("BROADCASTING message :"+msg.toString());
						 Set<Entry<Integer, String>> set = this.clientTable.entrySet();
						    for (Entry<Integer, String> e : set ) {
						    	
								 //if(e.getKey() != msg.srcID){
								  Message newmsg = new Message(msg.msgType,msg.srcID,e.getKey(), msg.ballotNum, msg.value,msg.op);
								  try {
									 
									sendMessage(newmsg, requestSocket);
								 
								} catch (IOException e1) {
									System.out.println("IO Exception");
									e1.printStackTrace();
								} 
					
					    	
						    }
				}
					else {
						try {
							 
							boolean res = sendMessage(msg, requestSocket);
							logger.info("Sending message :"+msg.toString());
							if(!res){
								logger.info("Destination host "+msg.destID+" unreachable");
								logger.info("Sending : "+msg.toString());
								
								Message newmsg = new Message(Constants.MESSAGE_TYPES.NOHOST,msg.srcID,msg.srcID,msg.ballotNum, msg.value,msg.op);	
								sendMessage(newmsg, requestSocket);
							}
						}
						 catch (IOException e) {
							System.out.println("IO Exception");
							e.printStackTrace();
						}
						
					}

				// sendTerminateMessage("END_OF_CONNECTION", requestSocket);
				}
			}
		}

	}

	private boolean sendMessage(Message msg, DatagramSocket requestSocket)
			throws IOException ,PortUnreachableException, UnknownHostException{

		byte buf[] = new byte[100000];
		String[] msgparts = new String[5];
		DatagramPacket newpkt = null;
		InetAddress ip = null;
		
			//msgparts = msg.split(":", 3);
			
			if (Router.clientStatus.get(msg.destID).equals(CLIENTSTATE.UP)) {
				
				
				String hostname = Router.clientTable.get(msg.destID);

					ip = InetAddress.getByName(hostname);
					
				
				
					newpkt = new DatagramPacket(buf, buf.length, ip,
							Constants.ClientPort);
				
						
				newpkt.setData(msg.getBytes());
				requestSocket.send(newpkt);
				return true;

			} else if (Router.clientStatus.get(msg.destID).equals(
					CLIENTSTATE.BLOCKED)) {
				this.msgQueue.add(msg);
				return true;
			}
			
			else {
				return false;
			}
		 
		}
	

	public static void main(String args[]) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = new String();
		String[] addr = new String[2];

		System.out
				.println("Enter the details of the client hostnames and IP address");
		System.out.println("Format: <Label>,<Hostname>");
		System.out.println("Type \"done\" in the end");

		input = br.readLine();

		while (!input.equals("done")) {

			addr = input.split(",", 2);
			Router.clientTable.put(Integer.parseInt(addr[0]), addr[1]);
			Router.clientStatus.put(Integer.parseInt(addr[0]), CLIENTSTATE.UP);

			input = br.readLine();

		}
				
		Iterator iterator = Router.clientTable.keySet().iterator();
		System.out.println("Routing table:");
		System.out.println("--------------");
		while (iterator.hasNext()) {
			Integer key = (Integer)iterator.next();
			String value = Router.clientTable.get(key).toString();

			System.out.println(key + " : " + value);
		}

		startStatusMaintainThread();

		new Router(Constants.RouterSendPort).listen();

	}

	private static void startStatusMaintainThread() throws IOException {
		MaintainLinkStates w;
		w = new MaintainLinkStates();
		Thread t = new Thread(w);
		t.start();

	}

}
