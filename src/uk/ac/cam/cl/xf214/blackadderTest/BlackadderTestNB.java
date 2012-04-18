package uk.ac.cam.cl.xf214.blackadderTest;

import java.nio.ByteBuffer;
import java.util.Arrays;

import uk.ac.cam.cl.xf214.DebugTool.LocalDebugger;
import uk.ac.cam.cl.xf214.blackadderWrapper.BAEvent;
import uk.ac.cam.cl.xf214.blackadderWrapper.BAWrapperNB;
import uk.ac.cam.cl.xf214.blackadderWrapper.BAWrapperShared;
import uk.ac.cam.cl.xf214.blackadderWrapper.Strategy;
import uk.ac.cam.cl.xf214.blackadderWrapper.callback.BlockingQueueCallback;

public class BlackadderTestNB {
	private static final String TAG = "BlackadderTestNB";
	private static BAWrapperNB wrapper;
	private static byte strategy = Strategy.DOMAIN_LOCAL;
	private static ByteBuffer buffer;
	
	private static boolean terminated = false;

	public static void main(String[] args) {
		// load c++ library
		String sharedObjPath = "/home/jave/workspace/BlackadderJNIWrapper/libs/";
		// System.load(sharedObjPath + "libgnustl_shared.so");
		System.load(sharedObjPath + "libblackadder.so");
		BAWrapperNB.configureObjectFile(sharedObjPath
				+ "libuk_ac_cam_cl_xf214_blackadderWrapper.so");
		wrapper = BAWrapperNB.getWrapper();
		
		startEventHandler();
		pubTest();
	}

	private static void subTest() {
		String room = "0000000000000000";
		String channel = "1111111111111111";
		
		print("3. Subscribe test");
		print("subscribing scope: " + "/" + room);
		wrapper.subscribeScope(BAWrapperShared.c_hex_to_char(room),
				new byte[0], strategy, null);
		print("subscribing scope: " + "/" + room + "/" + channel);
		wrapper.subscribeScope(BAWrapperShared.c_hex_to_char(channel),
				BAWrapperShared.c_hex_to_char(room), strategy, null);
	}

	private static void pubTest() {
		String scope = "0000000000000000";
		String item = "0101010101010101";
		
		print("2. Publish test");
		print("Publishing scope " + scope);
		wrapper.publishScope(BAWrapperShared.c_hex_to_char(scope), new byte[0],
				strategy, null);
		
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
		
		/*
		print("Test: Publishing data");
		byte[] buf = new byte[1000];
		Arrays.fill(buf, (byte)5);
		buffer = ByteBuffer.allocateDirect(1000);
		buffer.put(buf);
		buffer.flip();
		wrapper.publishData(BAWrapperShared.c_hex_to_char(scope + item), strategy, null,
				buffer);
		print("Data published");
		*/
	}
	
	private static void startEventHandler() {
		final Thread printEventThread = new Thread(new Runnable() {
			public void run() {
				BlockingQueueCallback callback = new BlockingQueueCallback();
				BAWrapperNB.setCallback(callback);
				
				BAEvent event;
				while (!terminated) {
					try {
						event = callback.getNextEvent();
						//print("New event received");
						switch (event.getType().getIndex()) {
						case BAEvent.SCOPE_PUBLISHED:
							printe("SCOPE_PUBLISHED: "
									+ BAWrapperShared.c_char_to_hex(event.getId()));
							break;
						case BAEvent.SCOPE_UNPUBLISHED:
							printe("SCOPE_UNPUBLISHED: "
									+ BAWrapperShared.c_char_to_hex(event.getId()));
							break;
						case BAEvent.START_PUBLISH:
							printe("START_PUBLISH: "
									+ BAWrapperShared.c_char_to_hex(event.getId()));

							byte[] payload = new byte[1000];
							Arrays.fill(payload, (byte) 5);
							buffer = ByteBuffer.allocateDirect(payload.length);
							buffer.put(payload);
							buffer.flip();
							printe("Publishing data...");
							for (int i = 0; i < 5; i++) {
								LocalDebugger.print(TAG, "Publishing data, length=" + buffer.capacity());
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
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		final Thread shutdownThread = new Thread(new Runnable() {
			public void run() {
				System.out.println("SIGINT intercepted, exiting...");
				terminated = true;
				printEventThread.interrupt();
				wrapper.disconnect();
			}
		});
		
		Runtime.getRuntime().addShutdownHook(shutdownThread);
		printEventThread.start();
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
