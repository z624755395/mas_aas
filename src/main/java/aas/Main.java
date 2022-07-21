package aas;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;

import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;
import parsii.tokenizer.ParseException;

public class Main {

	public static void main(String[] args) throws ParseException {
		// Create Manager
		ConnectedAssetAdministrationShellManager manager =
				new ConnectedAssetAdministrationShellManager(new AASRegistryProxy(Server.REGISTRYPATH));

		// Retrieve submodel
		ISubmodel submodel = manager.retrieveSubmodel(Server.MASAASID, Server.OBJFID);

		// Retrieve cost Property
		ISubmodelElement function = submodel.getSubmodelElement("mathFunction");

		// Print value
		String str = (String) function.getValue();
		System.out.println(function.getValue());
		
		//Parsii for java
		Scope scope = new Scope(); 
		Variable a = scope.create("a");
        Variable b = scope.create("b");
        Expression expr = Parser.parse(str, scope);  
		a.setValue(4);
		b.setValue(3);
		System.out.println(expr.evaluate());  
		
		
	}
}
