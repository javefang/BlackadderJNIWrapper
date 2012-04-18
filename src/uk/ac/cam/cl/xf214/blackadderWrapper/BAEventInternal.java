package uk.ac.cam.cl.xf214.blackadderWrapper;

import java.nio.ByteBuffer;

public class BAEventInternal {
	private byte type = -1;
	byte[] id = null;
	ByteBuffer data = null;
	
	public BAEventInternal() {
		// empty constructor
	}
	
	public byte getType() {
		return type;
	}
	
	public void setType(byte type) {
		this.type = type;
	}
	
	public byte[] getId() {
		return id;
	}
	
	public void setId(byte[] id) {
		this.id = id;
	}
	
	public ByteBuffer getData() {
		return data;
	}
	
	public void setData(ByteBuffer data) {
		this.data = data;
	}
}
