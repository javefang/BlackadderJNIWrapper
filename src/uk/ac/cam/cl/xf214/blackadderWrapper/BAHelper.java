package uk.ac.cam.cl.xf214.blackadderWrapper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BAHelper {
	public static final String TAG = "BAHelper";
	
	public static byte[] addSegment(byte[] first, byte[] second, int segLength) {
		if (second == null) {
			return null;	// second must not be empty
		} else if (first == null) {
			return second;	// root scope
		} else if (isValidId(first, segLength) && isValidId(second, segLength)) {
			byte[] result = new byte[first.length + second.length];
			System.arraycopy(first, 0, result, 0, first.length);
			System.arraycopy(second, 0, result, first.length, second.length);
			return result;
		} else {
			return null;
		}
	}

	
	public static byte[] getTailSeg(byte[] absoluteId, int countFromTail, int segLength) {
		if (isValidId(absoluteId, segLength)) {
			byte[] result = new byte[segLength];
			System.arraycopy(absoluteId, absoluteId.length - segLength * countFromTail, result, 0, segLength);
			return result;
		} else {
			return null;
		}
	}
	
	public static byte[] getPureId(byte[] fullId, int segLength) {
		return getTailSeg(fullId, 1, segLength);
	}
	
	public static boolean isValidId(byte[] id, int segLength) {
		if (id == null) {
			return false;
		}
		return id.length % segLength == 0;
	}
	
	public static boolean isValidIdHex(String idHex, int segLength) {
		if (idHex == null || idHex.isEmpty()) {
			return false;
		}
		return idHex.length() % (segLength*2) == 0;
	}
	
	public static boolean compareId(byte[] id1, byte[] id2) {
		if (id1 == null || id2 == null) {
			return false;
		}
		if (id1.length != id2.length) {
			return false;
		}
		for (int i = 0; i < id1.length; i++	) {
			if (id1[i] != id2[i]) {
				return false;
			}
		}
		return true;
	}
	
	public static byte[] textToByte(String text) {
		char[] textChar = text.toCharArray();
		byte[] data = new byte[textChar.length];
		for (int i = 0; i < textChar.length; i++) {
			data[i] = (byte)textChar[i];
		}
		return data;
	}
	
	
	
	public static String byteToText(byte[] data) {
		char[] textChar = new char[data.length];
		for (int i = 0; i < data.length; i++) {
			textChar[i] = (char)data[i];
		}
		return new String(textChar);
	}
	
	public static String byteToHex(byte[] data) {
		return BAWrapperShared.c_char_to_hex(data);
	}
	
	public static byte[] hexToByte(String hex) {
		return BAWrapperShared.c_hex_to_char(hex);
	}
	
	public static String byteToPrettyHex(byte[] fullId, int segLength) {
		if (!isValidId(fullId, segLength)) {
			return null;
		}
		
		StringBuffer buf = new StringBuffer(BAWrapperShared.c_char_to_hex(fullId));
		segLength *= 2;	// string has double the length of byte
		
		for (int i = buf.length() - segLength; i >= 0; i -= segLength) {
			buf.insert(i, '/');
		}
		
		return buf.toString();
	}
	
	/*
	public static void main(String[] args) {
		String sharedObjPath = "/home/jave/workspace/BlackadderJNIWrapper/libs/";
		// System.load(sharedObjPath + "libgnustl_shared.so");
		System.load(sharedObjPath + "libblackadder.so");
		BAWrapper.configureObjectFile(sharedObjPath
				+ "uk_ac_cam_xf214_blackadderWrapper.so");
		
		byte[] fullId = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0A, 0x0A, 0x0A, 0x0A };
		byte[] scope = {0x01, 0x01, 0x01, 0x01};
		byte[] item = {0x02, 0x02, 0x02, 0x02};
		System.out.println(byteToPrettyHex(fullId, 4));
		System.out.println(byteToPrettyHex(scope, 4));
	}*/
}
