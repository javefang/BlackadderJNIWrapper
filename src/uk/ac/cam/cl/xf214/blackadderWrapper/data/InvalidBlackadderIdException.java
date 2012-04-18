package uk.ac.cam.cl.xf214.blackadderWrapper.data;

public class InvalidBlackadderIdException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7850479563462321923L;
	public InvalidBlackadderIdException(int length) {
		super("Invalid Blackadder ID: length=" + length);
	}
}
