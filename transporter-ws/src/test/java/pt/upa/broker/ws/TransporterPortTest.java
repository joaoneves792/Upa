package pt.upa.transporter.ws;

import org.junit.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import javax.xml.registry.JAXRException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.TimerTask;
import java.util.Timer;
import java.util.List;

public class TransporterPortTest {
	private final String INVALID_ID = "-1";
	
	private final String VALID_ORIGIN = "Lisboa";
    private final String VALID_DESTINATION = "Leiria";
    private final String INVALID_ORIGIN = "Alameda";
    private final String INVALID_DESTINATION = "Porto Salvo";
    private final String NORTH_LOCATION = "Porto";
    private final String SOUTH_LOCATION = "Setubal";

	private final int INVALID_PRICE = -1;
	private final int VALID_PRICE = 50;
    private final int EVEN_PRICE = 50;
    private final int ODD_PRICE = 51;
    private final int OVERPRICED_PRICE = 101;
    private final int UNDERPRICED_PRICE = 9;
	
    private TransporterPort _transporter;
    public boolean _error;
    
    private class CheckStateTask extends TimerTask {
		JobView _job;
		JobStateView _state;
		TransporterPortTest _test;
		
		public CheckStateTask(JobView job, JobStateView state, TransporterPortTest test) {
			_job = job;
			_state = state;
			_test = test;
		}
		
		@Override
		public void run() {
			if(_job.getJobState() != _state)
				_test._error = true;
		}
	}

    @Before
    public void setUp() {
    	_error = false;
   		_transporter = new TransporterPort(1); //UpaTransporter1
    }

    @After
    public void tearDown() {
    	// empty for now
    }

	/*
	requestJob test cases
	*/

    @Test(expected = BadLocationFault_Exception.class)
	public void requestJobUnknownOrigin() throws Exception {
		_transporter.requestJob(INVALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
	}

    @Test(expected = BadLocationFault_Exception.class)
	public void requestJobUnknownDestination() throws Exception {
		_transporter.requestJob(VALID_ORIGIN, INVALID_DESTINATION, VALID_PRICE);
	}

    @Test(expected = BadPriceFault_Exception.class)
	public void requestJobInvalidPrive() throws Exception {
		_transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, INVALID_PRICE);
	}
	
	@Test
	public void requestJobOddTransporterInvalidZone() throws Exception {
		TransporterPort oddTransporter = new TransporterPort(3);
		assertNull("Should return null for invalid origins.",
				oddTransporter.requestJob(NORTH_LOCATION, VALID_DESTINATION, VALID_PRICE));
		assertNull("Should return null for invalid destinations.",
				oddTransporter.requestJob(VALID_ORIGIN, NORTH_LOCATION, VALID_PRICE));
	}

	@Test
	public void requestJobEvenTransporterInvalidZone() throws Exception {
		TransporterPort evenTransporter = new TransporterPort(2);
		assertNull("Should return null for invalid origins.",
				evenTransporter.requestJob(SOUTH_LOCATION, VALID_DESTINATION, VALID_PRICE));
		assertNull("Should return null for invalid destinations.",
				evenTransporter.requestJob(VALID_ORIGIN, SOUTH_LOCATION, VALID_PRICE));
	}
	
	@Test
	public void requestJobOverPricedOffer() throws Exception {
		assertNull("Should return null.",
				_transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, OVERPRICED_PRICE));
	}
	
	@Test
	public void requestJobUnderPricedOffer() throws Exception {
		int price = _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, UNDERPRICED_PRICE).getJobPrice();
		assertTrue("Should return a price lower than original.", price < UNDERPRICED_PRICE);
		assertTrue("Job prices can't be negative.", price >= 0);
	}
	
	@Test
	public void requestJobOddTransporterOddPrice() throws Exception {
		int price = new TransporterPort(3).requestJob(VALID_ORIGIN, VALID_DESTINATION, ODD_PRICE).getJobPrice();
		assertTrue("Should return a price lower than original.", price < ODD_PRICE);
		assertTrue("Job prices can't be negative.", price >= 0);
	}
	
	@Test
	public void requestJobOddTransporterEvenPrice() throws Exception {
		int price = new TransporterPort(3).requestJob(VALID_ORIGIN, VALID_DESTINATION, EVEN_PRICE).getJobPrice();
		assertTrue("Should return a price higher than original.", price > EVEN_PRICE);
	}
	
	@Test
	public void requestJobEvenTransporterOddPrice() throws Exception {
		int price = new TransporterPort(2).requestJob(VALID_ORIGIN, VALID_DESTINATION, ODD_PRICE).getJobPrice();
		assertTrue("Should return a price higher than original.", price > ODD_PRICE);
	}
	
	@Test
	public void requestJobEvenTransporterEvenPrice() throws Exception {
		int price = new TransporterPort(2).requestJob(VALID_ORIGIN, VALID_DESTINATION, EVEN_PRICE).getJobPrice();
		assertTrue("Should return a price lower than original.", price < EVEN_PRICE);
		assertTrue("Job prices can't be negative.", price >= 0);
	}
	
	@Test
	public void requestJobEvenTransporterSucess() throws Exception {
		TransporterPort evenTransporter = new TransporterPort(2);
		assertNotNull("Shouldn't return null for valid origins",
				evenTransporter.requestJob(NORTH_LOCATION, VALID_DESTINATION, VALID_PRICE));
		assertNotNull("Shouldn't return null for valid destinations.",
				evenTransporter.requestJob(VALID_ORIGIN, NORTH_LOCATION, VALID_PRICE));
	}

	@Test
	public void requestJobOddTransporterSucess() throws Exception {
		TransporterPort oddTransporter = new TransporterPort(3);
		assertNotNull("Shouldn't return null for valid origins",
				oddTransporter.requestJob(SOUTH_LOCATION, VALID_DESTINATION, VALID_PRICE));
		assertNotNull("Shouldn't return null for valid destinations.",
				oddTransporter.requestJob(VALID_ORIGIN, SOUTH_LOCATION, VALID_PRICE));
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
    	_transporter.setJobMinTime(100);
        _transporter.setJobMaxTime(101);

        _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        JobView job = _transporter.decideJob("0", false);
		assertEquals("Returned job state is not REJECTED.", JobStateView.REJECTED, job.getJobState());
		
		Thread.sleep(400);
		
		assertEquals("Timers shouldn't change a rejected job state", JobStateView.REJECTED, job.getJobState());
	}
	
	@Test
    public void decideJobAcceptSuccess() throws Exception {
    	_transporter.setJobMinTime(100);
        _transporter.setJobMaxTime(101);
        
        _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        JobView job = _transporter.decideJob("0", true);
		assertEquals("Returned job state is not ACCEPTED", JobStateView.ACCEPTED, job.getJobState());
				
    	Timer timer = new Timer();
		timer.schedule(new CheckStateTask(job, JobStateView.HEADING, this), 150);
		timer.schedule(new CheckStateTask(job, JobStateView.ONGOING, this), 250);
		timer.schedule(new CheckStateTask(job, JobStateView.COMPLETED, this), 350);
		Thread.sleep(400);

		assertFalse("Timer based change of states ain't working correctly.", _error);
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
		assertNull("Should return null for invalid ids", _transporter.jobStatus(INVALID_ID));
	}
	
	@Test
	public void jobStatusSuccess() throws Exception {
		JobView expectedJob = _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		assertSame("Returned jobView is not correct.", _transporter.jobStatus("0"), expectedJob);
	}


	/*
	listJobs test cases
	*/
	
	@Test
	public void listJobsSuccess() throws Exception {
		assertEquals("Job list size is not correct.", 0, _transporter.listJobs().size());
		_transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		assertEquals("Job list size is not correct.", 1, _transporter.listJobs().size());
		
		JobView job = _transporter.listJobs().get(0);
		assertEquals("Job list element is not correct.", "UpaTransporter1", job.getCompanyName());
		assertEquals("Job list element is not correct.", VALID_ORIGIN, job.getJobOrigin());
		assertEquals("Job list element is not correct.", VALID_DESTINATION, job.getJobDestination());
		assertEquals("Job list element is not correct.", JobStateView.PROPOSED, job.getJobState());
				
		_transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		assertEquals("Job list size is not correct.", 2, _transporter.listJobs().size());
	}
	
	/*
	clearJobs test cases
	*/
	
	@Test
	public void clearJobsSuccess() throws Exception {
		_transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		_transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		_transporter.clearJobs();
		assertEquals("Job list should be empty.", 0, _transporter.listJobs().size());
	}
	
}