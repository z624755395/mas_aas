package multiAgentSystem;

import java.util.ArrayList;

public class ProcessPlan {
	public static final String PROCESS1 = "process_p1";
	public static final String PROCESS2 = "process_p2";
	public static final String PROCESS3 = "process_p3";
	private ArrayList<Process> processes;

	
	public ProcessPlan() {	
		this.processes = new ArrayList<Process>();
		ArrayList<String> caps1 = new ArrayList<String>();
		caps1.add("input");
		caps1.add("transport");
		Process p1 = new Process("process_p1", caps1);
		ArrayList<String> caps2 = new ArrayList<String>();
		caps2.add("input");
		caps2.add("output");
		Process p2 = new Process("process_p2", caps2);
		ArrayList<String> caps3 = new ArrayList<String>();
		caps3.add("stamp");
		Process p3 = new Process("process_p3", caps3);		
		this.addProcess(p1);
		this.addProcess(p2);
		this.addProcess(p3);						
	}
	
	public ProcessPlan(ArrayList<Process> p) {
		this.processes = p;
	}
	
	public ArrayList<Process> getProcesses() {
		return processes;
	}
	
	public void addProcess(Process p) {
		processes.add(p);
	}
	
	public Process getProcess1() {
		return processes.get(0);
	}
	
	public Process getProcess2() {
		return processes.get(1);
	}
	public Process getProcess3() {
		return processes.get(2);
	}
	
	
}
