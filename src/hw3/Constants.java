package hw3;

import java.net.InetAddress;

public class Constants {
	
	public static final int FileServerPort = 4321;
	public static final int RouterListenPort = 1234;
	public static final int RouterSendPort = 7895;
	public static final String routerName = "dizzy.cs.ucsb.edu";
	public static final String fileServerName = "snoopy.cs.ucsb.edu";
	public static final int ClientPort = 6565;
	public static  int pid = 0;
	
	public static enum MESSAGE_TYPES {
		TOLEADER, PREPARE, ACCEPT, ACK , NOHOST
	}
	public static enum OPERATION {
		INSERT, DELETE
	}
	public static final int NULL = -1;
	public static final String NULL_STRING = "null";
	public static int LEADER = 0;
	public static final int BROADCAST = 9999;
	public static final int NO_OF_NODES = 3;

}
