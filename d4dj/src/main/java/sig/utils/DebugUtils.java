package sig.utils;


public class DebugUtils {
	public static void showStackTrace() {
		System.out.println("Trace:"+getStackTrace());
	}
	
	public static String getStackTrace() {
		StackTraceElement[] stacktrace = new Throwable().getStackTrace();
		StringBuilder stack = new StringBuilder("Mini stack tracer:");
		for (int i=0;i<Math.min(10, stacktrace.length);i++) {
			stack.append("\n"+stacktrace[i].getClassName()+": **"+stacktrace[i].getFileName()+"** "+stacktrace[i].getMethodName()+"():"+stacktrace[i].getLineNumber());
		}
		return stack.toString();
	}
}
