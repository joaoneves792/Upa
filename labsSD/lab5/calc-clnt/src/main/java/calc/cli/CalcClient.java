package calc.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

// classes generated from WSDL
import calc.DivideByZero;
import calc.CalcPortType;
import calc.CalcService;

public class CalcClient {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL name%n", CalcClient.class.getName());
			return;
		}

		String uddiURL = args[0];
		String name = args[1];

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
		CalcService service = new CalcService();
		CalcPortType port = service.getCalcPort();

		System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

		try {
			int result = port.sum(5, 6);
			System.out.println("5+6=");
			System.out.println(result);

            result = port.sub(6, 5);
			System.out.println("6-5=");
			System.out.println(result);
            
            result = port.mult(6, 5);
			System.out.println("6*5=");
			System.out.println(result);
            
            result = port.intdiv(9, 3);
			System.out.println("9/3=");
			System.out.println(result);

			System.out.println("9/0=");
            result = port.intdiv(9, 0);
			System.out.println(result);

		} catch (DivideByZero e) {
			System.out.println("Caught: " + e);
		}
	}

}
