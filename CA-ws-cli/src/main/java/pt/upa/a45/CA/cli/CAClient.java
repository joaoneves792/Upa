package pt.upa.a45.CA.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

// classes generated from WSDL
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.a45.ca.CA;
import pt.upa.a45.ca.CAException_Exception;
import pt.upa.a45.ca.CAImplService;

public class CAClient {

	private CA _port;

	public CAClient (String uddiURL)throws CAException{
		String name = "CA-ws";

		try {
			UDDINaming uddiNaming = new UDDINaming(uddiURL);
			String endpointAddress = uddiNaming.lookup(name);

			if (endpointAddress == null) {
				throw new CAException("Unable to contact the CA");
			}

			CAImplService service = new CAImplService();
			_port = service.getCAImplPort();

			BindingProvider bindingProvider = (BindingProvider) _port;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

		}catch (JAXRException e){
			throw new CAException("Unable to contact the CA");
		}
	}

	public X509Certificate getCertificate(String entity)throws CAException{
		try {
			return certFromByteArray(_port.getCertificate(entity));
		}catch (CAException_Exception e){
			throw new CAException(e.getMessage());
		}catch (CertificateException e){
			throw new CAException("Failed to rebuild certificate: " + e.getMessage());
		}
	}

	private X509Certificate certFromByteArray(byte[] data)throws CertificateException{
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(data));
	}
}
