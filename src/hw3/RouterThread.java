package hw3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.logging.Logger;

public class RouterThread implements Runnable {

	private DatagramSocket thisConnection;
	private Router router;
	private int portNum;
	private Logger logger;
	private static final String LOG_FILE_PREFIX = Constants.LOG_DIR
			+ "RouterThread";
	private static final String LOG_FILE_SUFFIX = ".log";
	
	private void initLogger() throws IOException {
		this.logger = hw3Logger.getLogger("RouterThread", LOG_FILE_PREFIX
				+ LOG_FILE_SUFFIX);
	}

	public RouterThread(int portNum, Router router) throws IOException {
		this.thisConnection = null;
		this.router = router;
		this.portNum = portNum;
		initLogger();
	}


	public void run() {
		
		try {
			this.thisConnection = new DatagramSocket(portNum);

		} catch (IOException e) {
			
			logger.warning("Connection failed on the port: " + portNum);
			e.printStackTrace();
		}

		/*
		 * try { out = new ObjectOutputStream(thisConnection.getOutputStream());
		 * in = new ObjectInputStream(thisConnection.getInputStream()); } catch
		 * (IOException e) {
		 * 
		 * e.printStackTrace(); }
		 */

		try {
			writeToQueue();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			readFromQueue();
		} catch (IOException e2) {

			e2.printStackTrace();
		}
		

	}

	private void writeToQueue() throws UnknownHostException {
		Message msg = null;
		byte[] buf = new byte[10000];
		String[] msgparts = null;
		

		DatagramPacket recvdPkt = new DatagramPacket(buf, buf.length);
		

		while(true){
		try {
			
			thisConnection.receive(recvdPkt);

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		msg = toObject(recvdPkt.getData());
		logger.info("Recieved packet in router : "+msg.toString());

	/*	msgparts = new String[2];
		msgparts = msg.split(":", 2);*/

		

		
		//	String newmsg = msg + ":" + recvdPkt.getAddress().getHostName();
			
		
			router.getMsgQueue().add(msg);
			/*try {
				buf = new byte[10000];
				recvdPkt = new DatagramPacket(buf, buf.length);
				thisConnection.receive(recvdPkt);

				//printRouterClientTable();
			}

			catch (IOException e1) {
				e1.printStackTrace();
			}

			msg = null;
			msg = toObject(recvdPkt.getData()); */
		}

	}

	private void printRouterClientTable() {
		Iterator iterator = router.getClientTable().keySet().iterator();
		logger.info("Routing table contents-->");
		
		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			String value = router.getClientTable().get(key).toString();
			logger.info(key + " "+ value);
			
		}

	}

	public void readFromQueue() throws IOException {

		System.out.println("Contents of the message queue");

		System.out.println(router.getMsgQueue().toString());

	}
	
	public Message toObject (byte[] bytes)
	{
	  Message obj = null;
	  try {
	    ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
	    ObjectInputStream ois = new ObjectInputStream (bis);
	    obj = (Message)ois.readObject();
	  }
	  catch (IOException ex) {
	   ex.printStackTrace();
	  }
	  catch (ClassNotFoundException ex) {
	   ex.printStackTrace();
	  }
	  return obj;
	}

	public void start() {
		this.start();

	}

}
