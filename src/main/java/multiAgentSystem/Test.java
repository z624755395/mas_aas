package multiAgentSystem;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.xml.sax.InputSource;

import aas.Math;
import aas.Server;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;

public class Test {
	

	public static void main(String[] args) {
		ProcessPlan p = new ProcessPlan();
		Process p1 = p.getProcess1();
		System.out.println(p1.getId());
		//System.out.println(p1.getRequiredCap().size());
		String[] caps = {"input", "output", "transport"};
		Iterator it = p1.getRequiredCap().iterator();
		int count = 0;
		while (it.hasNext()) {
			String cap = it.next().toString();
			
			if (Arrays.asList(caps).contains(cap)) {
				count += 1;
				System.out.println(count);
			}
			System.out.println(cap);
		}
		
		
		/*
		String a = "3;  4;12";
		String s = a.replaceAll(" ","");
		System.out.println(s);
		String b[] = s.split(";");
		ArrayList<Double> ar = new ArrayList<Double>();
		
		for (int i = 0; i < b.length; i++)
			ar.add(Double.parseDouble(b[i]));
		
		System.out.println(ar);
		
		double result = 0;
		double[] parameter = {1,2,3}; 
		try {
			for(int i = 0; i < ar.size(); i++) {				
			result = result + ar.get(i) * parameter[i];
			}
		} catch (Exception e) {
			System.out.println("cannot calculate the total coast, wrong dimension");
		}
		
		System.out.println(result);
		*/
	}
	/*
	private ArrayList<ACLMessage> getMsgList() {
		ArrayList<ACLMessage> msgList = new ArrayList<ACLMessage>();
		AID r = new AID();
		r.setLocalName("ra"); // receiver name

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM); // first inform message
		msg.addReceiver(r);
		msg.setSender(getAID()); // sender name
		msg.setContent("this is Message 1");
		msgList.add(msg);

		msg = new ACLMessage(ACLMessage.INFORM); // second inform message
		msg.addReceiver(r);
		msg.setSender(getAID()); // sender name
		msg.setContent("this is Message 2");
		msgList.add(msg);
		return msgList;
	}*/
	
	/*
	// Get the name of the process to proceed as a start-up argument
	Object[] args = getArguments();
	if (args != null && args.length > 0) {
		targetProcess = (String) args[0];
		System.out.println("Target process is " + targetProcess);

		// Add a TickerBehaviour that schedules a request to seller agents every 30s
		addBehaviour(new TickerBehaviour(this, 30000) {
			protected void onTick() {
				System.out.println("Trying to proceed " + targetProcess);
				// Update the list of resource agents
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("ResourceAgent");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					System.out.println("Found the following resource agents:");
					resouceAgents = new AID[result.length];
					for (int i = 0; i < result.length; ++i) {
						resouceAgents[i] = result[i].getName();
						System.out.println(resouceAgents[i].getName());
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				// Perform the request
				myAgent.addBehaviour(new RequestPerformer());
			}
		});
	} else {
		// Make the agent terminate
		System.out.println("No target Process specified");
		//doDelete();
	}
	
	// Create Manager
			ConnectedAssetAdministrationShellManager manager =
					new ConnectedAssetAdministrationShellManager(new AASRegistryProxy(Server.REGISTRYPATH));

			// Retrieve submodel
			ISubmodel submodel = manager.retrieveSubmodel(Server.OBJAASID, Server.OBJFID);

			// Retrieve Mathml Property
			ISubmodelElement function = submodel.getSubmodelElement("MathML");

			// Print value
			String str = (String) function.getValue();
			System.out.println(str);
			
			SAXReader sax = new SAXReader();
			//sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			Document document = sax.read(new InputSource(new StringReader(str)));
	        
			Math ma = new Math();
			String xml = str;
		        try {
		            String result = ma.parserXml(xml);
		            System.out.println("result: " + result);
		            Matcher m = eq.matcher(result);
		            String right = "";
		            if(m.find()){
		                System.out.println("----------------");
		                right = m.group(2);

		            }
		            System.out.println(right);
		            Scope scope = new Scope();  
		            Variable a = scope.getVariable("e");  
		            Variable b = scope.getVariable("p");
		            Variable c = scope.getVariable("d");
		            Expression expr = Parser.parse(right, scope);   
		            a.setValue(4);b.setValue(3);c.setValue(1);
		            System.out.println("cost = " + expr.evaluate());   
		            a.setValue(5);b.setValue(1);c.setValue(2);   
		            System.out.println("cost = " + expr.evaluate());
		     	         	            	            	     
		        } catch (Exception e) {
		            System.err.print(e.getMessage());
		        }				
			}
*/
}
