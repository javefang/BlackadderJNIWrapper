package uk.ac.cam.cl.xf214.blackadderWrapper.data;

import uk.ac.cam.cl.xf214.blackadderWrapper.BAHelper;

public class BAItem extends BAObject {
	private boolean startPublish;
	
	public static BAItem createBAItem(String idHex, BAScope scope) {
		return createBAItem(BAHelper.hexToByte(idHex), scope);
	}
	
	public static BAItem createBAItem(byte[] id, BAScope scope) {
		if (BAHelper.isValidId(id, scopeIdLength)) {
			if (scope != null) {
				return new BAItem(id, scope);
			} else {
				throw new NullPointerException();
			}
		} else {
			throw new InvalidBlackadderIdException(id.length);
		}
	}
	
	public static BAItem createBAItem(BAItem item, BAScope newScope) {
		if (item != null && newScope != null) {
			return new BAItem(item, newScope);
		} else {
			throw new NullPointerException();
		}
	}
	
	/**
	 * Constructor for creating new item under a scope
	 * @param id Item ID
	 * @param prefix Prefix scope for the item to be published under
	 */
	private BAItem(byte[] id, BAScope scope) {
		super(id, scope.fullId);
	}
	
	/**
	 * Constructor for republishing an existing item under another scope
	 * @param item
	 * @param prefix
	 */
	private BAItem(BAItem item, BAScope prefix) {
		super(item.fullId, prefix.fullId);
	}
	
	public void setStartPublish(boolean startPublish) {
		this.startPublish = startPublish;
	}
	
	public boolean isStartPublish() {
		return startPublish;
	}
	
	@Override
	public String toString() {
		return (startPublish ? "*":"") + "I:" + idHex;
	}

}
