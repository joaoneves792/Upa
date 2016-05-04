package pt.upa.ws.handler;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
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

	private KeyManager _keyManager;


	private void addNounce(SOAPEnvelope soapEnvelope, SOAPHeader soapHeader) throws SOAPException {
// 		Long nounce = new BigInteger(64, new Random().longValue());
		Name soapNamespace;
		SOAPHeaderElement soapHElement;
				
		soapNamespace = soapEnvelope.createName("nounce", "upa", "http://pt.upa.header");
		soapHElement = soapHeader.addHeaderElement(soapNamespace);
// 		soapHElement.addTextNode(nounce.toString());
		soapHElement.addTextNode("80085");
	}
	
	
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		/* Initialize the keystore and CA client */
		if(outbound) {
			String name = (String)smc.get("wsName");
			_keyManager = KeyManager.getInstance(name);
		}else{
			_keyManager = KeyManager.getInstance(null);
		}
		
// outbound //
		if (outbound) {
		// THIS is an example on how to get your own private key and everyone else's certificates
		// ----------------------------->
			try {
				//You can only get your private key when message is outbound
				PrivateKey mykey = _keyManager.getMyPrivateKey(); 
				
				 //Certs you can get whenever you want
				X509Certificate cert1 = _keyManager.getCertificate("UpaTransporter1");
				X509Certificate cert2 = _keyManager.getCertificate("UpaTransporter2");
				X509Certificate cert3 = _keyManager.getCertificate("UpaBroker");
				
				//Force to refresh this certificate
				X509Certificate forcedRefreshCert = _keyManager.forceCertificateRefresh("UpaBroker"); 
				
				//Get the CA certificate (stored locally on our Keystore)
				X509Certificate cacert = _keyManager.getCACertificate(); 
				
				/*Some asserts for testing...*/
				assert null != cacert;
				assert null != forcedRefreshCert;
				assert null != cert1;
				assert null != cert2;
				assert null != cert3;
				assert null != mykey;
			}catch (Exception e){
				System.out.println(e.toString());
				System.exit(-1);
			}
		// <-------------------------------
			
			// put token in request SOAP header
			try {
				SOAPEnvelope soapEnvelope = smc.getMessage().getSOAPPart().getEnvelope();
				
				SOAPHeader soapHeader = soapEnvelope.getHeader();
				if (soapHeader == null)
					soapHeader = soapEnvelope.addHeader();
				
				Name soapNamespace;
				SOAPHeaderElement soapHElement;
				
				// add header element (name, namespace prefix, namespace)
				addNounce(soapEnvelope, soapHeader);
				
				soapNamespace = soapEnvelope.createName("sender", "upa", "http://pt.upa.header");
				soapHElement = soapHeader.addHeaderElement(soapNamespace);
				soapHElement.addTextNode((String)smc.get("wsName"));
				
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
					System.out.println("Nounce element not found.");
					return false;
				}
				soapElement = (SOAPElement) it.next();
				
				headerValue = soapElement.getValue();
				System.out.println("SignatureHandler got (nounce)\t\t" + headerValue);
				
				
				// get sender header element
				soapNamespace = soapEnvelope.createName("sender", "upa", "http://pt.upa.header");
				it = soapHeader.getChildElements(soapNamespace);
				// check header element
				if (!it.hasNext()) {
					System.out.println("Sender element not found.");
					return false;
				}
				soapElement = (SOAPElement) it.next();
				
				headerValue = soapElement.getValue();
				System.out.println("SignatureHandler got (sender)\t\t" + headerValue);
				
				// get signature header element
				soapNamespace = soapEnvelope.createName("signature", "upa", "http://pt.upa.header");
				it = soapHeader.getChildElements(soapNamespace);
				// check header element
				if (!it.hasNext()) {
					System.out.println("Signature element not found.");
					return false;
				}
				soapElement = (SOAPElement) it.next();
				
				headerValue = soapElement.getValue();
				System.out.println("SignatureHandler got (signature)\t\t" + headerValue);
				
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
	
	public Set<QName> getHeaders() {
		return null;
	}
	
}
