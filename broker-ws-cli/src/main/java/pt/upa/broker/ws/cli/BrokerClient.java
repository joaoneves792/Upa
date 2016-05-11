
package pt.upa.broker.ws.cli;

import pt.upa.broker.ws.BrokerPortType;
import javax.xml.registry.JAXRException;


public class BrokerClient {
	private String _uddiURL;
	private BrokerPortFrontEnd _frontEnd;

	
	public BrokerClient(String uddiURL, String name) throws BrokerClientException {		
		try {
			_uddiURL = uddiURL;
			
			// it is assumed there are no failures regarding uddiNaming
			_frontEnd = new BrokerPortFrontEnd(uddiURL, name);
			
		} catch (JAXRException e) {
			BrokerClientException ex = new BrokerClientException(String.format("Client failed lookup on UDDI at %s!", _uddiURL));
			ex.initCause(e);
			throw ex;
		}
	}
	
	public BrokerPortFrontEnd getPort() throws BrokerClientException {
		if(_frontEnd == null)
			throw new BrokerClientException("FrontEnd failure.");
		else
			return _frontEnd;
	}

}

