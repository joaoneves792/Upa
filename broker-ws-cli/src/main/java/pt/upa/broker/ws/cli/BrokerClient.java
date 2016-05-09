package pt.upa.broker.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.BrokerService;

public class BrokerClient {
	public final int MAX_LOOKUPS = 10;
	public BrokerPortType port;
	
	public BrokerClient(String uddiURL, String name) throws BrokerClientException {
		String endpointAddress = null;
		
		try {
			//System.out.printf("Contacting UDDI at %s%n", uddiURL);
			UDDINaming uddiNaming = new UDDINaming(uddiURL);

			int i = 1;
			while(i <= MAX_LOOKUPS){
				//System.out.printf("Looking for '%s'%n", name);
				endpointAddress = uddiNaming.lookup(name);
		
				if (endpointAddress != null) {
					//System.out.println(name + " found!");
					if (i > 1)
						System.out.println("Success!");
					break;
				} 
				
				System.out.println("Trying to establish contact with" + name + "... " + i);				
				if (i == MAX_LOOKUPS) {
					//System.out.println(name + " not found!");
					throw new BrokerClientException(String.format("Service with name %s not found on UDDI at %s", name, uddiURL));
				}

				try {
					Thread.sleep(1000);
				} catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				i++;
			}
			
			//System.out.println("Creating stub ...");
			BrokerService service = new BrokerService();
			this.port = service.getBrokerPort();
	
			//System.out.println("Setting endpoint address ...");
			BindingProvider bindingProvider = (BindingProvider) port;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
			
		} catch (JAXRException e) {
			BrokerClientException ex = new BrokerClientException(String.format("Client failed lookup on UDDI at %s!", uddiURL));
			ex.initCause(e);
			throw ex;
		}
	}
	
	public BrokerPortType getPort(){
		return port;
	}
}
