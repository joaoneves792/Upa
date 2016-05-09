package pt.upa.broker.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.BrokerService;

public class BrokerClient {
	private final int MAX_LOOKUPS = 10;
	private BrokerPortType _port;
	
	String _uddiURL;
	private UDDINaming _uddiNaming;
	private String _name;
	
	
	public BrokerClient(String uddiURL, String name) throws BrokerClientException {		
		try {
			//System.out.printf("Contacting UDDI at %s%n", uddiURL);
			_uddiURL = uddiURL;
			_name = name;
			UDDINaming _uddiNaming = new UDDINaming(_uddiURL);
			
		} catch (JAXRException e) {
			BrokerClientException ex = new BrokerClientException(String.format("Client failed lookup on UDDI at %s!", _uddiURL));
			ex.initCause(e);
			throw ex;
		}
	}
	
	public BrokerPortType getPort() throws BrokerClientException {
		String endpointAddress = null;
		int i = 0;
		try {
			while(i <= MAX_LOOKUPS) {
				//System.out.printf("Looking for '%s'%n", _name);
				endpointAddress = _uddiNaming.lookup(_name);
		
				if (endpointAddress != null) {
					//System.out.println(_name + " found!");
					if (i > 1)
						System.out.println("Success!");
					break;
				} 
				
				System.out.println("Trying to establish contact with" + _name + "... " + i);				
				if (i == MAX_LOOKUPS) {
					//System.out.println(_name + " not found!");
					throw new BrokerClientException(String.format("Service with name %s not found on UDDI at %s", _name, _uddiURL));
				}
				
				try {
					Thread.sleep(1000);
				} catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				i++;
			}
			
			BrokerService service = new BrokerService();
			this._port = service.getBrokerPort();
			
			//System.out.println("Setting endpoint address ...");
			BindingProvider bindingProvider = (BindingProvider) _port;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
			
		} catch (JAXRException e) {
			BrokerClientException ex = new BrokerClientException(String.format("Client failed lookup on UDDI at %s!", _uddiURL));
			ex.initCause(e);
			throw ex;
		}
		
		return _port;
	}

}

