package uk.ac.cam.cl.xf214.blackadderWrapper.callback;

import uk.ac.cam.cl.xf214.blackadderWrapper.BAEvent;

public class DiscardCallback implements BAWrapperNBCallback {

	@Override
	public void eventReceived(BAEvent event) {
		event.freeNativeBuffer();
	}

}
