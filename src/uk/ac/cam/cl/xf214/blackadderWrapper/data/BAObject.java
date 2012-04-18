package uk.ac.cam.cl.xf214.blackadderWrapper.data;

import java.util.Arrays;

import uk.ac.cam.cl.xf214.blackadderWrapper.BAHelper;

public class BAObject {
	protected static int scopeIdLength = 8;
	
	protected byte[] id;
	protected byte[] prefix;
	protected byte[] fullId;
	protected String idHex;
	
	public static void setScopeIdLength(int idLen) {
		scopeIdLength = idLen;
	}
	
	public static int getScopeIdLength() {
		return scopeIdLength;
	}
	
	protected BAObject() {
		// do nothing
	}
	
	protected BAObject(byte[] id, byte[] prefix) {
		this.id = Arrays.copyOf(id, id.length);
		this.prefix = Arrays.copyOf(prefix, prefix.length);
		
		byte[] pureId;
		if (id.length == scopeIdLength) {
			pureId = id;
		} else {
			pureId = BAHelper.getPureId(id, scopeIdLength);
		}
		
		this.fullId = BAHelper.addSegment(prefix, pureId, scopeIdLength);
		this.idHex = BAHelper.byteToPrettyHex(fullId, scopeIdLength);
	}

	public boolean isValid() {
		return ((id.length % scopeIdLength) == 0) 
				&& ((prefix.length % scopeIdLength) == 0);
	}
	
	public String getIdHex() {
		return idHex;
	}
	
	@Override
	public String toString() {
		return idHex;
	}

	public byte[] getId() {
		return id;
	}

	public byte[] getPrefix() {
		return prefix;
	}

	public byte[] getFullId() {
		return fullId;
	}
}
