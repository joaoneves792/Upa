package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertNotNull;

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
		TransporterClient tc = new TransporterClient("http://localhost:9090", "UpaTransporter1", true, false);
		TransporterPortType port = tc.getPort();
		assertNotNull(port.ping("test"));
	}
	
		@Test(expected = XMLStreamReaderException.class)
	public void duplicateNouncePingEmptyTest() throws Exception {
		TransporterClient tc = new TransporterClient("http://localhost:9090", "UpaTransporter1", false, true);
		TransporterPortType port = tc.getPort();
		assertNotNull(port.ping("test"));
		assertNotNull(port.ping("test"));
	}

}
