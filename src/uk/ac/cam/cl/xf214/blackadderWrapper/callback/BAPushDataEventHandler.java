package uk.ac.cam.cl.xf214.blackadderWrapper.callback;

import uk.ac.cam.cl.xf214.blackadderWrapper.BAEvent;

/**
 * Blocking interface for calling data receiver
 * @author jave
 *
 */
public interface BAPushDataEventHandler {
	public void publishedData(BAEvent event);
}
