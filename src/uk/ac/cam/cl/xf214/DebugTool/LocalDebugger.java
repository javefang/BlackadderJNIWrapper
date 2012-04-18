package uk.ac.cam.cl.xf214.DebugTool;

public class LocalDebugger {
	private static Debugger debugger = new JavaSEDebugger();
	
	public static void print(String tag, String msg) {
		debugger.print(tag, msg);
	}

	public static void printe(String tag, String msg) {
		debugger.printe(tag, msg);
	}

	public static void setDebugger(Debugger newDebugger) {
		if (newDebugger == null) {
			throw new NullPointerException();
		}
		debugger = newDebugger;
	}
}
