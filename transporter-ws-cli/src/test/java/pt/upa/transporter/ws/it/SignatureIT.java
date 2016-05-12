package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import pt.upa.transporter.ws.cli.TransporterClient;
import pt.upa.transporter.ws.TransporterPortType;

import com.sun.xml.ws.streaming.XMLStreamReaderException;

/**
 * simple signature tampering test
 */
public class SignatureIT extends AbstractIT {

	@Test(expected = XMLStreamReaderException.class)
	public void signatureTamperingPingEmptyTest() throws Exception {
		TransporterClient tc = new TransporterClient("http://localhost:9090", "UpaTransporter1",true, true, false);
		assertNotNull(tc.getPort().ping("signature"));
	}
	
	@Test
	public void duplicateNouncePingEmptyTest() throws Exception {
		TransporterClient tc = new TransporterClient("http://localhost:9090", "UpaTransporter1",true, false, true);
		assertNotNull(tc.getPort().ping("nonce"));
		assertNull(tc.getPort().ping("duplicate_nonce"));
	}
	
}
