package multiAgentSystem;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ResourceAgent1 extends Agent {
	public static final String REGISTRYPATH = "http://localhost:4000/registry";
	public static final String AASSERVERPATH = "http://localhost:4001/aasServer";
	public static final IIdentifier RAID = new CustomId("resourceAgent1");
	public static final IIdentifier CAP = new CustomId("providedCapability1");
	public static final IIdentifier RESOURCEN = new CustomId("resourcen1");
	
	private String[] providedCap = {"input", "transport", "output", "stamp"};
	private boolean busy = false;
	private ProcessPlan plan = new ProcessPlan();

	// Put agent initializations here
	protected void setup() {
		// Register AAS
		try {
			registerAAS();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Register the resource agent in the yellow pages
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

		// Add the behaviour serving requests from the product agent
		addBehaviour(new RequestsServe());
		// Add the behaviour proceeding requests from the product agent
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
				// CFP Message received. Make an offer.
				String request = msg.getContent();
				ACLMessage reply = msg.createReply();
				try {
					if (busy == true) {
						// The agent is NOT available.
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("not-available");

					} else {
						// The resource agent is available.
						reply.setPerformative(ACLMessage.REFUSE);
						System.out.println(getAID().getName() + " receive message: " + request);
						String str = ""; 
						s: switch (request) {
						case ProcessPlan.PROCESS1: {
							Process p = plan.getProcess1();
							if (checkProcess(p)) {
								reply.setPerformative(ACLMessage.PROPOSE);
								str = "5;3;3;8;3;2";// processing=5; energy=3; material=3; time=8; quality=3; waste=2
							}			
							break s;
						}
						case ProcessPlan.PROCESS2: {
							Process p = plan.getProcess2();
							if (checkProcess(p)) {
								reply.setPerformative(ACLMessage.PROPOSE);
								str = "4;2;4;11;4;2";
							}			
							break s;
						}
						case ProcessPlan.PROCESS3: {
							Process p = plan.getProcess3();
							if (checkProcess(p)) {
								reply.setPerformative(ACLMessage.PROPOSE);
								str = "1;2;1;3;4;2";
							}			
							break s;
						}
						}
						reply.setContent(str);
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
					busy = true;
					reply.setPerformative(ACLMessage.INFORM);
					// Proceeding...
					System.out.println(getAID().getName() + " is proceeding " + process);
					// finish
					busy = false;
				} else {
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("fail");
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
	
	
	private void registerAAS() throws Exception {
		// read registry address
		ConnectedAssetAdministrationShellManager manager = new ConnectedAssetAdministrationShellManager(
				new AASRegistryProxy(REGISTRYPATH));
		// Create AAS and push it to server
		Asset asset = new Asset("ResourceAgent1", new CustomId("mas.ra1"), AssetKind.INSTANCE);
		AssetAdministrationShell shell = new AssetAdministrationShell("ResourceAgent1AAS", RAID, asset);
		// The manager uploads the AAS and registers it in the Registry server
		manager.createAAS(shell, AASSERVERPATH);
		// The first submodel
		Submodel raSubmodel = new Submodel("Provided_Capability", CAP);
		Property cap1 = new Property ("Cap1", "input");
		Property cap2 = new Property ("Cap2", "transport");
		Property cap3 = new Property ("Cap3", "output");
		Property cap4 = new Property ("Cap4", "stamp");
		raSubmodel.addSubmodelElement(cap1);
		raSubmodel.addSubmodelElement(cap2);
		raSubmodel.addSubmodelElement(cap3);
		raSubmodel.addSubmodelElement(cap4);
		// The second submodel
		Submodel resSubmodel = new Submodel("Resources", RESOURCEN );
		Property res1 = new Property ("Stack","");
		Property res2 = new Property ("Crane","");
		Property res3 = new Property ("Ramp","");
		Property res4 = new Property ("Stamp","");
		resSubmodel.addSubmodelElement(res1);
		resSubmodel.addSubmodelElement(res2);
		resSubmodel.addSubmodelElement(res3);
		resSubmodel.addSubmodelElement(res4);
		//  Push the Submodel to the AAS server
		manager.createSubmodel(shell.getIdentification(), raSubmodel);
		manager.createSubmodel(shell.getIdentification(), resSubmodel);
	}
	
	private boolean checkProcess(Process p) {
		int size = p.getRequiredCap().size();
		Iterator<String> it = p.getRequiredCap().iterator();
		int count = 0;
		while (it.hasNext()) {
			String cap = it.next();
			if (Arrays.asList(providedCap).contains(cap)) {
				count += 1;
			}
		}
		if (count == size) {
			return true;
		} else {
			return false;
		}	
	}

}
