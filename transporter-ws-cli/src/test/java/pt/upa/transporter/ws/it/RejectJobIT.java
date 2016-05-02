package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;


/**
 * Test suite
 */
public class RejectJobIT extends AbstractIT {

	/**
	 * Create a job (with valid arguments), decide on it (reject) and check that
	 * its state changed to JobStateView.REJECTED.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRejectJob() throws Exception {
		JobView jv = PORT.requestJob(CENTRO_1, SUL_1, PRICE_SMALLEST_LIMIT);
		jv = PORT.decideJob(jv.getJobIdentifier(), false);
		assertEquals(JobStateView.REJECTED, jv.getJobState());
	}

	/**
	 * Create a job (with valid arguments) and attempt to decide (reject) on it
	 * twice.
	 * 
	 * @result Should throw BadJobFault_Exception because it does not make sense
	 *         to decide on an already decided job.
	 * @throws Exception
	 */
	@Test(expected = BadJobFault_Exception.class)
	public void testRejectDuplicateJob() throws Exception {
		JobView jv = PORT.requestJob(CENTRO_2, SUL_2, PRICE_SMALLEST_LIMIT);
		PORT.decideJob(jv.getJobIdentifier(), false);
		PORT.decideJob(jv.getJobIdentifier(), false);
	}

	/**
	 * Invoke PORT.decideJob on an invalid (empty string) job identifier.
	 * 
	 * @result Should throw BadJobFault_Exception as the job is invalid.
	 * @throws Exception
	 */
	@Test(expected = BadJobFault_Exception.class)
	public void testRejectInvalidJob() throws Exception {
		PORT.decideJob(EMPTY_STRING, false);
	}

	/**
	 * Invoke PORT.decideJob on an invalid (null) job identifier.
	 * 
	 * @result Should throw BadJobFault_Exception as the job is invalid.
	 * @throws Exception
	 */
	@Test(expected = BadJobFault_Exception.class)
	public void testRejectNullJob() throws Exception {
		PORT.decideJob(null, false);
	}

}
