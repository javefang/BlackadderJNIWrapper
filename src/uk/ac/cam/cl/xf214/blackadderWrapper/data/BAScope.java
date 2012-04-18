package uk.ac.cam.cl.xf214.blackadderWrapper.data;

import uk.ac.cam.cl.xf214.blackadderWrapper.BAHelper;

public class BAScope extends BAObject {

	public static BAScope createBAScope(byte[] id) {
		if (id != null && BAHelper.isValidId(id, scopeIdLength)) {
			return new BAScope(id);
		} else {
			throw new InvalidBlackadderIdException(id.length);
		}
	}

	public static BAScope createBAScope(byte[] id, BAScope prefix) {
		if (id != null && BAHelper.isValidId(id, scopeIdLength) && prefix != null) {
			return new BAScope(id, prefix);
		} else {
			throw new InvalidBlackadderIdException(id.length);
		}
	}
	
	public static BAScope createBAScope(String idHex) {
		return createBAScope(BAHelper.hexToByte(idHex));
	}
	
	public static BAScope createBAScope(String idHex, BAScope prefix) {
		// TODO: is it possible to republish a scope under root? (not allowed in this code)
		return createBAScope(BAHelper.hexToByte(idHex), prefix);
	}
	
	/**
	 * Constructor for creating root scopes
	 * @param id scope id
	 */
	private BAScope(byte[] id) {
		super(id, new byte[0]);
	}
	
	/**
	 * Constructor for creating normal scopes (non-root)
	 * @param id scope id
	 * @param prefix prefix scope
	 */
	private BAScope(byte[] id, BAScope prefix) {
		super(id, prefix.fullId);
	}
	
	/**
	 * Constructor for creating republished scope
	 * @param scope
	 * @param prefix
	 */
	public BAScope(BAScope scope, BAScope prefix) {
		super(scope.fullId, prefix.fullId);
	}

	@Override
	public String toString() {
		return "S:" + idHex;
	}
}
