
package pt.upa.broker.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;
import java.util.List;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;
import pt.upa.broker.ws.*;

import java.net.ConnectException;
import java.io.IOException;
import java.rmi.RemoteException;
import com.sun.xml.ws.client.ClientTransportException;

public class BrokerPortFrontEnd implements BrokerPortType {

	private final int MAX_LOOKUPS = 10;
	
	private String _uddiURL;
	private UDDINaming _uddiNaming;
	private String _name;

	private BrokerPortType _port;
	
	public BrokerPortFrontEnd(String uddiURL, String name) throws JAXRException, BrokerClientException {
		//System.out.printf("Contacting UDDI at %s%n", uddiURL);
		_uddiURL = uddiURL;
		_name = name;
		
		// it is assumed there are no failures regarding uddiNaming
		_uddiNaming = new UDDINaming(_uddiURL);
		
		updatePort();
	}
	
	
	
// BrokerPort management //

	public BrokerPortType getPort() {
		return _port;
	}
	
	public void updatePort() throws BrokerClientException {
		String endpointAddress = null;
		
		for(int i = 0; i <= MAX_LOOKUPS; i++) {
			try {
				if(_name == "nope") throw new ConnectException("nopenope");
						
				System.out.println("Trying to establish contact with" + _name + "... " + i);
				UDDIRecord record = _uddiNaming.lookupRecord(_name);
				if(_uddiNaming.lookupRecord(_name) == null) {
					throw new JAXRException("Invalid uddi record.");
				
				} else {
					endpointAddress = record.getUrl();
					if(endpointAddress == null) {
						throw new JAXRException("Invalid endpoint address.");
					}
				}
				
				BrokerService service = new BrokerService();
				this._port = service.getBrokerPort();
				
				//System.out.println("Setting endpoint address ...");
				BindingProvider bindingProvider = (BindingProvider) _port;
				Map<String, Object> requestContext = bindingProvider.getRequestContext();
				requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
				
				// force exception throwing if broker is down
				_port.ping("ping");
				
				System.out.println(_name + " found.");
				return;
			
			
			} catch (Exception e) {
				System.out.println(_name + " is down!");
			}
				try {
					Thread.sleep(2000);
				} catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
		}
		
		throw new BrokerClientException("No broker was found.");
	}
	

	
// BrokerPortType interface methods //

	public void updateState(UpdateAction action, TransportView transport) {
			try {
				if(_name == "nope") throw new ConnectException("nopenope");
					
				_port.updateState(action, transport);
				return;
			
			
			} catch (ConnectException | WebServiceException e1) {
				System.out.println(e1.getMessage());
				
				try {
					if(_name == "nope") throw new ConnectException("nopenope");
					
					updatePort();
					_port.updateState(action, transport);
					return;	
					
				} catch(ConnectException | WebServiceException | BrokerClientException e2) {
					System.out.println("Failed to contact broker.\n" + e2.getMessage());
					return;
				}
			}
	}

	public void updateNounce(UpdateNounceDirection direction, String nounce) {
			try {
				if(_name == "nope")
					throw new ConnectException("nopenope");
				
				_port.updateNounce(direction, nounce);
				return;
				
			} catch (ConnectException | WebServiceException e1) {
				System.out.println(e1.getMessage());
				
				try {
					if(_name == "nope") throw new ConnectException("nopenope");
					
					updatePort();
					_port.updateNounce(direction, nounce);
					return;
				
				} catch(ConnectException | WebServiceException | BrokerClientException e2) {
					System.out.println("Failed to contact broker.\n" + e2.getMessage());
					return;
				}
			}

	}
	
	public String ping(String name) {
		try {
			if(_name == "nope")
					throw new ConnectException("nopenope");
					
			return _port.ping(name);
				
		} catch (ConnectException | WebServiceException e1) {
			System.out.println(e1.getMessage());
			
			try {
				if(_name == "nope") throw new ConnectException("nopenope");
				
				updatePort();
				return _port.ping(name);
				
			} catch(ConnectException | WebServiceException | BrokerClientException e2) {
				System.out.println("Failed to contact broker.\n" + e2.getMessage());
				return null;
			}
		}
	}
	
    public String requestTransport(String origin, String destination, int price)
				throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
						UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
			
			try {
			if(_name == "nope")
					throw new ConnectException("nopenope");
					
				return _port.requestTransport(origin, destination, price);
			
			} catch (InvalidPriceFault_Exception e1) { throw e1;
			} catch (UnavailableTransportFault_Exception e1) { throw e1;
			} catch (UnavailableTransportPriceFault_Exception e1) { throw e1;
			} catch (UnknownLocationFault_Exception e1) { throw e1;
			} catch (ConnectException | WebServiceException e1) {
				System.out.println(e1.getMessage());
				
				try {
					if(_name == "nope") throw new ConnectException("nopenope");
					
					updatePort();
					return _port.requestTransport(origin, destination, price);
					
				} catch(ConnectException | WebServiceException | BrokerClientException e2) {
					System.out.println("Failed to contact broker.\n" + e2.getMessage());
					return null;
				}
			}

	}
	      
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
			try {
			if(_name == "nope")
					throw new ConnectException("nopenope");
					
				return _port.viewTransport(id);
				
			} catch (UnknownTransportFault_Exception e1) { throw e1;
			} catch (ConnectException | WebServiceException e1) {
			System.out.println(e1.getMessage());
				
				try {
					if(_name == "nope") throw new ConnectException("nopenope");
					
					updatePort();
					return _port.viewTransport(id);
				
				} catch(ConnectException | WebServiceException | BrokerClientException e2) {
					System.out.println("Failed to contact broker.\n" + e2.getMessage());
					return null;
				}
			}
	}
	
    public List<TransportView> listTransports() {	
		try {
			if(_name == "nope")
					throw new ConnectException("nopenope");
					
				return _port.listTransports();
				
			} catch (ConnectException | WebServiceException e1) {
				System.out.println(e1.getMessage());
				
				try {
					if(_name == "nope") throw new ConnectException("nopenope");
					
					updatePort();
					return _port.listTransports();
					
				} catch(ConnectException | WebServiceException | BrokerClientException e2) {
					System.out.println("Failed to contact broker.\n" + e2.getMessage());
					return null;
				}
			}
	}

    public void clearTransports() {
		try {
			if(_name == "nope")
					throw new ConnectException("nopenope");
					
				_port.clearTransports();
				return;
				
		} catch (ConnectException | WebServiceException e1) {
			System.out.println(e1.getMessage());
			
			try {
				if(_name == "nope") throw new ConnectException("nopenope");
				
				updatePort();
				_port.clearTransports();
				return;
			
			} catch(ConnectException | WebServiceException | BrokerClientException e2) {
				System.out.println("Failed to contact broker.\n" + e2.getMessage());
				return;
			}
		}
	}
	
}
