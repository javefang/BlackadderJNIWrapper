package uk.ac.cam.cl.xf214.blackadderWrapper.data;

import java.util.Arrays;

import uk.ac.cam.cl.xf214.blackadderWrapper.BAHelper;
import uk.ac.cam.cl.xf214.blackadderWrapper.BAWrapperShared;
import uk.ac.cam.cl.xf214.blackadderWrapper.ByteArrayHelper;
import uk.ac.cam.cl.xf214.blackadderWrapper.callback.BAPushControlEventHandler;

public class BAPrefix {
	private byte[] prefix;
	private BAPushControlEventHandler handler;
	
	public BAPrefix(String prefixString, BAPushControlEventHandler handler) {
		this(BAHelper.hexToByte(prefixString), handler);
	}
	
	public BAPrefix(byte[] newPrefix, BAPushControlEventHandler handler) {
		if (handler == null) {
			throw new NullPointerException("BAPushControlEventHandler is null");
		}
		this.prefix = Arrays.copyOf(newPrefix, newPrefix.length);
		this.handler = handler;
		
	}
	
	public boolean prefixMatch(byte[] rid) {
		return ByteArrayHelper.prefixMatch(rid, prefix);
	}
	
	public int getPrefixLength() {
		return prefix.length;
	}
	
	public BAPushControlEventHandler getHandler() {
		return handler;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(prefix);
	}
	
	@Override
	public boolean equals(Object newPrefix) {
		return hashCode() == ((BAPrefix)newPrefix).hashCode();
	}
}