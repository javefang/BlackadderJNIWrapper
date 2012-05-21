package uk.ac.cam.cl.xf214.blackadderWrapper;

import java.nio.ByteBuffer;

import uk.ac.cam.cl.xf214.DebugTool.LocalDebugger;
import uk.ac.cam.cl.xf214.blackadderWrapper.callback.BAWrapperNBCallback;
import uk.ac.cam.cl.xf214.blackadderWrapper.callback.DiscardCallback;

public class BAWrapperNB extends BAWrapperShared implements EventManagement {
	public static final String TAG = "BAWrapperNB";
	public static final int DEFAULT_SCOPE_ID_LENGTH = 8;	// length in bytes (eqv string will have 16 characters)
	private static boolean USERSPACE = true;
	private long baPtr;	// TODO: should I mark it as final here?
	private boolean closed = false;
	private static BAWrapperNB instance = null;
	
	private static BAWrapperNBCallback callback = new DiscardCallback();	// discard all event by default
	
	public static void setUserSpace(boolean userspace) {
		USERSPACE = userspace;
	}
	
	public static BAWrapperNB getWrapper() {
		LocalDebugger.print(TAG, "getWrapper() called");
		if (instance == null) {
			LocalDebugger.print(TAG, "Creating new BAWrapperNB");
			instance = new BAWrapperNB(USERSPACE);
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
	
	private BAWrapperNB(boolean userspace) {
		if (!configured) {
			throw new IllegalStateException("Shared library not set. Call static method BlackadderWrapper.configure() first");
		}
		
		int user = userspace ? 1 : 0;
		baPtr = c_create_new_ba(user);
		if (baPtr == 0) {
			throw new NullPointerException("Native code failed to create BAWrapperNB");
		}
		
	}
	
	public void publishScope(byte[] scope, byte [] prefixScope, byte strat, byte[] strategyOptions) {		
		LocalDebugger.print(TAG, "publishScope(): " + BAHelper.byteToHex(prefixScope) + BAHelper.byteToHex(scope));
		c_publish_scope(baPtr, scope, prefixScope, strat, strategyOptions);
	}	

	public void publishItem(byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions) {
		LocalDebugger.print(TAG, "publishItem(): " + BAHelper.byteToHex(prefixScope) + BAHelper.byteToHex(scope));
		c_publish_item(baPtr, scope, prefixScope, strat, strategyOptions);		
	}

	public void unpublishScope(byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions) {
		LocalDebugger.print(TAG, "unpublishScope(): " + BAHelper.byteToHex(prefixScope) + BAHelper.byteToHex(scope));
		c_unpublish_scope(baPtr, scope, prefixScope, strat, strategyOptions);
	}

	public void unpublishItem(byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions) {
		LocalDebugger.print(TAG, "unpublishItem(): " + BAHelper.byteToHex(prefixScope) + BAHelper.byteToHex(scope));
		c_unpublish_item(baPtr, scope, prefixScope, strat, strategyOptions);
	}

	public void subscribeScope(byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions) {
		LocalDebugger.print(TAG, "subscribeScope(): " + BAHelper.byteToHex(prefixScope) + BAHelper.byteToHex(scope));
		c_subscribe_scope(baPtr, scope, prefixScope, strat, strategyOptions);
	}

	public void subscribeItem(byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions) {
		LocalDebugger.print(TAG, "subscribeItem(): " + BAHelper.byteToHex(prefixScope) + BAHelper.byteToHex(scope));
		c_subscribe_item(baPtr, scope, prefixScope, strat, strategyOptions);
	}

	public void unsubscribeScope(byte[] scope, byte[] prefixScope,	byte strat, byte[] strategyOptions) {
		LocalDebugger.print(TAG, "unsubscribeScope(): " + BAHelper.byteToHex(prefixScope) + BAHelper.byteToHex(scope));
		c_unsubscribe_scope(baPtr, scope, prefixScope, strat, strategyOptions);
	}

	public void unsubscribeItem(byte[] scope, byte[] prefixScope, byte strat, byte[] strategyOptions) {
		LocalDebugger.print(TAG, "unsubscribeItem(): " + BAHelper.byteToHex(prefixScope) + BAHelper.byteToHex(scope));
		c_unsubscribe_item(baPtr, scope, prefixScope, strat, strategyOptions);
	}	

	public void publishData(byte[] scope, byte strat, byte[] strategyOptions, byte[] jData) {			
		c_publish_data(baPtr, scope, strat, strategyOptions, jData, jData.length);		
	}
	
	public void publishData(byte[] scope, byte strat, byte[] strategyOptions, ByteBuffer buffer) {
		// TODO: suspected performance issue, use with caution
		c_publish_data_direct(baPtr, scope, strat, strategyOptions, buffer, buffer.capacity());		
	}
	
	/* called by c++ code */
	public static void onEventReceived(long event_ptr, byte type, byte[] id, ByteBuffer data) {
		// construct java representation for the Blackadder event		
		BAEvent.BAEventType eventType = BAEvent.BAEventType.getById(type);
		//LocalDebugger.print(TAG, "onEventReceived(): " + eventType);
		BAEvent event = null;
		if (data != null) {
			event = new BAEvent(eventType, id, data, data.capacity());
		} else {
			event = new BAEvent(eventType, id);
		}
		event.setNativeMemoryMappings(instance, event_ptr);
		
		// invoke callback with BAEvent
		if (callback != null) {
			callback.eventReceived(event);
		} else {
			// error! user did not specify a callback, free native memory and discard the event
			event.freeNativeBuffer();
		}
	}
	
	@Override
	public void deleteEvent(long event_ptr) {
		c_delete_event(baPtr, event_ptr);
	}
	
	public static void setCallback(BAWrapperNBCallback newCallback) {
		if (newCallback == null) {
			throw new NullPointerException("Callback cannot be null");
		}
		callback = newCallback;
	}
	
	public static BAWrapperNBCallback getCallback() {
		return callback;
	}
	
	/* native methods, non-blocking API */
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
	private native void c_delete_event(long ba_ptr, long event_ptr);
}
