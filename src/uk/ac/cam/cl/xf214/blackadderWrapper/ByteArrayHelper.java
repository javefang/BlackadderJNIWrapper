package uk.ac.cam.cl.xf214.blackadderWrapper;

public class ByteArrayHelper {
	public static byte[] andMask(byte[] src, byte[] mask) {
		byte[] rtn = new byte[src.length];
		for (int i = 0 ; i < rtn.length; i++) {
			rtn[i] = (byte)(src[i] & mask[i]);
		}
		return rtn;
	}
	
	/*
	public static boolean prefixMatch(byte[] rid, byte[] prefix) {
		// no match if prefix is longer than rid
		if (rid.length < prefix.length) {
			return false;
		}
		
		// check every byte of rid against prefix
		for (int i = 0; i < prefix.length; i++) {
			if (rid[i] != prefix[i]) {
				// return false if there is a mismatch
				return false;
			}
		}
		
		// return true if the prefix matches
		return true;
	}
	*/
}
