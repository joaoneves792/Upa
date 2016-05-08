package pt.upa.broker;

import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.BrokerPort;

public class BrokerApplication {

	public static void main(String[] args) throws Exception {
		System.out.println(BrokerApplication.class.getSimpleName() + " starting...");
			
		// Check arguments
		if (args.length < 3) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL wsName wsURL [wsBackupMode]%n", BrokerApplication.class.getName());
			return;
		}
		
		String uddiURL = args[0];
		String wsname = args[1];
		String url = args[2];
		boolean backupMode = false;
		String name = wsname;
		
		if (args.length >= 4 && (args[3].equals("true") || args[3].equals("True"))){
			name = name + "Backup";
			backupMode = true;
		}

		BrokerPort port = null;
		Endpoint endpoint = null;
		UDDINaming uddiNaming = null;
		try {
			port = new BrokerPort(uddiURL, backupMode);
			endpoint = Endpoint.create(port);

			// publish endpoint
			System.out.printf("Starting %s%n", url);
			endpoint.publish(url);

			// publish to UDDI
			System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiURL);
			uddiNaming = new UDDINaming(uddiURL);
			uddiNaming.rebind(name, url);

			// wait
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
			System.in.read();

		} catch (Exception e) {
			System.out.printf("Caught exception: %s%n", e);
			e.printStackTrace();

		} finally {
			//stop broker timer tasks
			if (port != null)
				port.stopTimer();
			
			try {
				if (endpoint != null) {
					// stop endpoint
					endpoint.stop();
					System.out.printf("Stopped %s%n", url);
				}
			} catch (Exception e) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
			try {
				if (uddiNaming != null) {
					
					// check if backup server has taken over
					if (backupMode && uddiNaming.lookup(name) == null)
						name = wsname;
					
					// delete from UDDI
					uddiNaming.unbind(name);
					System.out.printf("Deleted '%s' from UDDI%n", name);
				}
			} catch (Exception e) {
				System.out.printf("Caught exception when deleting: %s%n", e);
			}
		}

	}
	
}
