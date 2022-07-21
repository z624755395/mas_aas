package multiAgentSystem;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class ResourceAgent2 extends Agent {
	private ProcessPlan p;
	private boolean busy = false;
	private int time = 1;
	private int cost = 0;

	// Put agent initializations here
	protected void setup() {
		// Register the book-selling service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("ResourceAgent");
		sd.setName("first Resource Agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Add the behaviour serving queries from product agents
		addBehaviour(new RequestsServe());

		// Add the behaviour serving proceed orders from product agents
		addBehaviour(new Proceed());

	}

	/**
	 * Inner class RequestsServe. This is the behaviour used by resource agents to
	 * serve incoming requests from product agents. If the agent is available for
	 * the next process, it will replies with a PROPOSE message. Otherwise a REFUSE
	 * message is sent back.
	 */
	private class RequestsServe extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				System.out.println(getAID().getName()+ " receiving: " + msg.getContent());
				// CFP Message received. Process it
				String request = msg.getContent();
				// START,PROCESS1,PROCESS2...
				ACLMessage reply = msg.createReply();
				try {
					if (busy == true) {
						// The agent is NOT available.
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("not-available");

					} else {
						System.out.println(getAID().getName() + " preparing");
						// The resource agent is available.
						s: switch (request) {
						case ProcessPlan.START: {
							System.out.println("next process is " + request);
							break s;
						}
						case ProcessPlan.EXIT: {
							System.out.println("next process is " + request);
							break s;
						}
						case ProcessPlan.PROCESS1: {
							System.out.println("next process is " + request);
							break s;
						}
						case ProcessPlan.PROCESS2: {
							System.out.println("next process is " + request);
							break s;
						}
						case ProcessPlan.PROCESS3: {
							System.out.println("next process is " + request);
							break s;
						}
						}
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent("2;9;4");// cost = 2; time = 12; quality = 3
					}
				} catch (Exception e) {
					System.out.println(getLocalName() + "cannot understand");
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	} // End of inner class RequestsServe

	/**
	 * Inner class Proceed. This is the behaviour used by resource agents to serve
	 * incoming Request from product agents. The seller agent removes the purchased
	 * book from its catalogue and replies with an INFORM message to notify the
	 * buyer that the purchase has been sucesfully completed.
	 */
	private class Proceed extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// ACCEPT_PROPOSAL Message received. Process it
				String process = msg.getContent();
				ACLMessage reply = msg.createReply();

				if (busy != true) {
					reply.setPerformative(ACLMessage.INFORM);
					System.out.println(getAID().getName() + " is proceeding " + process);
				} else {
					// The requested book has been sold to another buyer in the meanwhile .
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	} // End of inner class OfferRequestsServer

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Printout a dismissal message
		System.out.println("resource agent " + getAID().getName() + " terminating.");
	}

	public void setFree() {
		busy = false;
	}

}
