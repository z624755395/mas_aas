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


public class ResourceAgent2 extends Agent {
	public static final String REGISTRYPATH = "http://localhost:4000/registry";
	public static final String AASSERVERPATH = "http://localhost:4001/aasServer";
	public static final IIdentifier RAID = new CustomId("resourceAgent2");
	public static final IIdentifier CAP = new CustomId("providedCapability2");
	public static final IIdentifier RESOURCEN = new CustomId("resourcen2");
	
	private String[] providedCap = {"input", "transport", "output"};
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
								str = "4;2;3;9;4;2";// processing=4; energy=2; material=3; time=9; quality=4; waste=2
							}
							break s;
						}
						case ProcessPlan.PROCESS2: {
							Process p = plan.getProcess2();
							if (checkProcess(p)) {
								reply.setPerformative(ACLMessage.PROPOSE);
								str = "4;4;3;10;3;2";
							}				
							break s;
						}
						case ProcessPlan.PROCESS3: {
							Process p = plan.getProcess3();
							if (checkProcess(p)) {
								reply.setPerformative(ACLMessage.PROPOSE);
								str = "2;2;3;8;5;2";
							}			
							break s;
						}
						}
						if (str == "") {
							System.out.println(getAID().getName() + " is not capable of proceeding: " + request);
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
				// ACCEPT_PROPOSAL Message received. Process it.
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
		Asset asset = new Asset("ResourceAgent2", new CustomId("mas.ra2"), AssetKind.INSTANCE);
		AssetAdministrationShell shell = new AssetAdministrationShell("ResourceAgent2AAS", RAID, asset);
		// The manager uploads the AAS and registers it in the Registry server
		manager.createAAS(shell, AASSERVERPATH);
		// The first submodel
		Submodel capSubmodel = new Submodel("Provided_Capability", CAP);
		Property cap1 = new Property ("Cap1", "input");
		Property cap2 = new Property ("Cap2", "transport");
		Property cap3 = new Property ("Cap3", "output");
		capSubmodel.addSubmodelElement(cap1);
		capSubmodel.addSubmodelElement(cap2);
		capSubmodel.addSubmodelElement(cap3);
		// The second submodel
		Submodel resSubmodel = new Submodel("Resources", RESOURCEN );
		Property res1 = new Property ("Stack","");
		Property res2 = new Property ("Crane","");
		Property res3 = new Property ("Ramp","");
		resSubmodel.addSubmodelElement(res1);
		resSubmodel.addSubmodelElement(res2);
		resSubmodel.addSubmodelElement(res3);
		//  Push the Submodel to the AAS server
		manager.createSubmodel(shell.getIdentification(), capSubmodel);
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
