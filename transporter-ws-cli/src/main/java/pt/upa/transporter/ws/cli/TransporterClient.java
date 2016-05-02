package pt.upa.transporter.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;
import java.util.jar.JarException;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;

public class TransporterClient {
	public TransporterPortType port;
	
	public TransporterClient(String uddiURL, String name) throws TransporterClientException {
		//System.out.println(TransporterClient.class.getSimpleName() + " starting...");
		
		//System.out.printf("Contacting UDDI at %s%n", uddiURL);
		try {
			UDDINaming uddiNaming = new UDDINaming(uddiURL);

			//System.out.printf("Looking for '%s'%n", name);
			String endpointAddress = uddiNaming.lookup(name);

			if (endpointAddress == null) {
				System.out.println(name + " not found!");
				throw new TransporterClientException(String.format("Service with name %s not found on UDDI at %s", name, uddiURL));
			} else {
				//System.out.printf("Found %s%n", endpointAddress);
			}

			//System.out.println("Creating stub ...");
			TransporterService service = new TransporterService();
			this.port = service.getTransporterPort();

			//System.out.println("Setting endpoint address ...");
			BindingProvider bindingProvider = (BindingProvider) port;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
		}catch (JAXRException e){
			TransporterClientException ex = new TransporterClientException(String.format("Client failed lookup on UDDI at %s!", uddiURL));
			ex.initCause(e);
			throw ex;
		}
	}

	public TransporterClient(String endpointAddress) throws TransporterClientException {
		//System.out.println(TransporterClient.class.getSimpleName() + " starting...");

		if (endpointAddress == null) {
			System.out.println("Null endpoint Address!");
			return;
		}

		//System.out.println("Creating stub ...");
		TransporterService service = new TransporterService();
		this.port = service.getTransporterPort();

		//System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
	}

	public TransporterPortType getPort(){
		return port;
	}

}
