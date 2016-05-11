package pt.upa.transporter;

import pt.upa.transporter.ws.cli.TransporterClient;

public class TransporterClientApplication {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL name%n", TransporterClientApplication.class.getName());
			return;
		}
		
		TransporterClient tclient = new TransporterClient(args[0], args[1]);
		
		// just testing functions from here on
		
		System.out.println(tclient.getPort().ping("Found:") + "\n");
	}
}
