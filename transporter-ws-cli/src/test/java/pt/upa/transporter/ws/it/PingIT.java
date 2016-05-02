package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Test suite
 */
public class PingIT extends AbstractIT {

	/**
	 * Receive a non-null reply from the transporter that was pinged through
	 * PORT.
	 */
	@Test
	public void pingEmptyTest() {
		assertNotNull(PORT.ping("test"));
	}

}
