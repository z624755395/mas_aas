package multiAgentSystem;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.xml.sax.InputSource;

import aas.Server;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;


public class ProductAgent extends Agent {
	private static final Pattern eq = Pattern.compile("(\\=)(.*)");
	private static final String REGISTRYPATH = "http://localhost:4000/registry";
	private static final String AASSERVERPATH = "http://localhost:4001/aasServer";
	public static final IIdentifier PAID = new CustomId("productAgent");
	public static final IIdentifier OBJFID = new CustomId("objectFunction");
	public static final IIdentifier PROCESS = new CustomId("process");
	
	private ProcessPlan plan;
	private boolean busy = false;
	// The list of known resource agents
	private AID[] resourceAgents;
	// Target process
	private String targetProcess;
	// Objective function
	private String objF;
	
	// agent initializations here
	protected void setup() {
		try {
			plan = new ProcessPlan();
			registerAAS();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		addBehaviour(new CyclicBehaviour(){
			@Override
			public void action() {
				//receive message from AMS
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					System.out.println(getAID().getName()+ " receive message: "+ msg.getContent());
					targetProcess = msg.getContent();
					if (checkProcess(targetProcess)) {
						System.out.println("Trying to proceed " + targetProcess);
						// Update the list of resource agents
						DFAgentDescription template = new DFAgentDescription();
						ServiceDescription sd = new ServiceDescription();
						sd.setType("ResourceAgent");
						template.addServices(sd);
						try {
							DFAgentDescription[] result = DFService.search(myAgent, template);
							System.out.print("Found the following resource agents: ");
							resourceAgents = new AID[result.length];
							for (int i = 0; i < result.length; ++i) {
								resourceAgents[i] = result[i].getName();
								System.out.print(resourceAgents[i].getName() + " ");			
							}
							System.out.println();
						} catch (FIPAException fe) {
							fe.printStackTrace();
						}
					// Send the request to RAs
					myAgent.addBehaviour(new RequestPerformer());
					}else {
						System.out.println("cannot understand");
					}
				}				
			}						
		});				
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("product agent " + getAID().getName() + " terminating.");
	}
	
	// check if the requested process exists in the plan
	private boolean checkProcess(String s) {
		Iterator it1 = plan.getProcesses().iterator();
		while(it1.hasNext()) {
			Process p = (Process) it1.next();
			if(s.contains(p.getId())) {
				return true;
			}
		}
		return false;
	}	
	
	
	/**
	 * Inner class RequestPerformer. This is the behaviour used by the product agent to
	 * request resource agents.
	 */
	private class RequestPerformer extends Behaviour {
		private AID bestOffer; // The agent who provides the best offer
		private double bestPrice; // The best offered price
		private int repliesCnt = 0; // The counter of replies from Resource agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;

		public void action() {
			switch (step) {
			case 0:
				// Send the cfp to all RAs
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < resourceAgents.length; ++i) {
					cfp.addReceiver(resourceAgents[i]);
				}
				cfp.setContent(targetProcess);
				cfp.setConversationId("requesting");
				cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("requesting"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// Receive all proposals/refusals from resource agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						// This is an offer
						ArrayList<Double> value = readString(reply.getContent());
						double price = calculate(value);
						System.out.println("offer from " + reply.getSender().getName() + " costs " + price);
						if (bestOffer == null || price < bestPrice) {
							// This is the best offer at present
							bestPrice = price;
							bestOffer = reply.getSender();
						}
					}
					repliesCnt++;
					if (repliesCnt >= resourceAgents.length) {
						// We received all replies
						step = 2;
					}
				} else {
					block();
				}
				break;
			case 2:
				System.out.println("best offer is from " + bestOffer.getName());
				// Send the proceed order to the resource agent that provided the best offer
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(bestOffer);
				order.setContent(targetProcess);
				order.setConversationId("requesting");
				order.setReplyWith("order" + System.currentTimeMillis());
				myAgent.send(order);
				// Prepare the template to get the proceed order reply
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("requesting"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			case 3:
				// Receive the proceed order reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					// Proceed order reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// Proceed successful. We can terminate
						System.out.println(
								targetProcess + " successfully proceed by the agent " + reply.getSender().getName());
						System.out.println("Total cost = " + bestPrice);
						System.out.println("----------------");
						//myAgent.doDelete();
					} else {
						System.out.println("Attempt failed.");
					}
					step = 4;
				} else {
					block();
				}
				break;
			}
		}

		public boolean done() {
			if (step == 2 && bestOffer == null) {
				System.out.println("Attempt failed: there is no available Agent for " + targetProcess);
			}
			return ((step == 2 && bestOffer == null) || step == 4);
		}
	} // End of inner class RequestPerformer

	/**
	 * convert the String to a list of Double value
	 * 
	 * @param input
	 * @return
	 */
	private ArrayList<Double> readString(String input) {
		if (input != null) {
			ArrayList<Double> out = new ArrayList<Double>();
			input = input.replaceAll(" ", "");
			String s[] = input.split(";");

			for (int i = 0; i < s.length; i++) {
				out.add(Double.parseDouble(s[i]));
			}
			return out;
		} else {
			return null;
		}
	}
	
	/**
	 * calculate the total cost
	 */
	private double calculate(ArrayList<Double> value) {
		String formula = "";
		double result = 0;
		try {
			 formula = objF;
	            Matcher m = eq.matcher(formula);
	            String right = "";
	            if(m.find()){
	                right = m.group(2);
	            }
	            Scope scope = new Scope();  
	            Variable a = scope.getVariable("p");  
	            Variable b = scope.getVariable("e");
	            Variable c = scope.getVariable("m");
	            Variable d = scope.getVariable("t");
	            Variable e = scope.getVariable("q");
	            Expression expr = Parser.parse(right, scope);   
	            a.setValue(value.get(0));
	            b.setValue(value.get(1));
	            c.setValue(value.get(2));
	            d.setValue(value.get(3));
	            e.setValue(value.get(4));
	            result = expr.evaluate();        
		} catch (Exception e) {
			System.out.println("cannot calculate the total coast, wrong dimension");
		}
		return result;
	}

	private void registerAAS() throws Exception {
		// read registry address
		ConnectedAssetAdministrationShellManager manager = new ConnectedAssetAdministrationShellManager(
				new AASRegistryProxy(REGISTRYPATH));
		// Create AAS and push it to server
		Asset asset = new Asset("ProductAgent", new CustomId("mas.pa"), AssetKind.INSTANCE);
		AssetAdministrationShell shell = new AssetAdministrationShell("ProductAgentAAS", PAID, asset);
		// The manager uploads the AAS and registers it in the Registry server
		manager.createAAS(shell, AASSERVERPATH);
		Submodel objFSubmodel = new Submodel("objective_function", OBJFID);
		// read and parse Function
		objF = readObjF();	
		Property obj = new Property ("Function", objF);
		objFSubmodel.addSubmodelElement(obj);
		// Push the Submodel to the AAS server
		manager.createSubmodel(shell.getIdentification(), objFSubmodel);
		// the second submodel
		ArrayList<Process> processes = plan.getProcesses();
		for (int i = 0; i < processes.size(); i++) {
			Process process = processes.get(i);		
			IIdentifier IId = new CustomId("process"+ i);
			Submodel processSubmodel = new Submodel(process.getId(), IId );
			ArrayList<String> reqCaps = process.getRequiredCap();
			for (int j = 1; j -1 < reqCaps.size(); j++) {
				Property prop = new Property ("requiredcapabilty" + j, reqCaps.get(j-1));
				processSubmodel.addSubmodelElement(prop);
			}
			manager.createSubmodel(shell.getIdentification(), processSubmodel);			
		}
		System.out.println("PA obj. function: " + objF);
	}
	
	private String readObjF() throws Exception {
		String str = "";
		String result = "";
		// Create Manager
		ConnectedAssetAdministrationShellManager manager =
						new ConnectedAssetAdministrationShellManager(new AASRegistryProxy(Server.REGISTRYPATH));
		// Retrieve submodel
		ISubmodel submodel = manager.retrieveSubmodel(Server.OBJAASID, Server.OBJFID);
		// Retrieve MathML Property
		ISubmodelElement function = submodel.getSubmodelElement("MathML");
		// Print value
		str = (String) function.getValue();;			
		SAXReader sax = new SAXReader();			
		Document document = sax.read(new InputSource(new StringReader(str)));        
		MathParser mp = new MathParser();
		result = mp.parserXml(str);	
		return result;
	}
}

