package uk.ac.cam.cl.xf214.blackadderWrapper.callback;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import uk.ac.cam.cl.xf214.DebugTool.LocalDebugger;
import uk.ac.cam.cl.xf214.blackadderWrapper.BAEvent;
import uk.ac.cam.cl.xf214.blackadderWrapper.BAEvent.BAEventType;
import uk.ac.cam.cl.xf214.blackadderWrapper.data.BAPrefix;

public class HashClassifierCallback implements BAWrapperNBCallback {	
	public static final String TAG = "HashClassifierCallback";
	
	private HashMap<Integer, BlockingQueue<BAEvent>> dataQueueMap;
	private Vector<BAPrefix> controlQueueList;	// TODO: change to use TreeMap to implement longest prefix match
	
	public HashClassifierCallback() {
		dataQueueMap = new HashMap<Integer, BlockingQueue<BAEvent>>();
		controlQueueList = new Vector<BAPrefix>();
	}
	
	@Override
	public void eventReceived(BAEvent event) {
		// identify event type
		BAEventType eventType = event.getType();
		boolean checkControlQueue;
		//LocalDebugger.print(TAG, "Event received, type is " + eventType);
		if (eventType == BAEventType.PUBLISHED_DATA) {	// If the event is published data
			// put event to a matching data queue
			int hashRid = Arrays.hashCode(event.getId());
			synchronized(dataQueueMap) {
				if (dataQueueMap.containsKey(hashRid)) {
					checkControlQueue = false;
					dataQueueMap.get(hashRid).offer(event);	// TODO: change to avoid waiting indefinitely
				} else {	// queueMap does not contain the queue for the eventId
					// TODO: check if controlQueue has prefix to match
					checkControlQueue = true;
				}
			}
		} else {
			checkControlQueue = true;
		}
		
		if (checkControlQueue) {	// if the event is type other than published data (control event)
			byte[] rid = event.getId();
			BAPrefix currentPrefix;
			synchronized(controlQueueList) {
				for (int i = 0; i < controlQueueList.size(); i++) {
					currentPrefix = controlQueueList.get(i);
					if (currentPrefix.prefixMatch(rid)) {
						switch(eventType) {
						case PUBLISHED_DATA:
							currentPrefix.getHandler().newData(event);
							break;
						case SCOPE_PUBLISHED:
							// RECV: inform relevant modules about published scope
							currentPrefix.getHandler().scopePublished(event);
							break;
						case SCOPE_UNPUBLISHED:
							// RECV: inform relevant modules about unpublished scope
							currentPrefix.getHandler().scopeUnpublished(event);
							break;
						case START_PUBLISH:
							// SEND: inform sender to start sending data
							currentPrefix.getHandler().startPublish(event);
							break;
						case STOP_PUBLISH:
							// SEND: inform sender to stop sending data
							currentPrefix.getHandler().stopPublish(event);
							break;
						default:
							// TODO: unknown event, flag error message
						} // end switch			
					} // end if
				} // end while
			} // end sync
		} // end if	
	}

	public void registerDataQueue(byte[] rid, BlockingQueue<BAEvent> dataQueue) {
		synchronized(dataQueueMap) {
			dataQueueMap.put(Arrays.hashCode(rid), dataQueue);
		}
	}
	
	public void unregisterDataQueue(byte[] rid) {
		synchronized(dataQueueMap) {
			dataQueueMap.remove(Arrays.hashCode(rid));
		}
	}
	
	public void registerControlQueue(BAPrefix prefix) {
		synchronized(controlQueueList) {
			controlQueueList.add(prefix);
		}
	}
	
	public void unregisterControlQueue(BAPrefix prefix) {
		synchronized(controlQueueList) {
			controlQueueList.remove(prefix);
		}
	}
	
	/*
	public static void main(String[] args) {
		loadJNILib();
		
		String scope = "0000000000000000";
		String item = "1111111111111111";
		byte[] scope_id = BAHelper.hexToByte(scope);
		byte[] item_id = BAHelper.hexToByte(item);
		
		HashClassifierCallback cls = new HashClassifierCallback();
		
		BAPrefix prefix = new BAPrefix(scope_id, new BAPushControlEventHandler() {

			@Override
			public void scopePublished(BAEvent event) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void scopeUnpublished(BAEvent event) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void startPublish(BAEvent event) {
				System.out.println("START_PUBLISH event triggered");
			}

			@Override
			public void stopPublish(BAEvent event) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void newData(BAEvent event) {
				// TODO Auto-generated method stub
				System.out.println("PUBLISHED_DATA event triggered");
			}
			
		});
		
		String subRidHex = scope + item;
		byte[] subRid = BAHelper.hexToByte(subRidHex);
		
		if (prefix.prefixMatch(subRid)) {
			System.out.println("Prefix matched");
		}
		
		System.out.println("Registering prefix to classifier...");
		cls.registerControlQueue(prefix);
		
		BAEvent event = new BAEvent(BAEvent.BAEventType.PUBLISHED_DATA, subRid);
		
		System.out.println("Sending PUBLISHED_DATA event");
		cls.eventReceived(event);
		
		System.out.println("Registering data queue for " + subRidHex);
		
		cls.registerDataQueue(subRid, new LinkedBlockingQueue<BAEvent>());
		
		System.out.println("Sending PUBLISHED_DATA event again");
		cls.eventReceived(event);
		
	}
	
	private static void loadJNILib() {
		String sharedObjPath = "/home/jave/workspace/BlackadderJNIWrapper/libs/";
		// System.load(sharedObjPath + "libgnustl_shared.so");
		System.load(sharedObjPath + "libblackadder.so");
		BAWrapperNB.configureObjectFile(sharedObjPath
				+ "libuk_ac_cam_cl_xf214_blackadderWrapper.so");
	}
	
	*/
	
	/*
	// test if the hash function on byte[] is correct and efficient
	// RESULT: hashing a byte[] is fast
	
	public static void main(String[] args) {
		
		int loop = 100000;
		int[] arr0 = arrHash(loop);
		
		for (int i = 0; i < 1000; i++) {
			if (!Arrays.equals(arr0, arrHash(loop))) {
				System.out.println("HashCode mismatch on loop " + i);
			}
		}
	}
	
	public static int[] arrHash(int loop) {
		int[] rtnValArr = new int[loop];
		
		byte[][] arr1 = new byte[loop][100];
		for (int i = 0; i < loop; i++) {
			Arrays.fill(arr1[i], (byte)i);
		}
		
		//long startTime = System.currentTimeMillis();
		
		for (int i = 0; i < loop; i++) {
			rtnValArr[i] = Arrays.hashCode(arr1[i]);
		}
		
		//long timeElapsed = System.currentTimeMillis() - startTime;
		
		//System.out.printf("Hash %d times takes: %dms\n", loop, timeElapsed);
		return rtnValArr;
	}
	*/
}
