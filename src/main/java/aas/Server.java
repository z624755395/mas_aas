package aas;

import java.util.function.Function;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.aas.restapi.AASModelProvider;
import org.eclipse.basyx.aas.restapi.MultiSubmodelProvider;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.components.registry.RegistryComponent;
import org.eclipse.basyx.components.registry.configuration.BaSyxRegistryConfiguration;
import org.eclipse.basyx.components.registry.configuration.RegistryBackend;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;

import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;


public class Server {

	// Server URLs
		public static final String REGISTRYPATH = "http://localhost:4000/registry";
		public static final String AASSERVERPATH = "http://localhost:4001/aasServer";

		// AAS/Submodel/Property Ids
		public static final IIdentifier MASAASID = new CustomId("mas");
		public static final IIdentifier OBJFID = new CustomId("objectFunction");
		

		public static void main(String[] args) {
			// Create Infrastructure
			startRegistry();
			startAASServer();

			// Create Manager - This manager is used to interact with an AAS server
			ConnectedAssetAdministrationShellManager manager = 
					new ConnectedAssetAdministrationShellManager(new AASRegistryProxy(REGISTRYPATH));
			
			// Create AAS and push it to server
			Asset asset = new Asset("MAS", new CustomId("example.mas"), AssetKind.INSTANCE);
			AssetAdministrationShell shell = new AssetAdministrationShell("MAS", MASAASID, asset);
			
			// The manager uploads the AAS and registers it in the Registry server
			manager.createAAS(shell, AASSERVERPATH);
			
			// Create submodel
			Submodel objFSubmodel = new Submodel("object_Function", OBJFID);

			// - Create property
			Property cost = new Property("cost_factor", 2);
			Property time = new Property("time_factor", 3);
			Property quality = new Property("quality_factor", 4);
			Property jparse = new Property ("mathFunction","3 * b + a * 4");

			// Add the property to the Submodel
			objFSubmodel.addSubmodelElement(cost);
			objFSubmodel.addSubmodelElement(time);
			objFSubmodel.addSubmodelElement(quality);
			objFSubmodel.addSubmodelElement(jparse);

			// - Push the Submodel to the AAS server
			manager.createSubmodel(shell.getIdentification(), objFSubmodel);
		}

		/**
		 * Starts an empty registry at "http://localhost:4000"
		 */
		private static void startRegistry() {
			BaSyxContextConfiguration contextConfig = new BaSyxContextConfiguration(4000, "/registry");
			BaSyxRegistryConfiguration registryConfig = new BaSyxRegistryConfiguration(RegistryBackend.INMEMORY);
			RegistryComponent registry = new RegistryComponent(contextConfig, registryConfig);

			// Start the created server
			registry.startComponent();
		}

		/**
		 * Startup an empty server at "http://localhost:4001/"
		 */
		private static void startAASServer() {
			BaSyxContextConfiguration contextConfig = new BaSyxContextConfiguration(4001, "/aasServer");
			BaSyxAASServerConfiguration aasServerConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "", REGISTRYPATH);
			AASServerComponent aasServer = new AASServerComponent(contextConfig, aasServerConfig);

			// Start the created server
			aasServer.startComponent();
		}

}
