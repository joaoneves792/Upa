package pt.upa.broker.ws;

import mockit.Expectations;
import mockit.Mocked;
import org.junit.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.TransporterClient;

import javax.xml.registry.JAXRException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by joao on 4/4/16.
 */
public class BrokerPortTest {


    private final String BOGUS_URL = "http://this.is.a.bogus.url:9090";
    private final String VALID_ORIGIN = "Lisboa";
    private final String VALID_DESTINATION = "Leiria";
    private final String INVALID_ORIGIN = "Alameda";
    private final String INVALID_DESTINATION = "Porto Salvo";

    private final int VALID_PRICE = 100;
    private final int INVALID_PRICE = -1;
    private final int OVERPRICED_PRICE = 101;
    private final int UNDERPRICED_PRICE = 99;

    private final String FAILED_JOB = "FAILED";
    private final String BOOKED_JOB = "BOOKED";
    private final String INVALID_ID = "-1";

    private final String TRANSPORTER_COMPANY_PREFIX = "UpaTransporter";


    private BrokerPort _broker;

    private ArrayList<String> _transporterList = new ArrayList<>();
    private JobView _overpricedJob = new JobView();
    private JobView _underpricedJob = new JobView();
    private JobView _acceptedJob = new JobView();

    @Mocked
    private TransporterClient _client;
    @Mocked
    private TransporterPortType _tpt;
    @Mocked
    private UDDINaming _uddi;


    @Before
    public void setUp() throws Exception {
        _broker = new BrokerPort(BOGUS_URL);
        _transporterList.clear();
        _transporterList.add(TRANSPORTER_COMPANY_PREFIX + "1");
        _overpricedJob.setCompanyName(TRANSPORTER_COMPANY_PREFIX + "1");
        _overpricedJob.setJobDestination(VALID_DESTINATION);
        _overpricedJob.setJobOrigin(VALID_ORIGIN);
        _overpricedJob.setJobIdentifier("1");
        _overpricedJob.setJobPrice(OVERPRICED_PRICE);
        _overpricedJob.setJobState(JobStateView.PROPOSED);

        _underpricedJob.setCompanyName(TRANSPORTER_COMPANY_PREFIX + "1");
        _underpricedJob.setJobDestination(VALID_DESTINATION);
        _underpricedJob.setJobOrigin(VALID_ORIGIN);
        _underpricedJob.setJobIdentifier("2");
        _underpricedJob.setJobPrice(UNDERPRICED_PRICE);
        _underpricedJob.setJobState(JobStateView.PROPOSED);

        _acceptedJob.setCompanyName(TRANSPORTER_COMPANY_PREFIX + "1");
        _acceptedJob.setJobDestination(VALID_DESTINATION);
        _acceptedJob.setJobOrigin(VALID_ORIGIN);
        _acceptedJob.setJobIdentifier("2");
        _acceptedJob.setJobPrice(UNDERPRICED_PRICE);
        _acceptedJob.setJobState(JobStateView.ACCEPTED);
    }

    @After
    public void tearDown() {
        //Empty for now
    }


    /*
    requestTransport test cases
     */

    @Test(expected = InvalidPriceFault_Exception.class)
    public void requestTransportNegativePrice() throws Exception {
        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, INVALID_PRICE);
    }

    @Test(expected = UnknownLocationFault_Exception.class)
    public void requestTransportInvalidOrigin() throws Exception {
        _broker.requestTransport(INVALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnknownLocationFault_Exception.class)
    public void requestTransportInvalidDestination() throws Exception {
        _broker.requestTransport(VALID_ORIGIN, INVALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnknownLocationFault_Exception.class)
    public void requestTransportInvalidOriginAndDestination() throws Exception {
        _broker.requestTransport(INVALID_ORIGIN, INVALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportUDDIUnavailable() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (new JAXRException());
            }
        };
        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportUDDIListFailed() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (_uddi);

                _uddi.list(TRANSPORTER_COMPANY_PREFIX + "_");
                result = (new JAXRException());
            }
        };
        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportTransporterNoneAvailable() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (_uddi);

                _uddi.list(TRANSPORTER_COMPANY_PREFIX + "_");
                result = (new ArrayList<String>());
            }
        };
        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportTransporterUnavailable() throws Exception {
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
                result = (new JAXRException());

            }
        };
        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }

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
    public void requestTransportFailedBookBadJob() throws Exception {
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
                result = (new BadJobFault_Exception("", new BadJobFault()));

            }
        };
        assertEquals("Booking should have failed", _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE), FAILED_JOB);
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
    public void requestTransportFailedBookTransporterConnectionFailed() throws Exception {
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
                result = (new JAXRException());

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

	/*
	viewTransport Test cases
	*/

    @Test(expected = UnknownTransportFault_Exception.class)
	public void viewTransportEmptyList() throws Exception {
		_broker.viewTransport("0");
	}

    @Test(expected = UnknownTransportFault_Exception.class)
	public void viewTransportInvalidBrokerId() throws Exception {
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
		_broker.viewTransport(INVALID_ID);
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

	@Test
	public void viewTransportSucess() throws Exception {
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
                result = (_acceptedJob);
            }
        };
        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		TransportView t = _broker.viewTransport("0");
        assertEquals(t.getDestination(), VALID_DESTINATION);
        assertEquals(t.getOrigin(), VALID_ORIGIN);
        assertEquals(t.getTransporterCompany(), TRANSPORTER_COMPANY_PREFIX + "1");
        assertTrue(t.getPrice() <= VALID_PRICE);
        assertEquals(t.getState(), TransportStateView.BOOKED);
        assertEquals(t.getId(), "0");
	}
	
    /*
    listTransport Test cases
    */

    @Test
    public void listTransportsEmptyOnStart() throws Exception {
        assertTrue("List not empty", _broker.listTransports().isEmpty());
    }

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

    /*
    clearTransports test cases
     */

    @Test
    public void clearTransportsSuccess() throws Exception {
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
        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        List<TransportView> tl = _broker.listTransports();
        assertEquals(tl.size(), 2);
        _broker.clearTransports();
        tl = _broker.listTransports();
        assertEquals(tl.size(), 0);
    }

}