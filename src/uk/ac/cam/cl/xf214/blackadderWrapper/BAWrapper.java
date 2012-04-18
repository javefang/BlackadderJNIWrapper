package uk.ac.cam.cl.xf214.blackadderWrapper;


import java.nio.ByteBuffer;

public class BAWrapper extends BAWrapperShared implements EventManagement {
	public static final int DEFAULT_SCOPE_ID_LENGTH = 8;	// length in bytes (eqv string will have 16 characters)
	private static boolean USERSPACE = true;
	private long baPtr;	// TODO: should I mark it as final here?
	private boolean closed = false;
	private static BAWrapper instance = null;
	
	public static void setUserSpace(boolean userspace) {
		USERSPACE = userspace;
	}
	
	public static BAWrapper getWrapper() {
		if (instance == null) {
			instance = new BAWrapper(USERSPACE);
		}
		return instance;
	}
	
	public boolean isClosed() {
		return closed;
	}
	
	public void disconnect() {
		if (!closed) {
			c_delete_ba(baPtr);
			closed = true;	// ensure the wrapper is only closed once
		}
	}
	
	private BAWrapper(boolean userspace) {
		if (!configured) {
			throw new IllegalStateException("Shared library not set. Call static method BlackadderWrapper.configure() first");
		}
		
		int user = userspace ? 1 : 0;
		baPtr = c_create_new_ba(user);
	}
	
	public void publishScope(byte[] scope, byte [] prefixScope, byte strat, byte[] strategyOptions) {		
		c_publish_scope(baPtr, scope, prefixScope, strat, strategyOptions);
	}	

	public void publishItem(byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions) {
		c_publish_item(baPtr, scope, prefixScope, strat, strategyOptions);		
	}

	public void unpublishScope(byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions) {
		c_unpublish_scope(baPtr, scope, prefixScope, strat, strategyOptions);
	}

	public void unpublishItem(byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions) {
		c_unpublish_item(baPtr, scope, prefixScope, strat, strategyOptions);
	}

	public void subscribeScope(byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions) {
		c_subscribe_scope(baPtr, scope, prefixScope, strat, strategyOptions);
	}

	public void subscribeItem(byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions) {
		c_subscribe_item(baPtr, scope, prefixScope, strat, strategyOptions);
	}

	public void unsubscribeScope(byte[] scope, byte[] prefixScope,	byte strat, byte[] strategyOptions) {
		c_unsubscribe_scope(baPtr, scope, prefixScope, strat, strategyOptions);
	}

	public void unsubscribeItem(byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions) {
		c_unsubscribe_item(baPtr, scope, prefixScope, strat, strategyOptions);
	}	

	public synchronized void publishData(byte[] scope, byte strat, byte[] strategyOptions, byte[] jData) {			
		c_publish_data(baPtr, scope, strat, strategyOptions, jData, jData.length);		
	}
	
	public synchronized void publishData(byte[] scope, byte strat, byte[] strategyOptions, ByteBuffer buffer) {			
		c_publish_data_direct(baPtr, scope, strat, strategyOptions, buffer, buffer.capacity());		
	}
	
	public BAEvent getNextEventDirect() {
		BAEventInternal e = new BAEventInternal();
		long event_ptr = c_nextEvent_direct(baPtr, e);
		
		BAEvent.BAEventType type = BAEvent.BAEventType.getById(e.getType());
		BAEvent retval = null;
		if (e.getData() != null) {
			retval = new BAEvent(type, e.getId(), e.getData(), e.getData().capacity());
		} else {
			retval = new BAEvent(type, e.getId());
		}
		
		retval.setNativeMemoryMappings(this, event_ptr);
		return retval;
	}
	
	public void deleteEvent(long event_ptr) {
		c_delete_event(baPtr, event_ptr);
	}
	
	/* native methods, blocking API */
	private native long c_create_new_ba(int userspace);
	private native void c_delete_ba(long ba_ptr);
	private native void c_publish_scope(long ba_ptr, byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions);
	private native void c_publish_item(long ba_ptr, byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions);
	private native void c_unpublish_scope(long ba_ptr, byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions);
	private native void c_unpublish_item(long ba_ptr, byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions);
	private native void c_subscribe_scope(long ba_ptr, byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions);
	private native void c_subscribe_item(long ba_ptr, byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions);
	private native void c_unsubscribe_scope(long ba_ptr, byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions);
	private native void c_unsubscribe_item(long ba_ptr, byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions);
	private native void c_publish_data(long ba_ptr, byte[] scope, byte strat, byte[] strategyOptions, byte[] dataBuffer, int length);
	private native void c_publish_data_direct(long ba_ptr, byte[] scope, byte strat, byte[] strategyOptions, ByteBuffer buffer, int length);
	private native long c_nextEvent_direct(long baPtr, BAEventInternal e);
	private native void c_delete_event(long ba_ptr, long event_ptr);	
}
