package pt.upa.transporter.ws;

import javax.jws.WebService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


// http://localhost:8081/transporter-ws/endpoint?wsdl

@WebService(
	endpointInterface="pt.upa.transporter.ws.TransporterPortType",
    wsdlLocation="transporter.1_0.wsdl",
    name="UpaTransporter",
    portName="TransporterPort",
    targetNamespace="http://ws.transporter.upa.pt/",
    serviceName="TransporterService"
)
public class TransporterPort implements TransporterPortType {
	private final String TRANSPORTER_COMPANY_PREFIX = "UpaTransporter";
	private final int MIN_TIME = 1000;
	private final int MAX_TIME = 5000;
	
	private int _id;
	private int _jobCounter;
	private int _jobMinTime;
	private int _jobMaxTime;
	private List<JobView> _jobs = new ArrayList<JobView>();
	
	// private timer task definition
	private class ChangeStateTask extends TimerTask {
		JobView _job;
		JobStateView _state;
		
		public ChangeStateTask(JobView job, JobStateView state) {
			_job = job;
			_state = state;
		}
		
		@Override
		public void run() {
			_job.setJobState(_state);
		}
	}
	
	public TransporterPort(int n) {
		_id = n;
		_jobCounter = 0;
		_jobMinTime = MIN_TIME;
		_jobMaxTime = MAX_TIME;
	}
	
	// public setters to use for tests involving timers
	public void setJobMinTime(int time) { _jobMinTime = time; }
	public void setJobMaxTime(int time) { _jobMaxTime = time; }
	
	// auxiliary function to get job with given id
	private JobView getJob(String id)
			throws BadJobFault_Exception {
		
		for(JobView j : _jobs)
			if(j.getJobIdentifier().equals(id))
				return j;
		
		BadJobFault faultInfo = new BadJobFault();
		faultInfo.setId(id);
		throw new BadJobFault_Exception("Invalid job identifier ", faultInfo);
	}
	
	// auxiliary function to check the tranporter's possible working locations
	private boolean verifyLocation(String location) throws BadLocationFault_Exception {
		// even, works north and center
		if(_id%2 == 0) {
			if(!Locations.north.contains(location))
				if(!Locations.center.contains(location))
					if(!Locations.south.contains(location)) {
						BadLocationFault locationFault = new BadLocationFault();
						locationFault.setLocation(location);
						throw new BadLocationFault_Exception("Unrecognised location: "+ location, locationFault);
					} else
						return false;
						
		// odd, works south and center
		} else {
			if(!Locations.south.contains(location))
				if(!Locations.center.contains(location))
					if(!Locations.north.contains(location)) {
						BadLocationFault locationFault = new BadLocationFault();
						locationFault.setLocation(location);
						throw new BadLocationFault_Exception("Unrecognised location: "+ location, locationFault);
					} else
						return false;
		}
		
		return true;
	}
	
	// auxliliary function to verify if a job state is correct
	private void verifyJobState(JobView job, JobStateView correctState)
    		throws BadJobFault_Exception {
    			
		if (job.getJobState() == correctState)
			return;
		BadJobFault faultInfo = new BadJobFault();
		faultInfo.setId(job.getJobIdentifier());
		throw new BadJobFault_Exception("Invalid job status", faultInfo);
	}
	
	// auxiliary function to pick random time
	private int pickRandomTime() {
		return _jobMinTime + (new Random().nextInt(_jobMaxTime - _jobMinTime));
	}
	
	// returns answer to ping request
	@Override
	public String ping(String name) {
		return name + " " + TRANSPORTER_COMPANY_PREFIX + _id;
	}
	
	
	// returns an offer for the given job
	@Override
    public JobView requestJob(String origin, String destination, int price)
 			throws BadLocationFault_Exception, BadPriceFault_Exception {
		
		// check for recognised location and working regions
		if(verifyLocation(origin) == false || verifyLocation(destination) == false)
			return null;
		
		// negative prices are not allowed
		if(price < 0) {
			BadPriceFault priceFault = new BadPriceFault();
			priceFault.setPrice(price);
 			throw new BadPriceFault_Exception("Invalid price: "+ price, priceFault);
		}
		
		// has to return null for prices over 100
		if(price > 100) {
			return null;
		}

		JobView job = new JobView();
		
		job.setCompanyName(TRANSPORTER_COMPANY_PREFIX+_id);
		job.setJobIdentifier(Integer.toString(_jobCounter++));
		job.setJobOrigin(origin);
		job.setJobDestination(destination);
		job.setJobState(JobStateView.PROPOSED);
		
		// has to return a better deal for prices lower or equal than 10
		if(price <= 10) {
			job.setJobPrice(1+(new Random()).nextInt(price-1));
		}
		
		// if price and id are both even or both odd return a lower price
		else if((_id + price)%2 == 0) {
			job.setJobPrice(1+(new Random()).nextInt(price-2));
		
		// if not, return an higher price
		} else {
			job.setJobPrice(price+1 + (new Random()).nextInt(price));
		}

		_jobs.add(job);
		return job;
	}
	
	// accepts or rejects job with given id
	@Override
    public JobView decideJob(String id, boolean accept)
    		throws BadJobFault_Exception {
    		
		// find job with given id (throws exception on fail)
		JobView job = getJob(id);
		
		// verify if job state is correct (throws exception if wrong)
		verifyJobState(job, JobStateView.PROPOSED);
		
		// change job state to accepted or rejected
		if (accept) {
			job.setJobState(JobStateView.ACCEPTED);
			
			// set job timers
			int time = 0;
			Timer timer = new Timer();
			timer.schedule(new ChangeStateTask(job, JobStateView.HEADING), time += pickRandomTime());
			timer.schedule(new ChangeStateTask(job, JobStateView.ONGOING), time += pickRandomTime());
			timer.schedule(new ChangeStateTask(job, JobStateView.COMPLETED), time += pickRandomTime());
			
		} else {
			job.setJobState(JobStateView.REJECTED);
		}
		return job;
    }
	
	// returns job current state
	@Override
	public JobView jobStatus(String id) {
		try {
			return getJob(id);
		} catch (BadJobFault_Exception e) {
			return null; // if no job is found
		}
	}
	
	// returns the list of current jobs
	@Override
	public List<JobView> listJobs() {
		return _jobs;
	}
	
	// clears the list of current jobs
	@Override
	public void clearJobs() {
		_jobs.clear();
		_jobCounter = 0;
	}
}
