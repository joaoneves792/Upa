package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random; // DELETE ME
import java.util.Timer;
import java.util.TimerTask;

import javax.jws.WebService;
import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.TransporterClient;
import pt.upa.transporter.ws.cli.TransporterClientException;
import pt.upa.broker.ws.cli.BrokerClient;
import pt.upa.broker.ws.cli.BrokerClientException;

@WebService(
	endpointInterface="pt.upa.broker.ws.BrokerPortType",
    wsdlLocation="broker.1_0.wsdl",
    name="UpaBroker",
    portName="BrokerPort",
    targetNamespace="http://ws.broker.upa.pt/",
    serviceName="BrokerService"
)
public class BrokerPort implements BrokerPortType {
	private final String TRANSPORTER_COMPANY_PREFIX = "UpaTransporter";
	private final int SIGNAL_TIME = 5000;
	
	private List<TransportView> _transportList = new ArrayList<>();
	private String _uddiLocation;

	private BrokerPortType _backupServer;
	private Timer _timer;
	private boolean _backupMode;
	
	@Resource
	private WebServiceContext webServiceContext;

	
	public BrokerPort(String uddiLocation) {
		_uddiLocation = uddiLocation;
	}

	public BrokerPort(String uddiLocation, boolean backupMode) {
		_uddiLocation = uddiLocation;
		_backupMode = backupMode;
		
		if (_backupMode) {
			System.out.println("Running on backup mode.");
		} else {
			try{
				_backupServer = new BrokerClient(_uddiLocation, "UpaBrokerBackup").getPort();
				propagateState("Hi.");
				_timer = new Timer();
				_timer.schedule(new sendSignalTask(), SIGNAL_TIME, SIGNAL_TIME);
				System.out.println("Backup Server found!");
			} catch (BrokerClientException e) {
				System.out.println("Backup Server not found!");
			}
		}
	}
	
// 	private void setContextForHandler(){
// 		if(null != webServiceContext){ // If null, then we are running the unit tests
// 			webServiceContext.getMessageContext().put("wsName", "UpaBroker");
// 			webServiceContext.getMessageContext().put("uddiURL", _uddiLocation);
// 		}
// 	}
	
	
	// backup related methods
	
	private class sendSignalTask extends TimerTask {
		@Override
		public void run() {
			propagateState(randomSignal()); // FIXME
		}
	}
	
	private class declareServerDeadTask extends TimerTask {
		@Override
		public void run() {
			try{
				System.out.println("It looks like that guy finally shut up. Time to take over!");
				
				UDDINaming uddiNaming = new UDDINaming(_uddiLocation);
				String url = uddiNaming.lookup("UpaBrokerBackup");
				
				uddiNaming.unbind("UpaBrokerBackup");
				System.out.println("Deleted 'UpaBrokerBackup' from UDDI");
				
				uddiNaming.rebind("UpaBroker", url);
				System.out.println("Added 'UpaBroker' to UDDI");
 			
			} catch (Exception e) {
				System.out.printf("Caught exception when taking over as main server: %s%n", e);
			}
		}
	}
	
	public void stopTimer(){
		if (_timer != null)
			_timer.cancel();
	}
	
	// DELETE ME
	private String randomSignal(){
		String [] msgs = {
			"I'm still here.",
			"Missed me?",
			"What time is it?",
			"So a guy walks into a bar...",
			"You won't get rid of me so easily.",
			"You don't talk to much, do you?",
			"Did you know that more than 60% of the human body is made of water?",
			"Shhhh... did you hear that?",
			"Can you guess what color I'm thinking of?",
			"Oh you want to be the main server huh? Well get in line!"
		};
		
		return msgs[new Random().nextInt(msgs.length)];
	};
	
	private void propagateState(String msg){
		try {
			if (_backupServer != null)
				_backupServer.updateState(msg);
		} catch (Exception e) {
			System.out.println("Backup Server lost.");
			_backupServer = null;
		}
	};
	
	public void updateState(String msg){
		stopTimer();
		
		System.out.println("Server says: " + msg);
		
		_timer = new Timer();
		_timer.schedule(new declareServerDeadTask(), SIGNAL_TIME*2);
	};
	


	// auxiliary function to get transport with given id
	private TransportView getTransport(String id)
			throws UnknownTransportFault_Exception {
				
		for (int i = 0; i < _transportList.size(); i++)
	    	if (_transportList.get(i).getId().equals(id))
				return _transportList.get(i);
		
		UnknownTransportFault faultInfo = new UnknownTransportFault();
		faultInfo.setId(id);
		throw new UnknownTransportFault_Exception("Invalid transport id", faultInfo);
	}
	
	// auxiliary function to convert job states to transport states
	private TransportStateView convertState(JobStateView state) {
		if (state == JobStateView.PROPOSED)
			return TransportStateView.BUDGETED;
		if (state == JobStateView.ACCEPTED)
			return TransportStateView.BOOKED;
		if (state == JobStateView.REJECTED)
			return TransportStateView.FAILED;
		if (state == JobStateView.HEADING)
			return TransportStateView.HEADING;
		if (state == JobStateView.ONGOING)
			return TransportStateView.ONGOING;
		if (state == JobStateView.COMPLETED)
			return TransportStateView.COMPLETED;
		return null;
	}
	
	// auxiliary function to convert transporter id to broker id
	private String jobIdToTransportId(JobView job) {
		return job.getCompanyName() + "_" + job.getJobIdentifier();
	}

	// auxiliary function to convert broker id to transport id
	private String transportIdToJobId(TransportView transport) {
		return transport.getId().split("_")[1];
	}
	
	// auxiliary function to check if a location is valid
	private void verifyLocation(String location) throws UnknownLocationFault_Exception {
		if(Locations.north.contains(location) || Locations.center.contains(location) || Locations.south.contains(location))
			return;
		UnknownLocationFault locationFault = new UnknownLocationFault();
		locationFault.setLocation(location);
		throw new UnknownLocationFault_Exception("Unrecognised location: " + location, locationFault);
	}

	// auxiliary function to create a transport from a job
	private TransportView createBudgetedTransport(JobView chosenJob) {
		TransportView budgetedTransport = new TransportView();
		budgetedTransport.setOrigin(chosenJob.getJobOrigin());
		budgetedTransport.setDestination(chosenJob.getJobDestination());
		budgetedTransport.setPrice(chosenJob.getJobPrice());
		budgetedTransport.setTransporterCompany(chosenJob.getCompanyName());
		budgetedTransport.setState(TransportStateView.BUDGETED);
		budgetedTransport.setId(jobIdToTransportId(chosenJob));
		return budgetedTransport;
	}
	
	// ping transporters
	@Override
    public String ping(String name) {
		String result = name + " UpaBroker";
		try {
			UDDINaming uddi = new UDDINaming(_uddiLocation);
			Collection<String> transporters = uddi.list(TRANSPORTER_COMPANY_PREFIX + "_");
			
			for (String transporter : transporters) {
				try{
					TransporterClient client = new TransporterClient(transporter);
					result = client.port.ping(result);
				} catch (TransporterClientException e) {
					result += " " + transporter + " Failed ";
				}
			}
		} catch(JAXRException e){
			result += " UDDI Failed ";
		}
		return result;
    }
	
	// tries to schedule transport from given origin to given destination
	@Override
    public String requestTransport(String origin, String destination, int price)
            throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    	
//     	setContextForHandler();
    	
		if(price < 0){
			InvalidPriceFault fault = new InvalidPriceFault();
			fault.setPrice(price);
			throw new InvalidPriceFault_Exception("Negative price", fault);
		}
		verifyLocation(origin);
		verifyLocation(destination);

		// contact all transporter companies and get their proposals
		List<JobView> availableJobs;
		availableJobs = getJobProposals(origin, destination, price);

		if(availableJobs.isEmpty()){
			UnavailableTransportFault fault = new UnavailableTransportFault();
			fault.setOrigin(origin);
			fault.setDestination(destination);
			throw new UnavailableTransportFault_Exception("No transports available between" + origin + "and" + destination, fault);
		}

		// pick the lowest priced job
		int lowestPrice = Integer.MAX_VALUE;
		JobView choosenJob = null;
		for (JobView job: availableJobs) {
			if(job.getJobPrice() < lowestPrice){
				lowestPrice = job.getJobPrice();
				choosenJob = job;
			}
		}

		//Ditch all the other proposals
		for(JobView j : availableJobs){
			if(j.getCompanyName().equals(choosenJob.getCompanyName()) && j.getJobIdentifier().equals(choosenJob.getJobIdentifier()))
				continue;
			try {
				TransporterClient client = new TransporterClient(_uddiLocation, j.getCompanyName());
				client.getPort().decideJob(j.getJobIdentifier(), false);
			} catch (TransporterClientException | BadJobFault_Exception e) {
				//Not our fault if we cant contact them or if the id doesnt match them, just skip to the next one
			}
		}

		if(choosenJob.getJobPrice() > price){
			UnavailableTransportPriceFault fault = new UnavailableTransportPriceFault();
			fault.setBestPriceFound(choosenJob.getJobPrice());
			throw new UnavailableTransportPriceFault_Exception("No transport found for your maximum price, lowest available is: " + choosenJob.getJobPrice(), fault);
		}

		TransportView transport = createBudgetedTransport(choosenJob);
		_transportList.add(transport);
		propagateState("I just got a new job. Hold on to it, would'ya?");

		bookJob(transport);

		return transport.getId();
    }

	// returns a list of the jobs proposed by the transporters
	private List<JobView> getJobProposals(String origin, String destination, int price) throws UnavailableTransportFault_Exception {
		List<JobView> availableJobs = new ArrayList<>();
		
//     	setContextForHandler();
		
		try{
			UDDINaming uddi = new UDDINaming(_uddiLocation);
			Collection<String> transporters = uddi.list(TRANSPORTER_COMPANY_PREFIX + "_");

			for(String transporter : transporters) {
				try {
					TransporterClient client = new TransporterClient(transporter);

					JobView job = client.getPort().requestJob(origin, destination, price);
					if (null != job)
						availableJobs.add(job);
				} catch (BadLocationFault_Exception | BadPriceFault_Exception e) {
					// we already checked for these issues, so if the transporter server
					// doesn't like them just ignore that transporter, its their bug!
// 				}catch (JAXRException e){
				} catch (TransporterClientException e) {
					// nothing we can do here, just move on to the next transporter...
				}
			}
		}catch (JAXRException e){  // connection to UDDI failed
			UnavailableTransportFault fault = new UnavailableTransportFault();
			fault.setOrigin(origin);
			fault.setDestination(destination);
			throw new UnavailableTransportFault_Exception("Unable to contact any transporter company at this time.", fault);
		}
		return availableJobs;
	}

	// confirm transport
	private void bookJob(TransportView transport) {
		
// 		setContextForHandler();
		
		try {
			TransporterClient client = new TransporterClient(_uddiLocation, transport.getTransporterCompany());
			JobView job = client.getPort().decideJob(transportIdToJobId(transport), true);
			if(null != job && job.getJobState() == JobStateView.ACCEPTED)
				transport.setState(TransportStateView.BOOKED);
			else
				transport.setState(TransportStateView.FAILED);
		} catch (TransporterClientException | BadJobFault_Exception e) {
			transport.setState(TransportStateView.FAILED);
		}
	}

	// returns transport current state (updated)
	@Override
    public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
    
        JobView job;
        TransportView transport = getTransport(id);
        
// 		setContextForHandler();
		
       	try{
			job = new TransporterClient(_uddiLocation,
					transport.getTransporterCompany()).getPort().jobStatus(transportIdToJobId(transport));
		} catch (TransporterClientException e) {
			// if unable to connect to transporter return job as it is
			return transport;
		}
        
        if (job != null){
        	transport.setState(convertState(job.getJobState()));
        	propagateState("About that job I told you about, it's state just changed.");
        } else {
        	UnknownTransportFault faultInfo = new UnknownTransportFault();
			faultInfo.setId(id);
			throw new UnknownTransportFault_Exception("Transporter has no record of any job with given id.", faultInfo);
        }
        
    	return transport;
    }
    
    // returns the list of the current transports
	@Override
    public List<TransportView> listTransports() {
    	return _transportList;
    }

	// clears the list of current transports
	@Override
    public void clearTransports() {
    	_transportList.clear();
    	propagateState("Remember all the jobs I gave you? They're trash, just forget about them.");
    	
// 		setContextForHandler();
		
		try{
			Collection<String> transporters = (new UDDINaming(_uddiLocation)).list(TRANSPORTER_COMPANY_PREFIX + "_");

			for(String transporter : transporters) {
				try{
					(new TransporterClient(transporter)).getPort().clearJobs();
				} catch (TransporterClientException e) {
					// nothing we can do here, just move on to the next transporter...
				}
			}
		}catch (JAXRException e){
			// connection to UDDI failed nothing we can do about the transporters jobs...
		}
    }
}
