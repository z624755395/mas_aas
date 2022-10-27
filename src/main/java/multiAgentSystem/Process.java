package multiAgentSystem;

import java.util.ArrayList;

public class Process {
	private ArrayList<String> requiredCapability = new ArrayList<String>();
	private String id;
	
	public Process(String id, ArrayList<String> cap) {
		this.id = id;
		this.requiredCapability = cap;
	}
	
	public Process() {

	}
	
	public ArrayList<String> getRequiredCap() {
		return requiredCapability;	
	}
	
	public String getId() {
		return id;
	}
}
