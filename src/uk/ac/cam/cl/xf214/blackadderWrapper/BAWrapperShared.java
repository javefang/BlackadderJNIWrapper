package uk.ac.cam.cl.xf214.blackadderWrapper;

public class BAWrapperShared {
	public static final int DEFAULT_PKT_SIZE = 1400;
	protected static boolean configured = false;
	/* native methods, shared utility */
	public static native byte[] c_hex_to_char(String hex_str);
	public static native String c_char_to_hex(byte[] char_str);
	
	public static void configureObjectFile(String path_to_so){
		System.load(path_to_so);
		configured = true;
	}
}
