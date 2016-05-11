package pt.upa.broker.ws;

// import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.HandlerChain;
import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.TransporterClient;
import pt.upa.transporter.ws.cli.TransporterClientException;
import pt.upa.broker.ws.cli.BrokerClient;
import pt.upa.broker.ws.cli.BrokerClientException;

import pt.upa.ws.handler.SignatureHandler;
import java.security.NoSuchAlgorithmException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;


// @HandlerChain(file="/handler-chain.xml")
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

	private BrokerClient _backupServer;
	private Timer _timer;
	private boolean _backupMode;
	
	
	private Map<String, String> _sentNounces = new TreeMap<String, String>();
	private Map<String, String> _receivedNounces = new TreeMap<String, String>();
	
	
	@Resource
	private WebServiceContext webServiceContext;
	
	private String getNounceFromContext() {
		MessageContext mc = webServiceContext.getMessageContext();
		return (String) mc.get("recievedNounce");
	}
	
	private void setContextForHandler(TransporterClient client, String transporter) {
		if(webServiceContext != null) {
			try {
				String nounceToSend = SignatureHandler.getSecureRandom(_sentNounces);
				client.setContext(transporter, nounceToSend);
				propagateNounce(UpdateNounceDirection.SENT, nounceToSend);
			} catch (NoSuchAlgorithmException e) {
				System.err.println("Failed to generate random: " + e.getMessage());
			}
		}
	}
	
	private void verifyNonce() throws BrokerException {
		if(webServiceContext != null) {
			String nounce = getNounceFromFile("/NonceDump.txt");
			if(SignatureHandler.nounceIsValid(_receivedNounces, nounce))
				propagateNounce(UpdateNounceDirection.RECIEVED, nounce);
			else
				throw new BrokerException("Recieved message with duplicated nonce.");
		}
	}
	
	/**
	 *	SOAPContext gets deleted before broker has a chance to get the nonce
	 *	consider the text file is an alternative context sharing channel
	 */
	private String getNounceFromFile(String path) {
		Charset charset = Charset.forName("US-ASCII");
		String absPath = new File("").getAbsolutePath() + path;

		Path filePath = Paths.get(absPath);
		String line = null;

		try (BufferedReader reader = Files.newBufferedReader(filePath, charset)) {
			line = reader.readLine();
			
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
		return line;
	}
	
	
	
// constructors //

	public BrokerPort(String uddiLocation) {
		_uddiLocation = uddiLocation;
	}

	public BrokerPort(String uddiLocation, boolean backupMode) {
		_uddiLocation = uddiLocation;
		_backupMode = backupMode;
		
		if (_backupMode) {
			System.out.println("Running on backup mode.");
			_timer = new Timer();
		
		} else {
			try{
				_backupServer = new BrokerClient(_uddiLocation, "UpaBrokerBackup");
				if(_backupServer.getPort() != null) {
					_timer = new Timer();
					_timer.schedule(new sendSignalTask(), SIGNAL_TIME);
					System.out.println("Backup Server found!");
				} else {
					_backupServer = null;
					System.out.println("Backup Server not found!");
				}
				
			} catch (BrokerClientException e) {
				_backupServer = null;
				System.out.println("Backup Server not found!");
			}
		}
	}
	
	
	
// backup related methods //
	
	private class sendSignalTask extends TimerTask {
		@Override
		public void run() {
			propagateState(UpdateAction.IMALIVE, null);
		}
	}
	
	private class declareServerDeadTask extends TimerTask {
		@Override
		public void run() {
			try{
				System.out.println("Contact with the server was lost. Taking over.");
				
				UDDINaming uddiNaming = new UDDINaming(_uddiLocation);
				String url = uddiNaming.lookup("UpaBrokerBackup");	// FIXME lookup record
				
				uddiNaming.unbind("UpaBrokerBackup");
				System.out.println("Deleted 'UpaBrokerBackup' from UDDI");
				
				uddiNaming.rebind("UpaBroker", url);
				System.out.println("Added 'UpaBroker' to UDDI");
 			
			} catch (JAXRException e) {
				System.out.printf("Caught exception when taking over as main server: %s%n", e);
			}
		}
	}
	
	// timer needs to be explicitly stopped (since it runs on a different thread)
	public void restartTimer(TimerTask task, int time){
		if (_timer != null) {
			_timer = new Timer();
			_timer.schedule(task, time);
		}
	}
	
	// timer needs to be explicitly stopped (since it runs on a different thread)
	public void stopTimer(){
		if (_timer != null)
			_timer.cancel();
	}
	
	private void propagateState(UpdateAction action, TransportView transport) {
		try {
			if (_backupServer != null) {
				stopTimer();
				_backupServer.getPort().updateState(action, transport);
				restartTimer(new sendSignalTask(), SIGNAL_TIME);
			}
			
		} catch (BrokerClientException e) {
			_backupServer = null;
			System.out.println("Backup Server lost.");
		}
	}
	
	private void propagateNounce(UpdateNounceDirection dir, String nounce){
		try {
			if (_backupServer != null) {
				stopTimer();
				_backupServer.getPort().updateNounce(dir, nounce);
				restartTimer(new sendSignalTask(), SIGNAL_TIME);
			}
			
		} catch (BrokerClientException e) {
			_backupServer = null;
			System.out.println("Backup Server lost.");
		}
		
	}
	
	@Override
	public void updateNounce(UpdateNounceDirection dir, String nounce) {
		stopTimer();
		restartTimer(new declareServerDeadTask(), SIGNAL_TIME*2);

		switch (dir) {
			case SENT:
				_sentNounces.put(nounce, nounce);
				System.out.println("setNounces " + nounce + " added.");
				break;
				
			case RECIEVED:
				_receivedNounces.put(nounce, nounce);
				System.out.println("recievedNounces " + nounce + " added.");
				break;
		}
		
	}
	
	@Override
	public void updateState(UpdateAction action, TransportView transport){
		stopTimer();
		restartTimer(new declareServerDeadTask(), SIGNAL_TIME*2);
		
		switch (action) {
			case ADD:
				_transportList.add(transport);
				System.out.println("Job " + transport.getId() + " added.");
				break;
				
			case UPDATE:
				try{
					getTransport(transport.getId()).setState(transport.getState());
					System.out.println("Job " + transport.getId() + " updated to " + transport.getState() + ".");
				} catch (UnknownTransportFault_Exception e) {
					System.out.println("Error: Job " + transport.getId() + " not found!");
				}
				break;
				
			case CLEAR:
				_transportList.clear();
				System.out.println("Job list cleared.");
				break;
				
			case IMALIVE:
				System.out.println(".");
				break;
		}
		
	}
	

	
// auxiliary broker functions //

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
	
	
	
// WSDL functions //
	
	// ping transporters
	@Override
    public String ping(String name) {
		String result = name + " UpaBroker";
		String aux = "";
		
		try {
			UDDINaming uddi = new UDDINaming(_uddiLocation);
			Collection<String> transporters = uddi.list(TRANSPORTER_COMPANY_PREFIX + "_");
			
			TransporterClient client = null;
			for (String transporter : transporters) {
				try{
					client = new TransporterClient(transporter);
					setContextForHandler(client, transporter);
					
					aux = client.port.ping(result);
					verifyNonce();
					
					result = aux;
					
				} catch (TransporterClientException e) {
					result += " " + transporter + " Failed ";
				} catch (BrokerException e) {
					System.err.println(e.getMessage());
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
		
		TransporterClient client = null;
		//Ditch all the other proposals
		for(JobView job : availableJobs){
			if(job.getCompanyName().equals(choosenJob.getCompanyName()) && job.getJobIdentifier().equals(choosenJob.getJobIdentifier()))
				continue;
			try {
				client = new TransporterClient(_uddiLocation, job.getCompanyName());
				setContextForHandler(client, job.getCompanyName());
				
				client.getPort().decideJob(job.getJobIdentifier(), false);
				verifyNonce();
					
			} catch (TransporterClientException | BadJobFault_Exception e) {
				//Not our fault if we cant contact them or if the id doesnt match them, just skip to the next one
			} catch (BrokerException e) {
				System.err.println(e.getMessage());
			}
		}
		
		if(choosenJob.getJobPrice() > price){
			UnavailableTransportPriceFault fault = new UnavailableTransportPriceFault();
			fault.setBestPriceFound(choosenJob.getJobPrice());
			throw new UnavailableTransportPriceFault_Exception("No transport found for your maximum price, lowest available is: " + choosenJob.getJobPrice(), fault);
		}
		
		TransportView transport = createBudgetedTransport(choosenJob);
		_transportList.add(transport);
		bookJob(transport);
		
		propagateState(UpdateAction.ADD, transport);
		
		return transport.getId();
    }

	// returns a list of the jobs proposed by the transporters
	private List<JobView> getJobProposals(String origin, String destination, int price) throws UnavailableTransportFault_Exception {
		List<JobView> availableJobs = new ArrayList<>();
		
		try{
			UDDINaming uddi = new UDDINaming(_uddiLocation);
			Collection<String> transporters = uddi.list(TRANSPORTER_COMPANY_PREFIX + "_");
			
			TransporterClient client = null;
			for(String transporter : transporters) {
				try {
					client = new TransporterClient(transporter);
					setContextForHandler(client, transporter);
					
					JobView job = client.getPort().requestJob(origin, destination, price);
					verifyNonce();
					
					if (null != job)
						availableJobs.add(job);
						
				} catch (BadLocationFault_Exception | BadPriceFault_Exception e) {
					// we already checked for these issues, so if the transporter server
					// doesn't like them just ignore that transporter, its their bug!
				} catch (TransporterClientException e) {
					// nothing we can do here, just move on to the next transporter...
				} catch (BrokerException e) {
					System.err.println(e.getMessage());
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
		
		try {
			TransporterClient client = new TransporterClient(_uddiLocation, transport.getTransporterCompany());
			setContextForHandler(client, transport.getTransporterCompany());
			
			JobView job = client.getPort().decideJob(transportIdToJobId(transport), true);
			verifyNonce();
			
			if(null != job && job.getJobState() == JobStateView.ACCEPTED)
				transport.setState(TransportStateView.BOOKED);
			else
				transport.setState(TransportStateView.FAILED);
		} catch (TransporterClientException | BadJobFault_Exception e) {
			transport.setState(TransportStateView.FAILED);
		} catch (BrokerException e) {
			System.err.println(e.getMessage());
		}
	}

	// returns transport current state (updated)
	@Override
    public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		
        JobView job = null;
        TransportView transport = getTransport(id);
		
       	try{
			TransporterClient client = new TransporterClient(_uddiLocation, transport.getTransporterCompany());
			setContextForHandler(client, transport.getTransporterCompany());
			
			job = client.getPort().jobStatus(transportIdToJobId(transport));
			verifyNonce();
			
		} catch (TransporterClientException e) {
			// if unable to connect to transporter return job as it is
			return transport;
		} catch (BrokerException e) {
			System.err.println(e.getMessage());
		}
        
        if (job != null){
        	transport.setState(convertState(job.getJobState()));
        	propagateState(UpdateAction.UPDATE, transport);
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
    	propagateState(UpdateAction.CLEAR, null);
		
		try{
			UDDINaming uddi = new UDDINaming(_uddiLocation);
			Collection<String> transporters = uddi.list(TRANSPORTER_COMPANY_PREFIX + "_");
			
			TransporterClient client = null;
			for(String transporter : transporters) {
				try {
					client = new TransporterClient(_uddiLocation, transporter);
					setContextForHandler(client, transporter);
					
					client.getPort().clearJobs();
					verifyNonce();
						
				} catch (TransporterClientException e) {
					// nothing we can do here, just move on to the next transporter...
				} catch (BrokerException e) {
					System.err.println(e.getMessage());
				}
			}
		}catch (JAXRException e){
			// connection to UDDI failed nothing we can do about the transporters jobs...
		}
    }
}

