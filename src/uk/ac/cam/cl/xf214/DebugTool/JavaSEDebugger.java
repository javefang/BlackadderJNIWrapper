package uk.ac.cam.cl.xf214.DebugTool;

public class JavaSEDebugger implements Debugger {

	@Override
	public void print(String tag, String msg) {
		System.out.println(tag + ": " + msg);
	}

	@Override
	public void printe(String tag, String msg) {
		System.err.println(tag + ": " + msg);
	}

}
