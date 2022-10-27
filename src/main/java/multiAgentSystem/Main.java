package multiAgentSystem;

public class Main {

	public static void main(String[] args) {
	    String[] args1 = {"-gui","ProductAgent:multiAgentSystem.ProductAgent;"
				+ "ResourceAgent1:multiAgentSystem.ResourceAgent1;ResourceAgent2:multiAgentSystem.ResourceAgent2"}; 
		jade.Boot.main(args1);
	}

}
