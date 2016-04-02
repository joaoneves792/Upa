package pt.upa.transporter.ws;

import javax.jws.WebService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


// http://localhost:8081/transporter-ws/endpoint?wsdl

@WebService(
	endpointInterface="pt.upa.transporter.ws.TransporterPortType",
    wsdlLocation="transporter.1_0.wsdl",
    name="UpaTransporter1",
    portName="TransporterPort",
    targetNamespace="http://ws.transporter.upa.pt/",
    serviceName="TransporterService"
)
public class TransporterPort implements TransporterPortType {
	
	int _id;
	int _jobCouter;
	List<JobView> _jobs = new ArrayList<JobView>();

	
	// so that the tranporter knows whether it is even or odd. needing fix for name in @WebService.
	public TransporterPort(int n) { _id = n; }	
	
	// auxiliary function to create a unic job identifier
	private int generateJobId() {
		_jobCouter++;
		return _jobCouter;
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
						throw new BadLocationFault_Exception("unrecognised location: "+ location, locationFault);
					} else
						return false;
						
		// odd, works south and center
		} else {
			if(!Locations.south.contains(location))
				if(!Locations.center.contains(location))
					if(!Locations.north.contains(location)) {
						BadLocationFault locationFault = new BadLocationFault();
						locationFault.setLocation(location);
						throw new BadLocationFault_Exception("unrecognised location: "+ location, locationFault);
					} else
						return false;
		}
		
		return true;
	}
	

	@Override
	public String ping(String name) {
		return name + " UpaTransporter" + _id;
	}
	
	
	// section 5.1. requirements in comments
	@Override
    public JobView requestJob(String origin, String destination, int price)
 			throws BadLocationFault_Exception, BadPriceFault_Exception {		
		
		// check for recognised location and working regions
		if(verifyLocation(origin) == false || verifyLocation(destination) == false)
			return null;
		
		// negatrive prices are not allowed
		if(price < 0) {
			BadPriceFault priceFault = new BadPriceFault();
			priceFault.setPrice(price);
			
 			throw new BadPriceFault_Exception("invalid price: "+ price, priceFault);
		}
		
		// has to return null for prices over 100
		if(price > 100) {
			return null;	
		}

		JobView job = new JobView();
		
		job.setCompanyName("UpaTransporter"+_id);
		job.setJobIdentifier(Integer.toString(generateJobId()));
		job.setJobOrigin(origin);
		job.setJobDestination(destination);
		job.setJobState(JobStateView.PROPOSED);
		
		// has to return a better deal for prices under or equal than 10
		if(price <= 10) {
			job.setJobPrice((new Random()).nextInt(price));
		}
		
		// if price and id are both even or both odd return a better price
		else if((_id + price)%2 == 0) {
			job.setJobPrice((new Random()).nextInt(price-1));
		
		// if not, return an higher price
		} else {
			job.setJobPrice(price+1 + (new Random()).nextInt(price));
		}

		_jobs.add(job);
		return job;
	}
	
	@Override
    public JobView decideJob(String id,boolean accept)
            throws BadJobFault_Exception {
		
    	return new JobView();

    }
	
	@Override
	public JobView jobStatus(String id) {
		return new JobView();
	}
	
	@Override
	public List<JobView> listJobs() {		 
		return _jobs;
	}
	
	@Override
	public void clearJobs() {
		_jobs.clear();
	}
}
