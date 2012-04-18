package uk.ac.cam.cl.xf214.blackadderWrapper.callback;

import uk.ac.cam.cl.xf214.blackadderWrapper.BAEvent;

public interface BAPushControlEventHandler {
	public void scopePublished(BAEvent event);
	public void scopeUnpublished(BAEvent event);
	public void startPublish(BAEvent event);
	public void stopPublish(BAEvent event);
	public void newData(BAEvent event);	// for notifying data of newly published item only
}
