package uk.ac.cam.cl.xf214.blackadderWrapper;

public interface Strategy {
	public static final byte NODE = (byte)0;
	public static final byte LINK_LOCAL = (byte)1;
	public static final byte DOMAIN_LOCAL = (byte)2;
	public static final byte IMPLICIT_RENDEZVOUS = (byte)3;
	public static final byte BROADCAST_IF = (byte)4;
}
