package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test suite - simple test of clearing a transporter's jobs.
 */
public class ClearJobIT extends AbstractIT {

	/**
	 * Delete all jobs in a transporter. There shouldn't be any errors.
	 * 
	 * @result PORT.listJobs() will return 0 after invoking
	 *         PORT.clearJobs().
	 * @throws Exception
	 */
	@Test
	public void testClearJob() throws Exception {
		CLIENT.getPort().clearJobs();
		assertEquals(0, CLIENT.getPort().listJobs().size());
	}

}
