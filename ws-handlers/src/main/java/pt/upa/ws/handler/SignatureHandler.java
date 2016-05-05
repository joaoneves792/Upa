package pt.upa.ws.handler;

import static javax.xml.bind.DatatypeConverter.printHexBinary;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.GeneralSecurityException;

import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;


import java.io.StringWriter;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class SignatureHandler implements SOAPHandler<SOAPMessageContext> {
	
	private KeyManager _keyManager;
	
	
	private void addHeaderElement(SOAPEnvelope soapEnv, String property, String value) throws SOAPException {
		SOAPHeader soapHeader = soapEnv.getHeader();
		Name soapNamespace = soapEnv.createName(property, "upa", "http://pt.upa.header");
		SOAPHeaderElement soapHElement = soapHeader.addHeaderElement(soapNamespace);
		soapHElement.addTextNode(value);
	}
	
	private SOAPElement getHeaderElement(SOAPEnvelope soapEnv, String property) throws SOAPException {
		SOAPHeader soapHeader = soapEnv.getHeader();
		Name soapNamespace = soapEnv.createName(property, "upa", "http://pt.upa.header");
		
		Iterator it = soapHeader.getChildElements(soapNamespace);
		if (!it.hasNext())
			return null;
		
		return (SOAPElement) it.next();
	}
	
	private String getSecureRandom(String senderName) throws NoSuchAlgorithmException {
		SecureRandom nounce;
		final byte array[] = new byte[16];
		String str = "";
		do {
			nounce = SecureRandom.getInstance("SHA1PRNG");
			nounce.nextBytes(array);
			str = printHexBinary(array);
			
		} while(_keyManager.containsNounce(senderName+"sent", str));
		
		return str;
	}
	
	private String getSignedDigest(String input) throws NoSuchAlgorithmException, GeneralSecurityException {
		final byte[] bytes = input.getBytes();
		
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
		messageDigest.update(bytes);
		byte[] digest = messageDigest.digest();
		
		PrivateKey privateKey = _keyManager.getMyPrivateKey();
		Signature sig = Signature.getInstance("SHA1WithRSA");
		
		sig.initSign(privateKey);
		sig.update(digest);
		byte[] output = sig.sign();
		
		return printHexBinary(output);
	}
	
	// FIXME throw right exceptions
	private String getSOAPBodyAsString(SOAPMessageContext smc) throws Exception {
		SOAPBody element = smc.getMessage().getSOAPBody();
		DOMSource source = new DOMSource(element);
		StringWriter stringResult = new StringWriter();
		
		TransformerFactory tff = TransformerFactory.newInstance();
		tff.newTransformer().transform(source, new StreamResult(stringResult));
		
		return stringResult.toString();
	}

	
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		/* Initialize the keystore and CA client */
		if(outbound) {
			String name = (String)smc.get("wsName");
			_keyManager = KeyManager.getInstance(name);
		} else {
			_keyManager = KeyManager.getInstance(null);
		}
		
// outbound //
		if (outbound) {
// 		// THIS is an example on how to get your own private key and everyone else's certificates
// 		// ----------------------------->
// 			try {
// 				//You can only get your private key when message is outbound
// 				PrivateKey mykey = _keyManager.getMyPrivateKey(); 
// 				
// 				 //Certs you can get whenever you want
// 				X509Certificate cert1 = _keyManager.getCertificate("UpaTransporter1");
// 				X509Certificate cert2 = _keyManager.getCertificate("UpaTransporter2");
// 				X509Certificate cert3 = _keyManager.getCertificate("UpaBroker");
// 				
// 				//Force to refresh this certificate
// 				X509Certificate forcedRefreshCert = _keyManager.forceCertificateRefresh("UpaBroker"); 
// 				
// 				//Get the CA certificate (stored locally on our Keystore)
// 				X509Certificate cacert = _keyManager.getCACertificate(); 
// 				
// 				/*Some asserts for testing...*/
// 				assert null != cacert;
// 				assert null != forcedRefreshCert;
// 				assert null != cert1;
// 				assert null != cert2;
// 				assert null != cert3;
// 				assert null != mykey;
// 			}catch (Exception e){
// 				System.out.println(e.toString());
// 				System.exit(-1);
// 			}
// 		// <-------------------------------
			
			// put token in request SOAP header
			try {
				SOAPEnvelope soapEnvelope = smc.getMessage().getSOAPPart().getEnvelope();
				
				// create header if it does not exist
				SOAPHeader soapHeader = soapEnvelope.getHeader();
				if (soapHeader == null)
					soapHeader = soapEnvelope.addHeader();
				
				final String senderName = (String)smc.get("wsName");
				addHeaderElement(soapEnvelope, "sender", senderName);
				
				final String nounce = getSecureRandom(senderName);
				addHeaderElement(soapEnvelope, "nounce", nounce);
				
				final String signature = getSignedDigest(senderName + nounce + getSOAPBodyAsString(smc));
				addHeaderElement(soapEnvelope, "signature", signature);
				
			} catch (SOAPException e) {
				System.out.printf("Failed to add SOAP header because of %s%n", e);
			} catch (GeneralSecurityException e) {
				System.out.println("Failed to create a header element: " + e.getMessage());
				return false;
			} catch (Exception e) {
				// FIXME don't catch everything
				System.out.printf("\nException in handler: %s%n", e);
			}
			
// inbound //
		} else {
			try {
				SOAPEnvelope soapEnvelope = smc.getMessage().getSOAPPart().getEnvelope();
				SOAPHeader soapHeader = soapEnvelope.getHeader();

				SOAPElement headerElement;
				
				// check header
				if (soapHeader == null) {
					System.out.println("Header not found.");
					return true;
				}
				
				// get sender header element
				headerElement = getHeaderElement(soapEnvelope, "sender");
				if(headerElement == null) {
					System.out.println("Sender element not found.");
					return false;
				}
				String senderName = headerElement.getValue();
// 				System.out.println("SignatureHandler got (sender)\t\t" + senderName);	
				
				X509Certificate cert = _keyManager.getCertificate(senderName);
				// FIXME get public key
				
				// get nounce header element
				headerElement = getHeaderElement(soapEnvelope, "nounce");
				if(headerElement == null) {
					System.out.println("Nounce element not found.");
					return false;
				}
				String nounce = headerElement.getValue();
// 				System.out.println("SignatureHandler got (nounce)\t\t" + nounce);
				if(!senderName.equals("UpaBroker"))
					if(_keyManager.containsNounce("UpaBroker"+senderName+"recieved", nounce)) {
					System.out.println("Repeated nounce.");
					return false;
				}
				
				// get signature header element
				headerElement = getHeaderElement(soapEnvelope, "signature");
				if(headerElement == null) {
					System.out.println("Signature element not found.");
					return false;
				}
				String signature = headerElement.getValue();
// 				System.out.println("SignatureHandler got (sender)\t\t" + signature);	
				
				// FIXME verify signature
				
			} catch (SOAPException e) {
				System.out.printf("Failed to get SOAP header because of %s%n", e);
			} catch (Exception e){
				System.out.println(e.toString());
				System.exit(-1);
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
