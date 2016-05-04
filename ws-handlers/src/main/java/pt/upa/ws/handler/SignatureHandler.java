package pt.upa.ws.handler;

import pt.upa.a45.CA.cli.CAClient;
import pt.upa.a45.CA.cli.CAException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;



public class SignatureHandler implements SOAPHandler<SOAPMessageContext> {

	public static final String REQUEST_PROPERTY = "my.request.property";
	public static final String RESPONSE_PROPERTY = "my.response.property";

	public static final String REQUEST_HEADER = "myRequestHeader";
	public static final String REQUEST_NS = "urn:example";

	public static final String RESPONSE_HEADER = "myResponseHeader";
	public static final String RESPONSE_NS = REQUEST_NS;

	public static final String CLASS_NAME = SignatureHandler.class.getSimpleName();
	public static final String TOKEN = "client-handler";

	private final char[] PASSWORD = "123456".toCharArray();

	private final String UDDI_URL = "http://localhost:9090"; /*This shouldnt be a constant, but I dont see any other way (First incoming message)*/

	private static KeyStore _ks = null;
	private CAClient _ca;

	public Set<QName> getHeaders() {
		return null;
	}

	private KeyStore loadKeystore(String filename, char[] password){
		FileInputStream fis;
		try {
			fis = new FileInputStream(filename);
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(fis, password);
			return keystore;
		} catch (FileNotFoundException e) {
			System.err.println("Keystore file <" + filename + "> not fount.");
			System.exit(-1);
		} catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e){
			System.err.println("Failed to load the Keystore" + e.getMessage());
			System.exit(-1);
		}
		return null;
	}

	private void initializeKeyStore(SOAPMessageContext smc){
		String name = (String)smc.get("wsName");
		//String uddiURL = (String)smc.get("uddiURL");
		
		System.out.println("[Handler] We are being requested by: " + name);
		
		if(name.equals("transporterClient"))
			return;
		
		loadKeystore(name + ".jks", PASSWORD);

	}

	private void initializeCAClient(){
		try {
			_ca = new CAClient(UDDI_URL);
		} catch (CAException e) {
			System.err.println("Failed to initializeKeyStore the CA client " + e.getMessage());
			System.exit(-1);
		}

	}



	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		/*Initialize the keystore and CA client*/
		if(_ks == null && outbound) {/* If its inbound then we dont need the keystore*/
			initializeKeyStore(smc);
		}
		if(null == _ca){
			initializeCAClient();
		}
		
		// outbound //
		if (outbound) {
			// get token from request context
			String propertyValue = (String) smc.get(REQUEST_PROPERTY);
			System.out.printf("%s received '%s'%n", CLASS_NAME, propertyValue);
			
			// put token in request SOAP header
			try {
				SOAPEnvelope soapEnvelope = smc.getMessage().getSOAPPart().getEnvelope();
				
				SOAPHeader soapHeader = soapEnvelope.getHeader();
				if (soapHeader == null)
					soapHeader = soapEnvelope.addHeader();
				
				Name soapNamespace;
				SOAPHeaderElement soapHElement;
				
				// add header element (name, namespace prefix, namespace)
				soapNamespace = soapEnvelope.createName("nounce", "upa", "http://pt.upa.header");
				soapHElement = soapHeader.addHeaderElement(soapNamespace);
				soapHElement.addTextNode("80085");
				
				soapNamespace = soapEnvelope.createName("sender", "upa", "http://pt.upa.header");
				soapHElement = soapHeader.addHeaderElement(soapNamespace);
				soapHElement.addTextNode("__sender_name__");
				
				soapNamespace = soapEnvelope.createName("signature", "upa", "http://pt.upa.header");
				soapHElement = soapHeader.addHeaderElement(soapNamespace);
				soapHElement.addTextNode("__signature_place_holder__");
				
			} catch (SOAPException e) {
				System.out.printf("Failed to add SOAP header because of %s%n", e);
			}
			
// inbound //
		} else {
			try {
				SOAPEnvelope soapEnvelope = smc.getMessage().getSOAPPart().getEnvelope();
				SOAPHeader soapHeader = soapEnvelope.getHeader();
				
				Name soapNamespace;
				SOAPElement soapElement;
				Iterator it;
				String headerValue;
				
				// check header
				if (soapHeader == null) {
					System.out.println("Header not found.");
					return true;
				}
				
				// get nounce header element
				soapNamespace = soapEnvelope.createName("nounce", "upa", "http://pt.upa.header");
				it = soapHeader.getChildElements(soapNamespace);
				// check header element
				if (!it.hasNext()) {
					System.out.printf("Header element %s not found.%n", RESPONSE_HEADER);
					return true;
				}
				soapElement = (SOAPElement) it.next();
				
				headerValue = soapElement.getValue();
				System.out.printf("%s got (nounce)\t\t'%s' %n", CLASS_NAME, headerValue);
				
				
				// get sender header element
				soapNamespace = soapEnvelope.createName("sender", "upa", "http://pt.upa.header");
				it = soapHeader.getChildElements(soapNamespace);
				// check header element
				if (!it.hasNext()) {
					System.out.printf("Header element %s not found.%n", RESPONSE_HEADER);
					return true;
				}
				soapElement = (SOAPElement) it.next();
				
				headerValue = soapElement.getValue();
				System.out.printf("%s got (sender)\t\t'%s'%n", CLASS_NAME, headerValue);
				
				// get signature header element
				soapNamespace = soapEnvelope.createName("signature", "upa", "http://pt.upa.header");
				it = soapHeader.getChildElements(soapNamespace);
				// check header element
				if (!it.hasNext()) {
					System.out.printf("Header element %s not found.%n", RESPONSE_HEADER);
					return true;
				}
				soapElement = (SOAPElement) it.next();
				
				headerValue = soapElement.getValue();
				System.out.printf("%s got (signature)\t'%s' %n", CLASS_NAME, headerValue);
				
			} catch (SOAPException e) {
				System.out.printf("Failed to get SOAP header because of %s%n", e);
			}
			
		}
		
		return true;
	}

	public boolean handleFault(SOAPMessageContext smc) {
		return true;
	}


	public void close(MessageContext messageContext) {
		
	}

}
