package pt.upa.ws.handler;

import java.util.Set;
import java.io.ByteArrayOutputStream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

	public Set<QName> getHeaders() {
		return null;
	}
	
	public boolean handleMessage(SOAPMessageContext smc) {
		printSOAP(smc);
		return true;
	}
	
	public boolean handleFault(SOAPMessageContext smc) {
		printSOAP(smc);
		return true;
	}
	
	// nothing to clean up
	public void close(MessageContext messageContext) { }

	
    // print sopa to terminal with proper formating
	public void printSOAP(SOAPMessageContext smc) {
	
        if ((Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))
            System.out.println("\nOutbound SOAP message:");
        else
            System.out.println("\nInbound SOAP message:");
	
        SOAPMessage message = smc.getMessage();
		try {
			TransformerFactory tff = TransformerFactory.newInstance();
			Transformer tf = tff.newTransformer();
			
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			
			Source sc = message.getSOAPPart().getContent();
			
			ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(streamOut);
			tf.transform(sc, result);
			
			String str = streamOut.toString();
			System.out.println(str);
		} catch (Exception e) {
			System.out.printf("\nException in handler: %s%n", e);
		}
	}

    
    /**
     * Check the MESSAGE_OUTBOUND_PROPERTY in the context to see if this is an
     * outgoing or incoming message. Write a brief message to the print stream
     * and output the message. The writeTo() method can throw SOAPException or
     * IOException
     */
    private void logToSystemOut(SOAPMessageContext smc) {
        Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
        if (outbound) {
            System.out.println("\nOutbound SOAP message:");
        } else {
            System.out.println("\nInbound SOAP message:");
        }
		
        SOAPMessage message = smc.getMessage();
        try {
            message.writeTo(System.out);
            System.out.println(); // just to add a newline to output
        } catch (Exception e) {
            System.out.printf("\nException in handler: %s%n", e);
        }
    }

}
