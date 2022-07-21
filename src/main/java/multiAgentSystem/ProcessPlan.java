package multiAgentSystem;

public class ProcessPlan {
	public static final String START = "process.start";
	public static final String EXIT = "process.exit";
	public static final String PROCESS1 = "process.p1";
	public static final String PROCESS2 = "process.p2";
	public static final String PROCESS3 = "process.p3";
	
	private String process;
	
	public ProcessPlan() {
		this.process = START;
	}
	
	public String getProcess() {
		return process;
	}
	
	public void ts1 () {
		if (process.equals(START)) {
			process = PROCESS1;
		}
	
	}
	
	public void t12 () {
		if (process.equals(PROCESS1)) {
			process = PROCESS2;
		}
	
	}
	
	public void t23 () {
		if (process.equals(PROCESS2)) {
			process = PROCESS3;
		}
	
	}
	
	public void t3e () {
		if (process.equals(PROCESS3)) {
			process = EXIT;
		}
	
	}
	
	
}
