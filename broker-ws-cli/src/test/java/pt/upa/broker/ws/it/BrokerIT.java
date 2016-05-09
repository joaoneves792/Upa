package pt.upa.broker.ws.it;

import org.junit.*;
import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.TransporterClient;

import pt.upa.broker.ws.*;
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

	private final String TRANSPORTER_NAME_PREFIX = "UpaTransporter";

	private final String VALID_ORIGIN = "Lisboa";
	private final String VALID_DESTINATION = "Leiria";
	private final String INVALID_ORIGIN = "Alameda";
	private final String INVALID_DESTINATION = "Porto Salvo";
	private final String NORTH_LOCATION = "Porto";
	private final String SOUTH_LOCATION = "Setubal";

	private final int VALID_PRICE = 100;
	private final int VALID_PRICE_ODD = 99;
	private final int VALID_PRICE_EVEN = 100;

	private final int OVERPRICED_PRICE = 101;
	private final int INVALID_PRICE = -1;

	private final String TRANSPORTER_ONE_NAME = "UpaTransporter1";
	private final String TRANSPORTER_TWO_NAME = "UpaTransporter2";

	private final String PING_MESSAGE = "Found:";
	private final String PING_RESPONSE = PING_MESSAGE + " "  + BROKER_NAME + " ";

// members //
	private UDDINaming _uddiNaming;
	private BrokerClient _broker;
	
// one-time initialization and clean-up //
    @BeforeClass
    public static void oneTimeSetUp() { }

    @AfterClass
    public static void oneTimeTearDown() { }


// initialization and clean-up for each test //
    @Before
    public void setUp() throws Exception {
		_broker = new BrokerClient(UDDI_URL, BROKER_NAME);
		_broker.getPort().clearTransports();
	}

    @After
    public void tearDown() {
	}


// tests //

    @Test
    public void pingSuccess() throws Exception {
		String result = _broker.getPort().ping(PING_MESSAGE);
        assertTrue((result.equals(PING_RESPONSE + TRANSPORTER_ONE_NAME + " " + TRANSPORTER_TWO_NAME)) || (result.equals(PING_RESPONSE + TRANSPORTER_TWO_NAME + " " + TRANSPORTER_ONE_NAME)));
    }

	@Test
    public void clearTransportsSuccess() throws Exception {
		_broker.getPort().requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		_broker.getPort().requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		
		List<TransportView> tvList = _broker.getPort().listTransports();
		assertEquals(tvList.size(), 2);
		
		_broker.getPort().clearTransports();
		tvList = _broker.getPort().listTransports();
		assertEquals(tvList.size(), 0);

		/* Cant go on, for that we would need a SignatureHandler
		and we can't just have a security handler running only for this test...*/

		/*_uddiNaming = new UDDINaming(UDDI_URL);
		Collection<String> transporters = _uddiNaming.list(TRANSPORTER_NAME_PREFIX + "_");
		TransporterClient tClient;
		List<JobView> jobList;

		for (String transporter : transporters) {
			tClient = new TransporterClient(transporter);
			jobList = tClient.port.listJobs();
		
			assertEquals(jobList.size(), 0);
		}*/
    }
    
    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportNoneAvailableBadLocation() throws Exception {
        _broker.getPort().requestTransport(NORTH_LOCATION, SOUTH_LOCATION, VALID_PRICE);
    }

    
    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportNoneAvailableBadPrice() throws Exception {
        _broker.getPort().requestTransport(VALID_ORIGIN, VALID_DESTINATION, OVERPRICED_PRICE);
    }

	@Test(expected = InvalidPriceFault_Exception.class)
	public void requestTransportInvalidPrice() throws Exception{
		_broker.getPort().requestTransport(VALID_ORIGIN, VALID_DESTINATION, INVALID_PRICE);
	}

	@Test(expected =UnknownLocationFault_Exception.class )
	public void requestTransportUnknownOrigin()throws Exception{
		_broker.getPort().requestTransport(INVALID_ORIGIN, VALID_DESTINATION,VALID_PRICE);
	}

	@Test(expected =UnknownLocationFault_Exception.class )
	public void requestTransportUnknownDestination()throws Exception{
		_broker.getPort().requestTransport(VALID_ORIGIN, INVALID_DESTINATION,VALID_PRICE);
	}


    @Test
    public void requestTransportSuccess() throws Exception {
		String id = _broker.getPort().requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		TransportView tv = _broker.getPort().viewTransport(id);
		assertEquals(TransportStateView.BOOKED, tv.getState());
		assertEquals(VALID_ORIGIN, tv.getOrigin());
		assertEquals(VALID_DESTINATION, tv.getDestination());
		assertTrue(VALID_PRICE >= tv.getPrice());
    }

	@Test
    public void listTransportsSuccess() throws Exception {
		_uddiNaming = new UDDINaming(UDDI_URL);
		List<TransportView> tvList;
		TransportView tv;
// 		TransporterClient tClient;
// 		List<JobView> jobList;
// 		JobView jv;

		_broker.getPort().requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE_ODD);
		tvList = _broker.getPort().listTransports();
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
		
		_broker.getPort().requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE_EVEN);
		tvList = _broker.getPort().listTransports();
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
        String id = _broker.getPort().requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		TransportView t = _broker.getPort().viewTransport(id);
        assertEquals(VALID_DESTINATION, t.getDestination());
        assertEquals(VALID_ORIGIN, t.getOrigin());
        assertEquals(id.split("_")[0], t.getTransporterCompany());
        assertTrue(t.getPrice() <= VALID_PRICE);
        assertEquals(TransportStateView.BOOKED, t.getState() );
        assertEquals(id, t.getId());
	}
    
	@Test
    public void viewTransportSuccessWithTimers() throws Exception {
        String id = _broker.getPort().requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		assertEquals("Returned transport state is not BOOKED", TransportStateView.BOOKED, _broker.getPort().viewTransport(id).getState());
		
		System.out.println("Testing timers...");
		Thread.sleep(15000);
	
		assertEquals("Returned transport state is not COMPLETED", TransportStateView.COMPLETED, _broker.getPort().viewTransport(id).getState());
	}
}
