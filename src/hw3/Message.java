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
	public Constants.OPERATION op;
	
	
	public Message(Constants.MESSAGE_TYPES msgType, int src, int dest, int proposalNum, String value, Constants.OPERATION op){
		
		this.msgType = msgType;
		this.destID = dest;
		this.srcID = src;
		this.ballotNum = proposalNum;
		this.value =  value;
		this.op = op;
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

	
}
