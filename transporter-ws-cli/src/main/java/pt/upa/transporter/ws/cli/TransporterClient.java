package pt.upa.transporter.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;

public class TransporterClient {
	public TransporterPortType port;
	
	public TransporterClient(String uddiURL, String name) throws Exception {
		System.out.println(TransporterClient.class.getSimpleName() + " starting...");
		
		System.out.printf("Contacting UDDI at %s%n", uddiURL);
		UDDINaming uddiNaming = new UDDINaming(uddiURL);

		System.out.printf("Looking for '%s'%n", name);
		String endpointAddress = uddiNaming.lookup(name);

		if (endpointAddress == null) {
			System.out.println("Not found!");
			return;
		} else {
			System.out.printf("Found %s%n", endpointAddress);
		}

		System.out.println("Creating stub ...");
		TransporterService service = new TransporterService();
		this.port = service.getTransporterPort();

		System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);	
	}
	
	
	
	
	
/*
	// don't this is necessary. it requires a bunch of imports
	
	public String ping(String name) {
		return port.ping(name);
	}
	
    public JobView requestJob(String origin, String destination, int price)
			throws BadLocationFault_Exception, BadPriceFault_Exception {
		
		return port.requestJob(origin, destination, price);
	}
	
    public JobView decideJob(String id,boolean accept)
			throws BadJobFault_Exception {
		
    	return port.decideJob(id, accept);
    }
	
	public JobView jobStatus(String id) {
		return port.jobStatus(id);
	}
	
	public List<JobView> listJobs() {		 
		return port.listJobs();
	}
	
	public void clearJobs() {
		port.clearJobs();
	}
	
*/

}
