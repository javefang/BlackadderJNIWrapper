package uk.ac.cam.cl.xf214.blackadderTest;

import java.nio.ByteBuffer;
import java.util.Arrays;

import uk.ac.cam.cl.xf214.blackadderWrapper.BAEvent;
import uk.ac.cam.cl.xf214.blackadderWrapper.BAWrapper;
import uk.ac.cam.cl.xf214.blackadderWrapper.BAWrapperShared;
import uk.ac.cam.cl.xf214.blackadderWrapper.Strategy;

public class BlackadderTest {
	private static BAWrapper wrapper;
	private static byte strategy = Strategy.DOMAIN_LOCAL;

	public static void main(String[] args) {
		// load c++ library
		String sharedObjPath = "/home/jave/workspace/BlackadderJNIWrapper/libs/";
		// System.load(sharedObjPath + "libgnustl_shared.so");
		System.load(sharedObjPath + "libblackadder.so");
		BAWrapper.configureObjectFile(sharedObjPath
				+ "libuk_ac_cam_cl_xf214_blackadderWrapper.so");
		wrapper = BAWrapper.getWrapper();

		
		startEventHandler();
		pubTest();
	}

	private static void subTest() {
		String room = "0000000000000000";
		
		print("3. Subscribe test");
		print("subscribing scope: " + "/" + room);
		wrapper.subscribeScope(BAWrapperShared.c_hex_to_char(room),
				new byte[0], strategy, null);
		//print("subscribing item " + scope);
		//String item = "0101010101010101";
		//wrapper.subscribeItem(BAWrapperShared.c_hex_to_char(item),
				//BAWrapperShared.c_hex_to_char(scope), strategy, null);
	}

	private static void pubTest() {

		String scope = "0000000000000000";
		print("2. Publish test");
		print("Publishing scope " + scope);
		wrapper.publishScope(BAWrapperShared.c_hex_to_char(scope), new byte[0],
				strategy, null);
		String item = "0101010101010101";
		print("Sleep 3s");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		print("Publishing item " + scope + "/" + item);
		wrapper.publishItem(BAWrapperShared.c_hex_to_char(item),
				BAWrapperShared.c_hex_to_char(scope), strategy, null);
	}

	private static void startEventHandler() {
		// create event handler
		Thread eventHandler = new Thread(new Runnable() {

			@Override
			public void run() {
				BAEvent event;
				while (!Thread.currentThread().isInterrupted()) {
					event = wrapper.getNextEventDirect();
					print("New event received");
					switch (event.getType().getIndex()) {
					case BAEvent.SCOPE_PUBLISHED:
						printe("SCOPE_PUBLISHED: "
								+ BAWrapperShared.c_char_to_hex(event.getId()));
						printe("Subscribing to new scope: " + BAWrapperShared.c_char_to_hex(event.getId()));
						wrapper.subscribeScope(event.getId(), new byte[0], strategy, null);
						break;
					case BAEvent.SCOPE_UNPUBLISHED:
						printe("SCOPE_UNPUBLISHED: "
								+ BAWrapperShared.c_char_to_hex(event.getId()));
						printe("Unsubscribing to scope: " + BAWrapperShared.c_char_to_hex(event.getId()));
						wrapper.unsubscribeScope(event.getId(), new byte[0], strategy, null);
						break;
					case BAEvent.START_PUBLISH:
						printe("START_PUBLISH: "
								+ BAWrapperShared.c_char_to_hex(event.getId()));

						byte[] payload = new byte[1000];
						Arrays.fill(payload, (byte) 5);
						ByteBuffer buffer = ByteBuffer
								.allocateDirect(payload.length);
						buffer.put(payload);
						buffer.flip();
						printe("Publishing data...");
						for (int i = 0; i < 5; i++) {
							wrapper.publishData(event.getId(), strategy, null,
								buffer);
						}
						printe("Data published!");
						break;
					case BAEvent.STOP_PUBLISH:
						printe("STOP_PUBLISH: "
								+ BAWrapperShared.c_char_to_hex(event.getId()));
						break;
					case BAEvent.PUBLISHED_DATA:
						printe("PUBLISHED_DATA: "
								+ BAWrapperShared.c_char_to_hex(event.getId()));
						printe("size=" + event.getDataLength());
						break;
					default:
						printe("ERROR: Unknown event captured");
					}
					event.freeNativeBuffer();
				}
			}
		});
		eventHandler.start();
	}

	private static void utilTest() {
		// util function test
		String hex_str = "0a000001";
		print("1. Conversion utility test");
		print("Testing c_hex_to_char: original value " + hex_str);
		byte[] cvtChar = BAWrapperShared.c_hex_to_char(hex_str);
		print(printbytes(cvtChar));

		print("Testing c_char_to_hex: original value " + printbytes(cvtChar));
		String s = BAWrapperShared.c_char_to_hex(cvtChar);
		print(s);

		if (hex_str.equals(s)) {
			print("Test successful!");
		} else {
			print("Test failed!");
		}
	}

	private static String printbytes(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (byte b : data) {
			buf.append(b + " ");
		}
		return buf.toString();
	}

	private static void print(String s) {
		System.out.println(s);
	}

	private static void printe(String s) {
		System.err.println(s);
	}
}
