package multiAgentSystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.basyx.submodel.metamodel.map.*;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.*;



public class PAsubmodel extends Submodel {
	private Property processplan;
	public PAsubmodel() {
		setIdShort("ProductAgent");
		processplan = new Property();
		processplan.setIdShort("currentprocess");
		processplan.setValueType(ValueType.String);
		addSubmodelElement(processplan);
		
	}
	
	
	
	
}
