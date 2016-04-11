package pt.upa.broker.ws.it;

import org.junit.*;
import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.TransporterPort;
import pt.upa.transporter.ws.cli.TransporterClient;

import pt.upa.broker.ws.*;
import pt.upa.broker.ws.BrokerPort;
import pt.upa.broker.ws.cli.BrokerClient;

import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

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
	private final String NORTH_LOCATION = "Porto";
	private final String SOUTH_LOCATION = "Setubal";
	private final String INVALID_ORIGIN = "Alameda";
	private final String INVALID_DESTINATION = "Porto Salvo";

	private final int VALID_PRICE = 100;
	private final int VALID_PRICE_ODD = 99;
	private final int VALID_PRICE_EVEN = 100;

	private final int INVALID_PRICE = -1;
	private final int OVERPRICED_PRICE = 101;
	private final int UNDERPRICED_PRICE = 9;

	private final String TRANSPORTER_ONE_NAME = "UpaTransporter1";
	private final String TRANSPORTER_TWO_NAME = "UpaTransporter2";

	private final String PING_MESSAGE = "Found:";
	private final String PING_RESPONSE = PING_MESSAGE + " "  + BROKER_NAME + " ";

// members //
	private UDDINaming _uddiNaming;
	
	private Endpoint _brokerEndpoint;
	
	// private Map<Integer, Endpoint> _transporterEndpoints = new TreeMap<Integer, Endpoint>();
	// private Map<Integer, TransporterPort> _transporters = new TreeMap<Integer, TransporterPort>();
	
	private BrokerPortType _broker;
	
// functions //

/*	private void startBroker() {
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
	}*/
	
// one-time initialization and clean-up //
    @BeforeClass
    public static void oneTimeSetUp() { }

    @AfterClass
    public static void oneTimeTearDown() { }


// initialization and clean-up for each test //
    @Before
    public void setUp() throws Exception {
		_broker = new BrokerClient(UDDI_URL, BROKER_NAME).getPort();
		_broker.clearTransports();

		//_transporter1 = new TransporterClient(UDDI_URL, TRANSPORTER_ONE_NAME).getPort();
		//_transporter2 = new TransporterClient(UDDI_URL, TRANSPORTER_TWO_NAME).getPort();

        //startBroker();
		//startTransporter(1);
		//startTransporter(2);
	}

    @After
    public void tearDown() {
//    	stopBroker();
// 		stopTransporter(1);
// 		stopTransporter(2);
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
		
		_uddiNaming = new UDDINaming(UDDI_URL);
		Collection<String> transporters = _uddiNaming.list(TRANSPORTER_NAME_PREFIX + "_");
		TransporterClient tClient;
		List<JobView> jobList;

		for (String transporter : transporters) {
			tClient = new TransporterClient(transporter);
			jobList = tClient.port.listJobs();
		
			assertEquals(jobList.size(), 0);
		}
    }
    
    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportNoneAvailableBadLocation() throws Exception {
        _broker.requestTransport(NORTH_LOCATION, SOUTH_LOCATION, VALID_PRICE);
    }

    
    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportNoneAvailableBadPrice() throws Exception {
        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, OVERPRICED_PRICE);
    }
    

	/* CANNOT BE DONE
    @Test(expected = UnavailableTransportPriceFault_Exception.class)
    public void requestTransportPriceTooHigh() throws Exception {
        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, UNDERPRICED_PRICE);
    }*/
    
    /* CANNOT BE DONE
    @Test
    public void requestTransportFailedBookNullJob() throws Exception {
        assertEquals("Booking should have failed", _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE), FAILED_JOB);
    }*/
    
    
    @Test
    public void requestTransportSuccess() throws Exception {
		String id = _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		TransportView tv = _broker.viewTransport(id);
		assertEquals(TransportStateView.BOOKED, tv.getState());
    }

	@Test
    public void listTransportsSuccess() throws Exception {
		_uddiNaming = new UDDINaming(UDDI_URL);
		List<TransportView> tvList;
		TransportView tv;
// 		TransporterClient tClient;
// 		List<JobView> jobList;
// 		JobView jv;

		_broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE_ODD);
		tvList = _broker.listTransports();
		assertEquals("Should only have one job", 1, tvList.size());
		
		tv = tvList.get(0);
		assertEquals(VALID_DESTINATION, tv.getDestination());
		assertEquals(VALID_ORIGIN, tv.getOrigin());
		assertEquals(TRANSPORTER_NAME_PREFIX + "1", tv.getTransporterCompany());
		assertTrue(tv.getPrice() <= VALID_PRICE_ODD);
		assertEquals(TransportStateView.BOOKED, tv.getState());
		
// 		tClient = new TransporterClient(_uddiNaming.lookup(TRANSPORTER_NAME_PREFIX + "2"));
// 		jobList = tClient.port.listJobs();
// 		assertEquals(jobList.size(), 1);
//
// 		jv = jvList.get(0);
// 		assertEquals(VALID_DESTINATION, jv.getDestination());
// 		assertEquals(VALID_ORIGIN, jv.getOrigin());
// 		assertTrue(jv.getPrice() <= VALID_PRICE_ODD);
// 		assertEquals(JobStateView.REJECTED, jv.getState());
		
		_broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE_EVEN);
		tvList = _broker.listTransports();
		assertEquals("Should have two jobs", 2, tvList.size());
		
		tv = tvList.get(1);
		assertEquals(VALID_DESTINATION, tv.getDestination());
		assertEquals(VALID_ORIGIN, tv.getOrigin());
		assertEquals(TRANSPORTER_NAME_PREFIX + "2", tv.getTransporterCompany());
		assertTrue(tv.getPrice() <= VALID_PRICE_EVEN);
		assertEquals(TransportStateView.BOOKED, tv.getState());
    }

	@Test
	public void viewTransportSucess() throws Exception {
        String id = _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		TransportView t = _broker.viewTransport(id);
        assertEquals(VALID_DESTINATION, t.getDestination());
        assertEquals(VALID_ORIGIN, t.getOrigin());
        assertEquals(id.split("_")[0], t.getTransporterCompany());
        assertTrue(t.getPrice() <= VALID_PRICE);
        assertEquals(TransportStateView.BOOKED, t.getState() );
        assertEquals(id, t.getId());
	}
    
	@Test
    public void viewTransportSuccessWithTimers() throws Exception {
        String id = _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		assertEquals("Returned transport state is not BOOKED", TransportStateView.BOOKED, _broker.viewTransport(id).getState());
		
		System.out.println("Testing timers...");
		Thread.sleep(15000);
	
		assertEquals("Returned transport state is not COMPLETED", TransportStateView.COMPLETED, _broker.viewTransport(id).getState());
	}
}
