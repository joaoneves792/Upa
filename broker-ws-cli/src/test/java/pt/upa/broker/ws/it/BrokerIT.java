package pt.upa.broker.ws.it;

import org.junit.*;
import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.TransporterPort;

import pt.upa.broker.ws.*;
import pt.upa.broker.ws.BrokerPort;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Timer;
import java.util.TimerTask;

import static org.junit.Assert.*;

/**
 *  Integration Test
 *
 *  Invoked by Maven in the "verify" life-cycle phase
 *  Should invoke "live" remote servers
 */
public class BrokerIT {

// static members //
	private final String UDDI_URL = "http://localhost:9090";

	private final String BROKER_NAME = "UpaBroker";
	private final String BROKER_URL = "http://localhost:8080/broker-ws/endpoint";

	private final String TRANSPORTER_NAME_PREFIX = "UpaTransporter";
	private final String TRANSPORTER_URL_PREFIX = "http://localhost:808";
	private final String TRANSPORTER_URL_SUFIX = "/transporter-ws/endpoint";

	private final String VALID_ORIGIN = "Lisboa";
    private final String VALID_DESTINATION = "Leiria";
    private final String INVALID_ORIGIN = "Alameda";
    private final String INVALID_DESTINATION = "Porto Salvo";

    private final int VALID_PRICE = 100;
    private final int INVALID_PRICE = -1;
    private final int OVERPRICED_PRICE = 101;
    private final int UNDERPRICED_PRICE = 9;

	private final String TRANSPORTER_ONE_NAME = "UpaTransporter1";
	private final String TRANSPORTER_TWO_NAME = "UpaTransporter2";

	private final String PING_MESSAGE = "Found:";
	private final String PING_RESPONSE = PING_MESSAGE + " "  + BROKER_NAME + " ";
    
// members //
	private UDDINaming _uddiNaming;
	
	private BrokerPort _broker;
	private Endpoint _brokerEndpoint;
	
	private Map<Integer, Endpoint> _transporterEndpoints = new TreeMap<Integer, Endpoint>();
	private Map<Integer, TransporterPort> _transporters = new TreeMap<Integer, TransporterPort>();
	
	
// functions //

	private void startBroker() {
		
		try {
			_uddiNaming = new UDDINaming(UDDI_URL);

			if(_uddiNaming.lookup(BROKER_NAME) == null) {
				_broker = new BrokerPort(UDDI_URL);
				_brokerEndpoint = Endpoint.create(_broker);

				System.out.printf("Starting %s%n", BROKER_URL);
				_brokerEndpoint.publish(BROKER_URL);
				
				//System.out.printf("Publishing '%s' to UDDI at %s%n", BROKER_NAME, UDDI_URL);
				_uddiNaming.rebind(BROKER_NAME, BROKER_URL);
			}
		} catch (Exception e) {
			System.out.printf("Caught exception: %s%n", e);
			e.printStackTrace();
		}
	}
	
	private void stopBroker() {
			try {
				if (_brokerEndpoint != null) {
					_brokerEndpoint.stop();
				}
			} catch (Exception e) { System.out.printf("Caught exception when stopping: %s%n", e); }
			
			try {
				if (_uddiNaming != null) {
					_uddiNaming.unbind(BROKER_NAME);
				}
			} catch (Exception e) { System.out.printf("Caught exception when deleting: %s%n", e); }
	}
	
	private void startTransporter(int id) {
		String TRANSPORTER_URL = TRANSPORTER_URL_PREFIX + id + TRANSPORTER_URL_SUFIX;
		String TRANSPORTER_NAME = TRANSPORTER_NAME_PREFIX + id;
		
		try {
			if(_uddiNaming.lookup(TRANSPORTER_NAME) == null) {
				System.out.printf("Starting %s%n", TRANSPORTER_URL);
				TransporterPort port = new TransporterPort(id);
				Endpoint endpoint = Endpoint.create(port);
				
				_transporters.put(id , port);
				_transporterEndpoints.put(id, endpoint);
				
				//System.out.printf("Publishing '%s' to UDDI at %s%n", TRANSPORTER_NAME, UDDI_URL);
				endpoint.publish(TRANSPORTER_URL);

				_uddiNaming.rebind(TRANSPORTER_NAME, TRANSPORTER_URL);
			}
		} catch (Exception e) {
			System.out.printf("Caught exception: %s%n", e);
			e.printStackTrace();
		}
	}

	private void stopTransporter(int id) {
		String TRANSPORTER_NAME = TRANSPORTER_NAME_PREFIX + id;

		try {
			if (_transporterEndpoints.get(id) != null) {
				_transporterEndpoints.get(id).stop();
			}
		} catch (Exception e) { System.out.printf("Caught exception when stopping: %s%n", e); }
		
		try {
			if (_uddiNaming != null && _transporters.get(id) != null) {
				_uddiNaming.unbind(TRANSPORTER_NAME);
			}
		} catch (Exception e) { System.out.printf("Caught exception when deleting: %s%n", e); }
	}

	
	

	
// one-time initialization and clean-up //
    @BeforeClass
    public static void oneTimeSetUp() { }

    @AfterClass
    public static void oneTimeTearDown() { }


// initialization and clean-up for each test //
    @Before
    public void setUp() {
		startBroker();
		startTransporter(1);
		startTransporter(2);
	}

    @After
    public void tearDown() {
   		stopBroker();
		stopTransporter(1);
		stopTransporter(2);
	}


// tests //


    @Test
    public void pingSuccess() {
		String result = _broker.ping(PING_MESSAGE);
        assertTrue((result.equals(PING_RESPONSE + TRANSPORTER_ONE_NAME + " " + TRANSPORTER_TWO_NAME)) || (result.equals(PING_RESPONSE + TRANSPORTER_TWO_NAME + " " + TRANSPORTER_ONE_NAME)));
    }


	@Test
    public void clearTransportsSuccess() throws Exception {
		_broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		_broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		
		List<TransportView> tvList = _broker.listTransports();
		assertEquals(tvList.size(), 2);
		
		_broker.clearTransports();
		tvList = _broker.listTransports();
		assertEquals(tvList.size(), 0);
		
		List<JobView> jobList;
		
		for(Map.Entry<Integer, TransporterPort> entry : _transporters.entrySet()) {
			jobList = entry.getValue().listJobs();
			assertEquals(jobList.size(), 0);
		}
    }
    
/*   
    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportNoneAvailableBadLocation() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (_uddi);

                _uddi.list(TRANSPORTER_COMPANY_PREFIX + "_");
                result = (_transporterList);

                new TransporterClient((String) any);
                result = (_client);

                _client.getPort();
                result = (_tpt);

                _tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (new BadLocationFault_Exception("", new BadLocationFault()));

            }
        };
        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }
    
    
    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportNoneAvailableBadPrice() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (_uddi);

                _uddi.list(TRANSPORTER_COMPANY_PREFIX + "_");
                result = (_transporterList);

                new TransporterClient((String) any);
                result = (_client);

                _client.getPort();
                result = (_tpt);

                _tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (new BadPriceFault_Exception("", new BadPriceFault()));

            }
        };
        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }
    
    
    @Test(expected = UnavailableTransportPriceFault_Exception.class)
    public void requestTransportPriceTooHigh() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (_uddi);

                _uddi.list(TRANSPORTER_COMPANY_PREFIX + "_");
                result = (_transporterList);

                new TransporterClient((String) any);
                result = (_client);

                _client.getPort();
                result = (_tpt);

                _tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (_overpricedJob);

            }
        };
        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }
    
    
    @Test
    public void requestTransportFailedBookNullJob() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (_uddi);

                _uddi.list(TRANSPORTER_COMPANY_PREFIX + "_");
                result = (_transporterList);

                new TransporterClient((String) any);
                result = (_client);

                _client.getPort();
                result = (_tpt);

                _tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (_underpricedJob);

                _tpt.decideJob((String) any, true);
                result = (null);

            }
        };
        assertEquals("Booking should have failed", _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE), FAILED_JOB);
    }
    
    
    @Test
    public void requestTransportSuccess() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (_uddi);

                _uddi.list(TRANSPORTER_COMPANY_PREFIX + "_");
                result = (_transporterList);

                new TransporterClient((String) any);
                result = (_client);

                _client.getPort();
                result = (_tpt);

                _tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (_underpricedJob);

                _tpt.decideJob((String) any, true);
                result = (_acceptedJob);

            }
        };
        assertEquals("Booking should have failed", _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE), BOOKED_JOB);
    }
    
    
    @Test(expected = UnknownTransportFault_Exception.class)
	public void viewTransportInvalidTransporterId() throws Exception {
	    new Expectations() {
            {
                new UDDINaming((String) any);
                result = (_uddi);

                _uddi.list(TRANSPORTER_COMPANY_PREFIX + "_");
                result = (_transporterList);

                new TransporterClient((String) any);
                result = (_client);

                _client.getPort();
                result = (_tpt);

                _tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (_underpricedJob);

                _tpt.decideJob((String) any, true);
                result = (_acceptedJob);
                
                _tpt.jobStatus((String) any);
                result = null;
            }
        };
        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		_broker.viewTransport("0");
	}
*/
    
//     @Test
// 	public void viewTransportSucess() throws Exception {
// 
//         _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
// 		TransportView t = _broker.viewTransport(TRANSPORTER_NAME_PREFIX + "1_0");
// 		
//         assertEquals(t.getDestination(), VALID_DESTINATION);
//         assertEquals(t.getOrigin(), VALID_ORIGIN);
//         assertEquals(t.getTransporterCompany(), TRANSPORTER_NAME_PREFIX + "1");
//         assertTrue(t.getPrice() <= VALID_PRICE);
//         assertEquals(t.getState(), TransportStateView.BOOKED);
//         assertEquals(t.getId(), TRANSPORTER_NAME_PREFIX + "1_0");
// 	}
    
/*
	@Test
    public void listTransportsSuccess() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (_uddi);

                _uddi.list(TRANSPORTER_COMPANY_PREFIX + "_");
                result = (_transporterList);

                new TransporterClient((String) any);
                result = (_client);

                _client.getPort();
                result = (_tpt);

                _tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (_underpricedJob);

                _tpt.decideJob((String) any, true);
                result = (_acceptedJob);

            }
        };
        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        List<TransportView> tl = _broker.listTransports();
        assertEquals("Should only have one job", tl.size(), 1);
        assertEquals(tl.get(0).getDestination(), VALID_DESTINATION);
        assertEquals(tl.get(0).getOrigin(), VALID_ORIGIN);
        assertEquals(tl.get(0).getTransporterCompany(), TRANSPORTER_COMPANY_PREFIX + "1");
        assertTrue(tl.get(0).getPrice() <= VALID_PRICE);
        assertEquals(tl.get(0).getState(), TransportStateView.BOOKED);

        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        tl = _broker.listTransports();
        assertEquals("Should have two jobs", tl.size(), 2);
        assertEquals(tl.get(1).getDestination(), VALID_DESTINATION);
        assertEquals(tl.get(1).getOrigin(), VALID_ORIGIN);
        assertEquals(tl.get(1).getTransporterCompany(), TRANSPORTER_COMPANY_PREFIX + "1");
        assertTrue(tl.get(1).getPrice() <= VALID_PRICE);
        assertEquals(tl.get(1).getState(), TransportStateView.BOOKED);
    }
    
    
    // othername
	@Test
    public void decideJobRejectSuccess() throws Exception {
    	_transporter.setJobMinTime(100);
        _transporter.setJobMaxTime(101);

        _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        JobView job = _transporter.decideJob("0", false);
		assertEquals("Returned job state is not REJECTED.", JobStateView.REJECTED, job.getJobState());
		
		Thread.sleep(400);
		
		assertEquals("Timers shouldn't change a rejected job state", JobStateView.REJECTED, job.getJobState());
	}
	
	
	// othername
	@Test
    public void decideJobAcceptSuccess() throws Exception {
    	_transporter.setJobMinTime(100);
        _transporter.setJobMaxTime(101);
        
        _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        JobView job = _transporter.decideJob("0", true);
		assertEquals("Returned job state is not ACCEPTED", JobStateView.ACCEPTED, job.getJobState());
				
    	Timer timer = new Timer();
		timer.schedule(new CheckStateTask(job, JobStateView.HEADING, this), 150);
		timer.schedule(new CheckStateTask(job, JobStateView.ONGOING, this), 250);
		timer.schedule(new CheckStateTask(job, JobStateView.COMPLETED, this), 350);
		Thread.sleep(400);

		assertFalse("Timer based change of states ain't working correctly.", _error);
	}

*/

}
