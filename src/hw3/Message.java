package hw3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

public class Message implements Serializable{

	Constants.MESSAGE_TYPES msgType;
	int destID;
	int srcID;
	int proposalNum;
	String value;
	
	
	public Message(Constants.MESSAGE_TYPES msgType, int src, int dest, int proposalNum, String value){
		
		this.msgType = msgType;
		this.destID = dest;
		this.srcID = src;
		this.proposalNum = proposalNum;
		this.value =  value;
	}
	
	public String toString(){
		String res;
		res = "Msg Type: "+this.msgType+" "+"  Dest: "+this.destID+"  Src: "+this.srcID+"  Proposal No: "+this.proposalNum+"  Value: "+this.value+"\n";
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

	public static Message msgToLeader(int srcID, int destID, String value){
		Message msg = new Message(Constants.MESSAGE_TYPES.TOLEADER, srcID,destID,Constants.NULL,value);
		return msg;
	}
	
	public static Message prepareMsg(int srcID, int destID, int proposalNum,String value){
		Message msg = new Message(Constants.MESSAGE_TYPES.PREPARE, srcID,destID,proposalNum,Constants.NULL_STRING);
		return msg;
	}
	
	public static Message acceptMsg(int srcID, int destID, int propNum ,String value){
		Message msg = new Message(Constants.MESSAGE_TYPES.ACCEPT, srcID,destID,propNum,value);
		return msg;
	}
	
	
}
