package multiAgentSystem;

import java.util.ArrayList;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ProductAgent extends Agent {
	public static final String REGISTRYPATH = "http://localhost:4000/registry";
	public static final String AASSERVERPATH = "http://localhost:4001/aasServer";
	//public static final IIdentifier PAID = new CustomId("productAgent");
	private ProcessPlan plan;
	//private PAsubmodel submodel;
	private boolean busy = false;
	// The Process to proceed
	private Process currentProcess;
	// The list of known seller agents
	private AID[] resouceAgents;
	// target Process
	private String targetProcess;
	
	// agent initializations here
	protected void setup() {
		
		//registerAAS();
		
		System.out.println("Agent" + getAID().getName() + " say hello");
		// Get the name of the process to proceed as a start-up argument
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			targetProcess = (String) args[0];
			System.out.println("Target process is " + targetProcess);

			// Add a TickerBehaviour that schedules a request to seller agents every 30s
			addBehaviour(new TickerBehaviour(this, 30000) {
				protected void onTick() {
					System.out.println("Trying to proceed " + targetProcess);
					// Update the list of seller agents
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
			doDelete();
		}
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("product agent " + getAID().getName() + " terminating.");
	}

	/**
	 * Inner class RequestPerformer. This is the behaviour used by product agents to
	 * request resource agents.
	 */
	private class RequestPerformer extends Behaviour {
		private AID bestOffer; // The agent who provides the best offer
		private double bestPrice; // The best offered price
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;

		public void action() {
			switch (step) {
			case 0:
				// Send the cfp to all sellers
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < resouceAgents.length; ++i) {
					cfp.addReceiver(resouceAgents[i]);
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
				// Receive all proposals/refusals from seller agents
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
					if (repliesCnt >= resouceAgents.length) {
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
				// Receive the purchase order reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					// Purchase order reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						System.out.println(
								targetProcess + " successfully proceed by the agent " + reply.getSender().getName());
						System.out.println("Total cost = " + bestPrice);
						myAgent.doDelete();
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
		String s = "calculating:";
		double result = 0;
		double[] parameter = { 1, 2, 3 };
		try {
			for (int i = 0; i < value.size(); i++) {
				result = result + parameter[i] * value.get(i);
				s = s + parameter[i] + "*" + value.get(i);
				if (i + 1 < value.size())
					s = s + " + ";
			}
			s = s + " = " + result;
			System.out.println(s);
		} catch (Exception e) {
			System.out.println("cannot calculate the total coast, wrong dimension");
		}
		return result;
	}
	
	/*
	private void registerAAS() {
		// read registry address
		ConnectedAssetAdministrationShellManager manager = new ConnectedAssetAdministrationShellManager(
				new AASRegistryProxy(REGISTRYPATH));
		// Create AAS and push it to server
		Asset asset = new Asset("PA", new CustomId("example.pa"), AssetKind.INSTANCE);
		AssetAdministrationShell shell = new AssetAdministrationShell("PA", PAID, asset);
		// The manager uploads the AAS and registers it in the Registry server
		manager.createAAS(shell, AASSERVERPATH);
	}
	*/

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
	}

}
