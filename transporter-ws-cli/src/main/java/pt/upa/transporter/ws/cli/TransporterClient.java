package pt.upa.transporter.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;
import java.util.TreeMap;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceContext;
import javax.annotation.Resource;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;

import pt.upa.ws.handler.SignatureHandler;
import java.security.NoSuchAlgorithmException;


public class TransporterClient {
	public TransporterPortType port;
	private Boolean _forgeSignature = false;
	private Boolean _dupNounce = false;
// 	private String _endpointAddress;

	// Yes, this should be in broker-ws, not here. I'm looking into it.
	private Map<String, String> _sentNounces = new TreeMap<String, String>();
	private Map<String, String> _receivedNounces = new TreeMap<String, String>();

	
	@Resource
	private WebServiceContext webServiceContext;

	private String getNounceFromContext() {
		return (String) webServiceContext.getMessageContext().get("recievedNounce");
	}
	
	private void setContext(TransporterPortType port, String endpointAddress) {
		//System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		
		try { 
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
			requestContext.put("wsName", "UpaBroker");
			requestContext.put("wsNounce", SignatureHandler.getSecureRandom(_sentNounces));
			
			if(_forgeSignature)
				requestContext.put("forgeSignature", "true");
			else
				requestContext.put("forgeSignature", "false");
			
			if(_dupNounce)
				requestContext.put("dupNounce", "true");
			else
				requestContext.put("dupNounce", "false");
				
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Failed to generate random: " + e.getMessage());
		}
		
	}
	
	
	public TransporterClient(String uddiURL, String name) throws TransporterClientException {
		try {
			//System.out.printf("Contacting UDDI at %s%n", uddiURL);
			UDDINaming uddiNaming = new UDDINaming(uddiURL);
			
			//System.out.printf("Looking for '%s'%n", name);
			String endpointAddress = uddiNaming.lookup(name);
			
			if (endpointAddress == null) {
				//System.out.println(name + " not found!");
				throw new TransporterClientException(String.format("Service with name %s not found on UDDI at %s", name, uddiURL));
			} else {
				//System.out.printf("Found %s%n", endpointAddress);
			}
			
			//System.out.println("Creating stub ...");
			TransporterService service = new TransporterService();
			this.port = service.getTransporterPort();
			
			setContext(port, endpointAddress);
			
		}catch (JAXRException e) {
			TransporterClientException ex = new TransporterClientException(String.format("Client failed lookup on UDDI at %s!", uddiURL));
			ex.initCause(e);
			throw ex;
		}
	}
	
	public TransporterClient(String endpointAddress) throws TransporterClientException {
		//System.out.println(TransporterClient.class.getSimpleName() + " starting...");
		
		if (endpointAddress == null) {
			//System.out.println("Null endpoint Address!");
			return;
		}
		
		//System.out.println("Creating stub ...");
		TransporterService service = new TransporterService();
		this.port = service.getTransporterPort();
		
		setContext(port, endpointAddress);
	}

	
	public TransporterPortType getPort(){
		return port;
	}
	
	
	
// constructors to test signature verification //

	public TransporterClient(String uddiURL, String name, Boolean forge, Boolean dup)
																	throws TransporterClientException {		
		_forgeSignature = forge;
		_dupNounce = dup;
		
		try {
			//System.out.printf("Contacting UDDI at %s%n", uddiURL);
			UDDINaming uddiNaming = new UDDINaming(uddiURL);
			
			//System.out.printf("Looking for '%s'%n", name);
			String endpointAddress = uddiNaming.lookup(name);
			
			if (endpointAddress == null) {
				//System.out.println(name + " not found!");
				throw new TransporterClientException(String.format("Service with name %s not found on UDDI at %s", name, uddiURL));
			} else {
				//System.out.printf("Found %s%n", endpointAddress);
			}
			
			//System.out.println("Creating stub ...");
			TransporterService service = new TransporterService();
			this.port = service.getTransporterPort();
			
			setContext(port, endpointAddress);
			
		} catch (JAXRException e) {
			TransporterClientException ex = new TransporterClientException(String.format("Client failed lookup on UDDI at %s!", uddiURL));
			ex.initCause(e);
			throw ex;
		}
	}

	public TransporterClient(String endpointAddress, Boolean forge, Boolean dup)
																	throws TransporterClientException {		
		_forgeSignature = forge;
		_dupNounce = dup;
		
		//System.out.println(TransporterClient.class.getSimpleName() + " starting...");
		if (endpointAddress == null) {
			//System.out.println("Null endpoint Address!");
			return;
		}
		
		//System.out.println("Creating stub ...");
		TransporterService service = new TransporterService();
		this.port = service.getTransporterPort();
		
		setContext(port, endpointAddress);
	}

}
