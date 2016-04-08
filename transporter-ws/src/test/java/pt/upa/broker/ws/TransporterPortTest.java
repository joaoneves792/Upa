package pt.upa.transporter.ws;

import org.junit.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import javax.xml.registry.JAXRException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNull;

public class TransporterPortTest {
	private final String TRANSPORTER_COMPANY_PREFIX = "UpaTransporter";
	private final String INVALID_ID = "-1";
	private final String VALID_ORIGIN = "Lisboa";
    private final String VALID_DESTINATION = "Leiria";

    private final int VALID_PRICE = 50;
	
    private TransporterPort _transporter;
    

    @Before
    public void setUp() {
   		_transporter = new TransporterPort(1); //UpaTransporter1
    }

    @After
    public void tearDown() {
    	// empty for now
    }

	/*
	decideJob test cases
	*/

    @Test(expected = BadJobFault_Exception.class)
	public void decideJobEmptyList() throws Exception {
		_transporter.decideJob("0", true);
	}

    @Test(expected = BadJobFault_Exception.class)
	public void decideJobInvalidId() throws Exception {
        _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		_transporter.decideJob(INVALID_ID, true);
	}

    @Test(expected = BadJobFault_Exception.class)
    public void decideJobInvalidState() throws Exception {
        JobView job = _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		job.setJobState(JobStateView.ACCEPTED); //Points to the same object as the one on the list
		_transporter.decideJob("0", true);
	}

    @Test
    public void decideJobRejectSuccess() throws Exception {
        _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		assertEquals("Returned job state is not REJECTED",
				JobStateView.REJECTED, _transporter.decideJob("0", false).getJobState());
	}
	
	@Test
    public void decideJobAcceptSuccess() throws Exception {
   		_transporter.setJobMinTime(100);
        _transporter.setJobMaxTime(130);
        _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        JobView job = _transporter.decideJob("0", true);
		assertEquals("Returned job state is not ACCEPTED",
				JobStateView.ACCEPTED, job.getJobState());
		Thread.sleep(135);
		assertEquals("Job state didn't change to HEADING after given time",
				JobStateView.HEADING, job.getJobState());
		Thread.sleep(265);
		assertEquals("Job state didn't change to ONGOING after given time",
				JobStateView.ONGOING, job.getJobState());
		Thread.sleep(395);
		assertEquals("Job state didn't change to COMPLETED after given time",
				JobStateView.COMPLETED, job.getJobState());
	}
	

	/*
	jobStatus test cases
	*/

    @Test
	public void jobStatusEmptyList() throws Exception {
		_transporter.jobStatus("0");
	}

    @Test
	public void jobStatusInvalidId() throws Exception {
        _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		assertNull("Should return null", _transporter.jobStatus(INVALID_ID));
	}
	
	@Test
	public void jobStatusSuccess() throws Exception {
		JobView expectedJob = _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		assertSame("Returned jobView is not correct", _transporter.jobStatus("0"), expectedJob);

	}
	
}