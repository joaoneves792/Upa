package pt.upa.transporter.ws;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;

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

	@Override
	public String ping(String name) {
		return name + " pong";
	}
	
	@Override
    public JobView requestJob(String origin, String destination, int price)
            throws BadLocationFault_Exception, BadPriceFault_Exception {
		
		return new JobView();
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
		return  new ArrayList<JobView>();
	}
	
	@Override
	public void clearJobs() {
		
	}
}
