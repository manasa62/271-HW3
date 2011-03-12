package hw3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

public class Message implements Serializable{

	public Constants.MESSAGE_TYPES msgType;
	public int destID;
	public int srcID;
	public int ballotNum;
	public String value;
	
	
	public Message(Constants.MESSAGE_TYPES msgType, int src, int dest, int proposalNum, String value){
		
		this.msgType = msgType;
		this.destID = dest;
		this.srcID = src;
		this.ballotNum = proposalNum;
		this.value =  value;
	}
	
	public String toString(){
		String res;
		res = "Msg Type: "+this.msgType+" "+"  Dest: "+this.destID+"  Src: "+this.srcID+"  Ballot No: "+this.ballotNum+"  Value: "+this.value+"\n";
		return res;
	}
	
	public byte[] getBytes() throws java.io.IOException{
	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
	      ObjectOutputStream oos = new ObjectOutputStream(bos);
	      oos.writeObject(this);
	      oos.flush();
	      oos.close();
	      bos.close();
	      byte [] data = bos.toByteArray();
	      return data;
	  }

	
	
	public static Message prepareMsg(int srcID, int destID, int proposalNum,String value){
		Message msg = new Message(Constants.MESSAGE_TYPES.PREPARE, srcID,destID,proposalNum,Constants.NULL_STRING);
		return msg;
	}
	
	public static Message acceptMsg(int srcID, int destID, int propNum ,String value){
		Message msg = new Message(Constants.MESSAGE_TYPES.ACCEPT, srcID,destID,propNum,value);
		return msg;
	}
	
	public static Message ackMsg(int srcID, int destID, int propNum ,String acceptedValue){
		Message msg = new Message(Constants.MESSAGE_TYPES.ACK, srcID,destID,propNum,acceptedValue);
		return msg;
	}
	
	
}
