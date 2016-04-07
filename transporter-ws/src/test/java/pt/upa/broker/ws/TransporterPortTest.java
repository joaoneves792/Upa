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
   		_transporter = new TransporterPort(1); //random id
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
    public void decideJobSucessOnReject() throws Exception {
        _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		assertEquals("Returned job state is not REJECTED",
				_transporter.decideJob("0", false).getJobState(), JobStateView.REJECTED);
	}
	
	@Test
    public void decideJobSucessOnAccept() throws Exception {
        _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		assertEquals("Returned job state is not ACCEPTED",
				_transporter.decideJob("0", true).getJobState(), JobStateView.ACCEPTED);
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
	public void jobStatusSucess() throws Exception {
		JobView expectedJob = _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		assertSame("Returned jobView is not correct", _transporter.jobStatus("0"), expectedJob);

	}
	
}