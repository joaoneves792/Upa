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


	List<JobView> _jobsList = new ArrayList<JobView>();

	@Override
	public String ping(String name) {
		return name + " UpaTransporter";
	}
	
	@Override
    public JobView requestJob(String origin, String destination, int price)
			throws BadLocationFault_Exception, BadPriceFault_Exception {

		JobView job = new JobView();
		
		
		
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
		return _jobsList;
	}
	
	@Override
	public void clearJobs() {
		_jobsList.clear();
	}
}
