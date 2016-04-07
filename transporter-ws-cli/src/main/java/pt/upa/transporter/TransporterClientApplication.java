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
		
		System.out.println("\nping(): " + tclient.port.ping("ping") + "\n");
		
		System.out.println("requestJob(Lisboa,Leiria,50): "
						+ tclient.port.requestJob("Lisboa", "Leiria",50) + "\n");
						
		System.out.println("jobStatus(\"0\"): " + tclient.port.jobStatus("0"));
		System.out.println("jobStatus(\"-1\"): " + tclient.port.jobStatus("-1"));
		System.out.println();
		
		try {
			System.out.print("decideJob(\"0\", true): ");
			System.out.println(tclient.port.decideJob("0", true));
		} catch (Exception e) {
			System.out.println("Exception Caught: " + e.getMessage());
		}
	
		try {
			System.out.print("decideJob(\"-1\", true): ");
			System.out.println(tclient.port.decideJob("-1", true));
		} catch (Exception e) {
			System.out.println("Exception Caught: " + e.getMessage());
		}
		System.out.println();

// 		System.out.println();
// 		for(int t=128; t>-100; t = t-60)
// 			try {
// 				System.out.println("requestJob(Lisboa,Leiria,"+t+"): "
// 										+ tclient.port.requestJob("Lisboa", "Leiria", t));
// 			} catch (Exception  e) {
// 				System.out.println("requestJob() failed: " + e.getMessage());
// 			}
// 		
// 		System.out.println();
// 		for(int t=128; t>-100; t = t-60)
// 			try {
// 				switch(t) {
// 					case 128: System.out.println("requestJob(RandomName,Porto,"+t+"): "
// 											+ tclient.port.requestJob("RandomName", "Porto", t)); break;
// 					case 68: System.out.println("requestJob(Porto,Lisboa,"+t+"): "
// 											+ tclient.port.requestJob("Porto", "Lisboa", t)); break;
// 					case 8: System.out.println("requestJob(Leiria,Beja,"+t+"): "
// 											+ tclient.port.requestJob("Leiria", "Beja", t)); break;
// 					case -52: System.out.println("requestJob(Evora,Braga,"+t+"): "
// 											+ tclient.port.requestJob("Evora", "Braga", t)); break;
// 				}
// 				
// 			} catch (Exception  e) {
// 				System.out.println("requestJob() failed: " + e.getMessage());
// 			}
// 		
// 		System.out.println("\nlistJobs(): " + tclient.port.listJobs());
// 		
// 		System.out.println("\nclearJobs(): "); tclient.port.clearJobs();
// 		
// 		System.out.println("\nlistJobs(): " + tclient.port.listJobs());
// 		
// 		System.out.println();

	}
}
