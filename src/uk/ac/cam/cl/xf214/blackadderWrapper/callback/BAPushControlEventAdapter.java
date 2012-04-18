package uk.ac.cam.cl.xf214.blackadderWrapper.callback;

import uk.ac.cam.cl.xf214.DebugTool.LocalDebugger;
import uk.ac.cam.cl.xf214.blackadderWrapper.BAEvent;

public class BAPushControlEventAdapter implements BAPushControlEventHandler {

	@Override
	public void scopePublished(BAEvent event) {
		event.freeNativeBuffer();
	}

	@Override
	public void scopeUnpublished(BAEvent event) {
		event.freeNativeBuffer();
	}

	@Override
	public void startPublish(BAEvent event) {
		event.freeNativeBuffer();
	}

	@Override
	public void stopPublish(BAEvent event) {
		event.freeNativeBuffer();
	}

	@Override
	public void newData(BAEvent event) {
		//LocalDebugger.print("BAPushControlEventAdapter", "newData() length=" + event.getDataLength());
		event.freeNativeBuffer();
	}

}
