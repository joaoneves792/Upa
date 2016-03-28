package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

@WebService(
	endpointInterface="pt.upa.broker.ws.BrokerPortType",
    wsdlLocation="broker.1_0.wsdl",
    name="UpaBroker",
    portName="BrokerPort",
    targetNamespace="http://ws.broker.upa.pt/",
    serviceName="BrokerService"
)
public class BrokerPort implements BrokerPortType {

	// TODO
	
	
    public String ping(String name) {
    	
//     	try {  		
// 			UDDINaming uddiNaming = new UDDINaming("http://localhost:9090");
// 			String endpointAddress = uddiNaming.lookup("UpaTransporter1");
// 			System.out.printf("Looking for '%s'%n", "UpaTransporter1");
// 			
// 			TransporterService service = new TransporterService();
// 			TransporterPortType port = service.getTransporterPort();
// 
// 			BindingProvider bindingProvider = (BindingProvider) port;
// 			Map<String, Object> requestContext = bindingProvider.getRequestContext();
// 			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
			
// 	    	return port.ping(name + " pong");

//     	} catch (JAXRException e) {
// 	    	return name + " pong";
//     	}

		return name + " pong";


    }
	

    public String requestTransport(String origin, String destination, int price)
            throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    	
    	return "FIXME";
    }
    
    
    public TransportView viewTransport(String id)
            throws UnknownTransportFault_Exception {
    	
    	return new TransportView();
    }
    
    public List<TransportView> listTransports() {
    	
    	return new ArrayList<TransportView>();
    }

    public void clearTransports() {
    	
    }
    
}
