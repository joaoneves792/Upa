package pt.upa.transporter.ws;

import org.junit.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import javax.xml.registry.JAXRException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNull;

import java.util.TimerTask;
import java.util.Timer;

public class TransporterPortTest {
	
	// Temporary work around
	private class TimerError {
		private boolean _error;
		public boolean getError() { return _error; }
		public void setError(boolean error) { _error = error; }
	}
	
	private class CheckStateTask extends TimerTask {
		JobView _job;
		JobStateView _state;
		TimerError _error;
		
		public CheckStateTask(JobView job, JobStateView state, TimerError error) {
			_job = job;
			_state = state;
			_error = error;
		}
		
		@Override
		public void run() {
			if(_state != _job.getJobState())
				_error.setError(true);
				
			/*
			try {
				System.out.println("CHECK");
				assertEquals("Job state didn't change after given time", _state, _job.getJobState());
			} catch (Exception e) {
				_error.setError(true);
			}*/
		}
	}
	
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
		assertEquals("Returned job state is not REJECTED.",
				JobStateView.REJECTED, _transporter.decideJob("0", false).getJobState());
	}
	
	@Test //THE WAY TIMERS ARE BEING TESTED AINT THE BEST
    public void decideJobAcceptSuccess() throws Exception {
    	TimerError error = new TimerError();
    	Timer timer = new Timer();
   		_transporter.setJobMinTime(100);
        _transporter.setJobMaxTime(101);
        _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        JobView job = _transporter.decideJob("0", true);
		assertEquals("Returned job state is not ACCEPTED",
				JobStateView.ACCEPTED, job.getJobState());
		timer.schedule(new CheckStateTask(job, JobStateView.HEADING, error), 150);
		timer.schedule(new CheckStateTask(job, JobStateView.ONGOING, error), 250);
		timer.schedule(new CheckStateTask(job, JobStateView.COMPLETED, error), 350);
		Thread.sleep(400);
		assertFalse("Timer based change of states not working correctly.", error.getError());
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
		assertNull("Should return null.", _transporter.jobStatus(INVALID_ID));
	}
	
	@Test
	public void jobStatusSuccess() throws Exception {
		JobView expectedJob = _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		assertSame("Returned jobView is not correct.", _transporter.jobStatus("0"), expectedJob);

	}
	
}