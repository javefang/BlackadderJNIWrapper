package uk.ac.cam.cl.xf214.blackadderWrapper.callback;

import java.util.Arrays;
import java.util.HashMap;

import uk.ac.cam.cl.xf214.DebugTool.LocalDebugger;
import uk.ac.cam.cl.xf214.blackadderWrapper.BAEvent;
import uk.ac.cam.cl.xf214.blackadderWrapper.BAEvent.BAEventType;
import uk.ac.cam.cl.xf214.blackadderWrapper.BAHelper;

public class HashClassifierCallback implements BAWrapperNBCallback {	
	public static final String TAG = "HashClassifierCallback";
	
	private HashMap<Integer, BAPushDataEventHandler> dataHandlerMap;
	private LPMTree<BAPushControlEventHandler> controlEventHandlerLPM;
	
	public HashClassifierCallback() {
		dataHandlerMap = new HashMap<Integer, BAPushDataEventHandler>();
		controlEventHandlerLPM = new LPMTree<BAPushControlEventHandler>();
	}
	
	@Override
	public void eventReceived(BAEvent event) {
		// identify event type
		//LocalDebugger.print(TAG, "new event!");
		BAEventType eventType = event.getType();
		boolean checkControlQueue;
		
		if (eventType == BAEventType.PUBLISHED_DATA) {	// If the event is published data
			// put event to a matching data queue
			int hashRid = Arrays.hashCode(event.getId());
			//LocalDebugger.print(TAG, "Acquiring mutex lock for dataQueueMap...");
			synchronized(dataHandlerMap) {
				if (dataHandlerMap.containsKey(hashRid)) {
					checkControlQueue = false;
					//LocalDebugger.print(TAG, "Offering BA_PKT to dataQueue " + dataQueueMap.get(hashRid));
					dataHandlerMap.get(hashRid).publishedData(event);	// TODO: data handler must ensure fast returning
				} else {	// queueMap does not contain the queue for the eventId
					// TODO: check if controlQueue has prefix to match
					checkControlQueue = true;
				}
				//LocalDebugger.print(TAG, "Releasing mutex lock for dataQueueMap...");
			}
		} else {
			checkControlQueue = true;
		}
		
		if (checkControlQueue) {	// if the event is type other than published data (control event)
			LocalDebugger.print(TAG, "Control Event received, type=" + eventType + ", RID=" + BAHelper.byteToHex(event.getId()));
			byte[] rid = event.getId();
			synchronized(controlEventHandlerLPM) {
				//LocalDebugger.print(TAG, "Acquiring mutex lock for controlQueueList...");
				BAPushControlEventHandler handler = controlEventHandlerLPM.searchPrefix(rid);
				if (handler != null) {
					LocalDebugger.print(TAG, "Found matching prefix: " + BAHelper.byteToHex(rid));
					switch(eventType) {
					case PUBLISHED_DATA:
						handler.newData(event);
						break;
					case SCOPE_PUBLISHED:
						// RECV: inform relevant modules about published scope
						handler.scopePublished(event);
						break;
					case SCOPE_UNPUBLISHED:
						// RECV: inform relevant modules about unpublished scope
						handler.scopeUnpublished(event);
						break;
					case START_PUBLISH:
						// SEND: inform sender to start sending data
						handler.startPublish(event);
						break;
					case STOP_PUBLISH:
						// SEND: inform sender to stop sending data
						handler.stopPublish(event);
						break;
					default:
						LocalDebugger.printe(TAG, "Unknown event, type=" + event.getType());
						// TODO: unknown event, flag error message
					} // end switch
				}
			}
		}
	}

	public void registerDataEventHandler(byte[] rid, BAPushDataEventHandler dataQueue) {
		synchronized(dataHandlerMap) {
			dataHandlerMap.put(Arrays.hashCode(rid), dataQueue);
		}
	}
	
	public void unregisterDataEventHandler(byte[] rid) {
		synchronized(dataHandlerMap) {
			dataHandlerMap.remove(Arrays.hashCode(rid));
		}
	}
	
	public void registerControlEventHandler(byte[] prefix, BAPushControlEventHandler handler) {
		synchronized(controlEventHandlerLPM) {
			controlEventHandlerLPM.add(prefix, handler);
		}
	}
	
	public void unregisterControlEventHandler(byte[] prefix) {
		synchronized(controlEventHandlerLPM) {
			controlEventHandlerLPM.delete(prefix);
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
