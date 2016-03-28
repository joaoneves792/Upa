package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import pt.upa.transporter.ws.cli.TransporterClient; //Error: Package not found

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
	
	@Override
    public String ping(String name) {
		try {
			TransporterClient client = TransporterClient("http://localhost:9090", "UpaTransporter1");
			return client.port.ping(name + " pong");
		} catch(Exception e){
			System.out.println(e.getMessage());
			return name + " pong";
		}
    }
	
	@Override
    public String requestTransport(String origin, String destination, int price)
            throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    	
    	return "FIXME";
    }
    
	@Override
    public TransportView viewTransport(String id)
            throws UnknownTransportFault_Exception {
    	
    	return new TransportView();
    }
    
	@Override
    public List<TransportView> listTransports() {
    	
    	return new ArrayList<TransportView>();
    }

	@Override
    public void clearTransports() {
    	
    }
    
}
