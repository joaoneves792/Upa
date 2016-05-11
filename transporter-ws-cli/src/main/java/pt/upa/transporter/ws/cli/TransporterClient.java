package pt.upa.transporter.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;
import java.util.TreeMap;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.annotation.Resource;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;

import pt.upa.ws.handler.SignatureHandler;
import java.security.NoSuchAlgorithmException;


public class TransporterClient {
	public TransporterPortType port;
	private String _uddiLocation;
	private String _endpointAddress;
	
	// variables for transporter integration tests
	private Integer _nonceCounter = 0;
	private Boolean _ignoreContext = false;
	private Boolean _forgeSignature = false;
	private Boolean _dupNounce = false;


	@Resource
	private WebServiceContext webServiceContext;

	public String getNounceFromContext() {
		MessageContext mc = webServiceContext.getMessageContext();
		return (String) mc.get("recievedNounce");
	}
	
	
	public void setContext(String endpointAddress, String nounce) {
		//System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, _endpointAddress);
			
			requestContext.put("wsName", "UpaBroker");
			
			if(_forgeSignature)
				requestContext.put("forgeSignature", "true");
			else
				requestContext.put("forgeSignature", "false");
			
			if(_dupNounce)
				requestContext.put("wsNounce", SignatureHandler.DUP_NOUNCE);
			else
				requestContext.put("wsNounce", nounce);
	}
	
	
	public TransporterPortType getPort(){
		
		// get a diferent sequence of numbers for each test
		// this sequence is smaller and not in hexadecimal to not conflict with the actual nonces from broker
		if(_ignoreContext == true || _forgeSignature == true || _dupNounce == true)
			setContext("someaddress", Long.toString(System.currentTimeMillis()));
			
		return port;
	}
	
	
	public TransporterClient(String uddiURL, String name) throws TransporterClientException {
		_uddiLocation = uddiURL;
		try {
			//System.out.printf("Contacting UDDI at %s%n", uddiURL);
			UDDINaming uddiNaming = new UDDINaming(uddiURL);
			
			//System.out.printf("Looking for '%s'%n", name);
			String endpointAddress = null;
			if(uddiNaming.lookupRecord(name) != null)
				endpointAddress = uddiNaming.lookupRecord(name).getUrl();
			
			if (endpointAddress == null) {
				//System.out.println(name + " not found!");
				throw new TransporterClientException(String.format("Service with name %s not found on UDDI at %s", name, uddiURL));
			} else {
				//System.out.printf("Found %s%n", endpointAddress);
				_endpointAddress = endpointAddress;
			}
			
			//System.out.println("Creating stub ...");
			TransporterService service = new TransporterService();
			this.port = service.getTransporterPort();
			
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
		
		_endpointAddress = endpointAddress;
				
		//System.out.println("Creating stub ...");
		TransporterService service = new TransporterService();
		this.port = service.getTransporterPort();
		
	}

	
	
// constructors to test signature verification //

	public TransporterClient(String uddiURL, String name, Boolean ignore, Boolean forge, Boolean dup)
																	throws TransporterClientException {
		
		try {
			System.out.printf("Contacting UDDI at %s%n", uddiURL);
			UDDINaming uddiNaming = new UDDINaming(uddiURL);
			
			//System.out.printf("Looking for '%s'%n", name);
			String endpointAddress = uddiNaming.lookup(name);
			
			if (endpointAddress == null) {
				//System.out.println(name + " not found!");
				throw new TransporterClientException(String.format("Service with name %s not found on UDDI at %s", name, uddiURL));
			} else {
				//System.out.printf("Found %s%n", endpointAddress);
				_endpointAddress = endpointAddress;
			}
			
			//System.out.println("Creating stub ...");
			TransporterService service = new TransporterService();
			this.port = service.getTransporterPort();
			
		} catch (JAXRException e) {
			TransporterClientException ex = new TransporterClientException(String.format("Client failed lookup on UDDI at %s!", uddiURL));
			ex.initCause(e);
			throw ex;
		}
		_uddiLocation = uddiURL;
		_ignoreContext = ignore;
		_forgeSignature = forge;
		_dupNounce = dup;
	}

	public TransporterClient(String endpointAddress, Boolean ignore, Boolean forge, Boolean dup)
																	throws TransporterClientException {		
		
// 		this.TransporterClient(endpointAddress);
		
		_ignoreContext = ignore;
		_forgeSignature = forge;
		_dupNounce = dup;
		
		//System.out.println(TransporterClient.class.getSimpleName() + " starting...");
		if (endpointAddress == null) {
			//System.out.println("Null endpoint Address!");
			return;
		}
		
		_endpointAddress = endpointAddress;		
		
		//System.out.println("Creating stub ...");
		TransporterService service = new TransporterService();
		this.port = service.getTransporterPort();
		
	}

}

	
	
	