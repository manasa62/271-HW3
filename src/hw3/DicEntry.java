package hw3;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class DicEntry implements Serializable
{

	
	public String value;
	
	
	public DicEntry( String value){
		
		this.value = value;
		
	}
	
	public String toString(){
		
		return this.value;
		
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
