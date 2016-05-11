package pt.upa.ws.handler;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.GeneralSecurityException;

import java.util.Iterator;
import java.util.Set;
import java.util.Map;

import java.io.StringWriter;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;


public class SignatureHandler implements SOAPHandler<SOAPMessageContext> {
	
	private KeyManager _keyManager;
	
	public static String DUP_NOUNCE = "B00B135B00B13SB00B135B00B13SB00B";
	
// functions to manage server nounces
	public static String getSecureRandom(Map<String, String> sent) throws NoSuchAlgorithmException {
		SecureRandom nounce;
		final byte array[] = new byte[16];
		String str = "";
		do {
			nounce = SecureRandom.getInstance("SHA1PRNG");
			nounce.nextBytes(array);
			str = printHexBinary(array);
			
		} while(sent.get(str) != null);
		
		sent.put(str, str);
		return str;
	}

	public static Boolean nounceIsValid(Map<String, String> received, String nounce) {
		
// 		System.out.println("\n\n" + nounce + "\n\n");
		
		if(received.get(nounce) == null) {
			received.put(nounce, nounce);
			return true;
		} else
			return false;
	}
	
	private void writeNounceToFile(String path, String content) {
		Charset charset = Charset.forName("US-ASCII");
		String absPath = new File("").getAbsolutePath() + path;
		
		Path filePath = Paths.get(absPath);
		
		try (BufferedWriter writer = Files.newBufferedWriter(filePath, charset)) {
			writer.write(content, 0, content.length());
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
	}
	
	
// --------------------
	
	
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
	

	// FIXME throw right exceptions
	private String getSOAPBodyAsString(SOAPMessageContext smc) throws Exception {
		SOAPBody element = smc.getMessage().getSOAPBody();
		DOMSource source = new DOMSource(element);
		StringWriter stringResult = new StringWriter();
		
		TransformerFactory tff = TransformerFactory.newInstance();
		tff.newTransformer().transform(source, new StreamResult(stringResult));
		
		return stringResult.toString();
	}
	
	private String getSignedDigest(String input) throws NoSuchAlgorithmException, GeneralSecurityException {
		final byte[] bytes = input.getBytes();
		
		PrivateKey privateKey = _keyManager.getMyPrivateKey();
		Signature sig = Signature.getInstance("SHA256withRSA");
		
		sig.initSign(privateKey);
		sig.update(bytes);
		byte[] output = sig.sign();
		
		return printHexBinary(output);
	}
	
	private boolean signatureIsValid(String signature, String message, PublicKey publicKey) throws Exception {
		// signature comes in hexadecimal
		// message os computed localy and is a regular string
		final byte[] signatureBytes = DatatypeConverter.parseHexBinary(signature);
		final byte[] digestBytes = message.getBytes();

		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(publicKey);
		sig.update(digestBytes);
		try {
			return sig.verify(signatureBytes);
		} catch (SignatureException se) {
			System.err.println("Caught exception while verifying signature " + se);
			return false;
		}
	}

	private String corruptSignature(String signature) {
		char[] charArray = signature.toCharArray();
		
		// assuming signature has at least 2 characters
		char temp = charArray[0];
		charArray[0] = charArray[1];
		charArray[1] = temp;
		
		return new String(charArray);
	}

	
	public boolean handleMessage(SOAPMessageContext smc) {
		
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		/* Initialize the keystore and CA client */
		if(outbound) {
			String name = (String)smc.get("wsName");
			_keyManager = KeyManager.getInstance(name);
			
			return handleOutbound(smc);
			
		} else {
			_keyManager = KeyManager.getInstance(null);
			
			return handleInbound(smc);
		}
		
// 		return true;
	}
	
	
	public boolean handleFault(SOAPMessageContext smc) {
		return true;
	}

	public void close(MessageContext messageContext) {
		
	}
	
	public Set<QName> getHeaders() {
		return null;
	}
	
		
	public boolean handleOutbound(SOAPMessageContext smc) {
			
			System.out.println("HELLO");
		// put token in request SOAP header
		try {
			SOAPEnvelope soapEnvelope = smc.getMessage().getSOAPPart().getEnvelope();
			// create header if it does not exist
			SOAPHeader soapHeader = soapEnvelope.getHeader();
			if (soapHeader == null)
				soapHeader = soapEnvelope.addHeader();
			
// 			String ignore = (String)smc.get("wsIgnore");
// 			if(ignore != null)
// 				addHeaderElement(soapEnvelope, "wsIgnore", "true");
			
			final String senderName = (String)smc.get("wsName");
			addHeaderElement(soapEnvelope, "sender", senderName);
			System.out.println("sender added: " + senderName);
			
			// should be final, but can't because of the nounce tests 
			String nounce = (String)smc.get("wsNounce");
			if("true".equals((String)smc.get("dupNounce"))) {
				nounce = DUP_NOUNCE;
			}
			addHeaderElement(soapEnvelope, "nounce", nounce);
// 			System.out.println("nounce added: " + nounce);
			
			// should be final, but can't because of the signature tests 
			String signature = getSignedDigest(senderName + nounce + getSOAPBodyAsString(smc));
			
			if("true".equals((String)smc.get("forgeSignature"))) {
				signature = corruptSignature(signature);
			}
			addHeaderElement(soapEnvelope, "signature", signature);
// 			System.out.println("signature added: " + signature);
			System.out.println("");
			
		} catch (SOAPException e) {
			System.out.printf("Failed to add SOAP header because of %s%n", e);
		} catch (GeneralSecurityException e) {
			System.out.println("Failed to create a header element: " + e.getMessage());
			return false;
		} catch (Exception e) {
			// FIXME: don't catch everything
			System.out.printf("\nException in handler: %s%n", e);
			e.printStackTrace();
		}
		
		return true;
	}
	
	
	public boolean handleInbound(SOAPMessageContext smc) {
		try {
			SOAPEnvelope soapEnvelope = smc.getMessage().getSOAPPart().getEnvelope();
			SOAPHeader soapHeader = soapEnvelope.getHeader();
			
			SOAPElement headerElement;
			
			// check header
			if (soapHeader == null) {
				System.out.println("Header not found.");
				return false;
			}
			
			
// 			// get ignore context flag for transporter integration tests
// 			headerElement = getHeaderElement(soapEnvelope, "wsIgnore");
// 			if(headerElement != null) {
// 				System.out.println("Ignoring context.");
// 				return false;
// 			}
// 			String ignore = headerElement.getValue();
// // 			System.out.println("SignatureHandler got (sender)\t\t" + senderName);
// 			if("true".equals(ignore)) {
// 				smc.put("DONOTSENDBACK", "true");	// to ignore context on transporter-cli tests
// 				smc.setScope("DONOTSENDBACK", Scope.APPLICATION);
// 			}
			
			
			
			
			// get sender header element
			headerElement = getHeaderElement(soapEnvelope, "sender");
			if(headerElement == null) {
				System.out.println("Sender element not found.");
				return false;
			}
			String senderName = headerElement.getValue();
			X509Certificate cert = _keyManager.getCertificate(senderName);
			try {
				_keyManager.verifyCertificate(cert);
			}catch (CertificateException | SignatureException e){
				try{
					cert = _keyManager.forceCertificateRefresh(senderName);
					_keyManager.verifyCertificate(cert);
				}catch (CertificateException | SignatureException ex){
					System.err.println("The certificate for " + senderName + " failed to pass verification! Ignoring message");
					return false;
				}
			}
			PublicKey publicKey = cert.getPublicKey();
// 			System.out.println("SignatureHandler got (sender)\t\t" + senderName);

			
			// get nounce header element
			headerElement = getHeaderElement(soapEnvelope, "nounce");
			if(headerElement == null) {
				System.out.println("Nounce element not found.");
				return false;
			}
			String nounce = headerElement.getValue();
// 			System.out.println("SignatureHandler got (nounce)\t\t" + nounce);
			smc.put("recievedNounce", senderName+nounce); // servers handle nounces
			smc.setScope("recievedNounce", Scope.APPLICATION);
// 			System.out.println("SignatureHandler got (nounce)\t\t" + (String)smc.get("recievedNounce"));

			if(!senderName.equals("UpaBroker")) {
				String path = "/NonceDump.txt";
				writeNounceToFile(path, senderName+nounce);
			}
			
			// get signature header element
			headerElement = getHeaderElement(soapEnvelope, "signature");
			if(headerElement == null) {
				System.out.println("Signature element not found.");
				return false;
			}
			String signature = headerElement.getValue();
// 			System.out.println("SignatureHandler got (signature)\t\t" + signature);	
			
			
			String str = senderName + nounce + getSOAPBodyAsString(smc);
			if(!signatureIsValid(signature, str, publicKey)) {
				/*Refresh the certificate and try again once*/
				cert = _keyManager.forceCertificateRefresh(senderName);
				try{
					_keyManager.verifyCertificate(cert);
				}catch (CertificateException | SignatureException e){
					System.err.println("The certificate for " + senderName + " failed to pass verification! Ignoring message");
					return false;
				}
				if(!signatureIsValid(signature, str, cert.getPublicKey())) {
					System.out.println("Recieved invalid signature from " + senderName);
					return false;
				}
			}
			
		} catch (SOAPException e) {
			System.out.printf("Failed to get SOAP header because of %s%n", e);
		} catch (Exception e){
			System.out.println("BUG IS HERE. " + e.toString());
			e.printStackTrace();
			System.exit(-1);
		}
		
		return true;
	}
	
	
}

//		mvn exec:java -Dws.port=8078 -Dws.backupMode=true
//		mvn clean -Dmaven.test.skip=true install
//		mvn clean -Dtest=PingIT test


// deprecated //

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



// 	private String getSecureRandom(String senderName) throws NoSuchAlgorithmException {
// 		SecureRandom nounce;
// 		final byte array[] = new byte[16];
// 		String str = "";
// 		do {
// 			nounce = SecureRandom.getInstance("SHA1PRNG");
// 			nounce.nextBytes(array);
// 			str = printHexBinary(array);
// 			
// 		} while(_keyManager.containsNounce(senderName+"sent", str));
// 		
// 		_keyManager.addNounce(senderName+"sent", str);
// 		return str;
// 	}
//
// 	if(!senderName.equals("UpaBroker")) {
// 		// everyone not broker only speaks with broker
// 		if(_keyManager.containsNounce("UpaBroker"+senderName+"recieved", nounce)) {
// 			System.out.println(senderName + " received a repeated nounce.");
// 			return false;
// 		}
// 	} else {
// 		// FIXME no idea how to distinguish transporters here
// 		// broker receives stuff from everyone
// 		if(_keyManager.containsNounce(senderName+"recieved", nounce)) {
// 			System.out.println(senderName + " received a repeated nounce.");
// 			return false;
// 		}
// 	}
	

