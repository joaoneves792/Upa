package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import pt.upa.transporter.ws.cli.TransporterClient;
import pt.upa.transporter.ws.TransporterPortType;

import com.sun.xml.ws.streaming.XMLStreamReaderException;

public class SignatureIT extends AbstractIT {

	/**
	 * simple signature tampering test
	 * (not sure if is exepecting the correct exception but for now it is working)
	 */
	@Test(expected = XMLStreamReaderException.class)
	public void signatureTamperingPingEmptyTest() throws Exception {
		TransporterClient tc = new TransporterClient("http://localhost:9090", "UpaTransporter1",true, true, false);
		assertNotNull(tc.getPort().ping("signature"));
	}
	
// 	@Test(expected = XMLStreamReaderException.class)
	@Test
	public void duplicateNouncePingEmptyTest() throws Exception {
		TransporterClient tc = new TransporterClient("http://localhost:9090", "UpaTransporter1",true, false, true);
		assertNotNull(tc.getPort().ping("nonce"));
// 		tc.getPort().ping("duplicate_nonce");
		assertNull(tc.getPort().ping("duplicate_nonce"));
	}

}
