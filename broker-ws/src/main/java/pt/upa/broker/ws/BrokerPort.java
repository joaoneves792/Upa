package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.TransporterClient;

@WebService(
	endpointInterface="pt.upa.broker.ws.BrokerPortType",
    wsdlLocation="broker.1_0.wsdl",
    name="UpaBroker",
    portName="BrokerPort",
    targetNamespace="http://ws.broker.upa.pt/",
    serviceName="BrokerService"
)
public class BrokerPort implements BrokerPortType {

	private List<TransportView> _transportList = new ArrayList<>();
	private String _uddiLocation;
	private int _idCounter;

	private final String TRANSPORTER_COMPANY_PREFIX = "UpaTransporter_";

	public BrokerPort(String uddiLocation) {
		_uddiLocation = uddiLocation;
		_idCounter = 0;
	}
	
	private TransportView getTransport(String id)
			throws UnknownTransportFault_Exception {
				
		for (int i = 0; i < _transportList.size(); i++)
	    	if (_transportList.get(i).getId().equals(id))
				return _transportList.get(i);
		
		UnknownTransportFault faultInfo = new UnknownTransportFault();
		faultInfo.setId(id);
		throw new UnknownTransportFault_Exception("Invalid transport id", faultInfo);
	}
	
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
	
	@Override
    public String ping(String name) {
		String result = name + " UpaBroker";
		try {
			UDDINaming uddi = new UDDINaming(_uddiLocation);
			Collection<String> transporters = uddi.list(TRANSPORTER_COMPANY_PREFIX);

			for (String transporter : transporters) {
				try{
					TransporterClient client = new TransporterClient(transporter);
					result = client.port.ping(result);
				}catch (JAXRException e){
					result += " " + transporter + " failed! ";
				}
			}
		}catch(JAXRException e){
			result += " UDDI Failed ";
		}
		return result;
    }
	
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



		//Contact all transporter companies and get their proposals
		List<JobView> availableJobs;
		availableJobs = getJobProposals(origin, destination, price);

		if(availableJobs.isEmpty()){
			UnavailableTransportFault fault = new UnavailableTransportFault();
			fault.setOrigin(origin);
			fault.setDestination(destination);
			throw new UnavailableTransportFault_Exception("No transports available between" + origin + "and" + destination, fault);
		}

		//Pick the lowest priced job
		int lowestPrice = Integer.MAX_VALUE;
		JobView choosenJob = null;
		for (JobView job: availableJobs) {
			if(job.getJobPrice() < lowestPrice){
				lowestPrice = job.getJobPrice();
				choosenJob = job;
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

		return (transport.getState() == TransportStateView.BOOKED)? "BOOKED": "FAILED";
    }

	private List<JobView> getJobProposals(String origin, String destination, int price) throws UnavailableTransportFault_Exception {
		List<JobView> availableJobs = new ArrayList<>();
		try{
			UDDINaming uddi = new UDDINaming(_uddiLocation);
			Collection<String> transporters = uddi.list(TRANSPORTER_COMPANY_PREFIX);

			for(String transporter : transporters) {
				try {
					TransporterClient client = new TransporterClient(transporter);

					JobView job = client.getPort().requestJob(origin, destination, price);
					if (null != job)
						availableJobs.add(job);
				} catch (BadLocationFault_Exception | BadPriceFault_Exception e) {
					//We already checked for these issues, so if the transporter server
					//doesn't like them just ignore that transporter, its their bug!
				}catch (JAXRException e){
					//Nothing we can do here, just move on to the next transporter...
				}
			}
		}catch (JAXRException e){  //Connection to UDDI failed
			UnavailableTransportFault fault = new UnavailableTransportFault();
			fault.setOrigin(origin);
			fault.setDestination(destination);
			throw new UnavailableTransportFault_Exception("Unable to contact any transporter company at this time.", fault);
		}
		return availableJobs;
	}

	private void bookJob(TransportView transport) {
		try {
			TransporterClient client = new TransporterClient(_uddiLocation, transport.getTransporterCompany());
			JobView job = client.getPort().decideJob(transport.getId(), true);
			if(null != job && job.getJobState() == JobStateView.ACCEPTED)
				transport.setState(TransportStateView.BOOKED);
			else
				transport.setState(TransportStateView.FAILED);
		}catch (JAXRException | BadJobFault_Exception e){
			transport.setState(TransportStateView.FAILED);
		}
	}

	private void verifyLocation(String location) throws UnknownLocationFault_Exception {
		if(Locations.north.contains(location) || Locations.center.contains(location) || Locations.south.contains(location))
			return;
		UnknownLocationFault locationFault = new UnknownLocationFault();
		locationFault.setLocation(location);
		throw new UnknownLocationFault_Exception("Unrecognised location: "+ location, locationFault);
	}

	private TransportView createBudgetedTransport(JobView chosenJob) {
		TransportView budgetedTransport = new TransportView();
		budgetedTransport.setOrigin(chosenJob.getJobOrigin());
		budgetedTransport.setDestination(chosenJob.getJobDestination());
		budgetedTransport.setPrice(chosenJob.getJobPrice());
		budgetedTransport.setTransporterCompany(chosenJob.getCompanyName());
		budgetedTransport.setState(TransportStateView.BUDGETED);
		budgetedTransport.setId(Integer.toString(_idCounter++));
		return budgetedTransport;
	}

	@Override
    public TransportView viewTransport(String id)
            throws UnknownTransportFault_Exception {
        JobView job;
        TransportView transport = getTransport(id);
        
       	try{
			job = new TransporterClient(_uddiLocation, transport.getTransporterCompany()).getPort().jobStatus(id);
		}catch (JAXRException e){  //Connection to UDDI failed
			// if unable to connect to transporter return job as it is
			return transport;
		}
        
        if (job != null){
        	transport.setState(convertState(job.getJobState()));
        } else {
        	UnknownTransportFault faultInfo = new UnknownTransportFault();
			faultInfo.setId(id);
			throw new UnknownTransportFault_Exception("Transporter has no record of any job with given id.", faultInfo);
        }
        
    	return transport;
    }
    
	@Override
    public List<TransportView> listTransports() {
    	return _transportList;
    }

	@Override
    public void clearTransports() {
    	_transportList.clear();
    	_idCounter = 0;
		try{
			Collection<String> transporters = (new UDDINaming(_uddiLocation)).list(TRANSPORTER_COMPANY_PREFIX);

			for(String transporter : transporters) {
				try{
					(new TransporterClient(transporter)).getPort().clearJobs();
				}catch (JAXRException e){
					//Nothing we can do here, just move on to the next transporter...
				}
			}
		}catch (JAXRException e){
			/*Connection to UDDI failed nothing we can do about the transporters jobs...*/
		}
    }
    
}
