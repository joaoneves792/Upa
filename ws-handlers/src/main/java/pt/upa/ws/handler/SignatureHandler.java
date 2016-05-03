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
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
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

	private KeyStore _ks = null;
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

	private void initialize(SOAPMessageContext smc){
		String name = (String)smc.get("wsName");
		String uddiURL = (String)smc.get("uddiURL");
		
		System.out.println("[Handler] We are being requested by: " + name);
		
		/*if(name.equals("transporterClient"))
			return;
		
		loadKeystore(name + ".jks", PASSWORD);
		try {
			_ca = new CAClient(uddiURL);
		} catch (CAException e) {
			System.err.println("Failed to initialize the CA client " + e.getMessage());
			System.exit(-1);
		}*/
		
	}

	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		/*Initialize the keystore and CA client*/
		if(_ks == null){
			initialize(smc);
		}
		
		if (outbound) {
			// get token from request context
			String propertyValue = (String) smc.get(REQUEST_PROPERTY);
			System.out.printf("%s received '%s'%n", CLASS_NAME, propertyValue);
			
			// put token in request SOAP header
			try {
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				
				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();
				
				// add header element (name, namespace prefix, namespace)
				Name name = se.createName(REQUEST_HEADER, "e", REQUEST_NS);
				SOAPHeaderElement element = sh.addHeaderElement(name);
				
				// add header element value
				String newValue = propertyValue + "," + TOKEN;
				element.addTextNode(newValue);
				
				System.out.printf("%s put token '%s' on request message header%n", CLASS_NAME, newValue);
				
			} catch (SOAPException e) {
				System.out.printf("Failed to add SOAP header because of %s%n", e);
			}
			
		} else {	// inbound
			// get token from response SOAP header
			try {
				// get SOAP envelope header
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();
				
				// check header
				if (sh == null) {
					System.out.println("Header not found.");
					return true;
				}
				
				// get first header element
				Name name = se.createName(RESPONSE_HEADER, "e", RESPONSE_NS);
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					System.out.printf("Header element %s not found.%n", RESPONSE_HEADER);
					return true;
				}
				SOAPElement element = (SOAPElement) it.next();
				
				// get header element value
				String headerValue = element.getValue();
				System.out.printf("%s got '%s'%n", CLASS_NAME, headerValue);
				
				// put token in response context
				String newValue = headerValue + "," + TOKEN;
				System.out.printf("%s put token '%s' on response context%n", CLASS_NAME, TOKEN);
				smc.put(RESPONSE_PROPERTY, newValue);
				// set property scope to application so that client class can
				// access property
				smc.setScope(RESPONSE_PROPERTY, Scope.APPLICATION);
				
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
