package uk.ac.cam.cl.xf214.blackadderWrapper.callback;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import uk.ac.cam.cl.xf214.blackadderWrapper.BAEvent;

/**
 * Callback function to be used for Blackadder non-blocking API.
 * This callback basically behaves as a blocking API.
 * The incoming events are saved into a BlockingQueue, which will be read later by calling "getNextEvent()".
 * @author Jave Fang
 *
 */
public class BlockingQueueCallback implements BAWrapperNBCallback {
	private BlockingQueue<BAEvent> eventQueue;
	
	public BlockingQueueCallback(int queueSize) {
		eventQueue = new ArrayBlockingQueue<BAEvent>(queueSize);
	}
	
	/**
	 * Called by the Blackadder non-blocking API
	 */
	@Override
	public void eventReceived(BAEvent event) {
		eventQueue.offer(event);
	}
	
	public BAEvent getNextEvent() throws InterruptedException {
		return eventQueue.take();
	}
}
