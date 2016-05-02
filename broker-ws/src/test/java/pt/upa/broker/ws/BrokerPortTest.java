package pt.upa.broker.ws;

import mockit.Expectations;
import mockit.Mocked;
import org.junit.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.TransporterClient;
import pt.upa.transporter.ws.cli.TransporterClientException;

import javax.xml.registry.JAXRException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BrokerPortTest {
    private final String TRANSPORTER_COMPANY_PREFIX = "UpaTransporter";

    private final String BOGUS_URL = "http://this.is.a.bogus.url:9090";
    private final String VALID_ORIGIN = "Lisboa";
    private final String VALID_DESTINATION = "Leiria";
    private final String INVALID_ORIGIN = "Alameda";
    private final String INVALID_DESTINATION = "Porto Salvo";

    private final int VALID_PRICE = 100;
    private final int INVALID_PRICE = -1;
    private final int OVERPRICED_PRICE = 101;
    private final int UNDERPRICED_PRICE = 99;

    private final String VALID_ID = TRANSPORTER_COMPANY_PREFIX + "1_0";
    private final String INVALID_ID = TRANSPORTER_COMPANY_PREFIX + "1_-1";

    private BrokerPort _broker;

    private ArrayList<String> _transporterList = new ArrayList<>();
    private JobView _overpricedJob = new JobView();
    private JobView _underpricedJob = new JobView();
    private JobView _acceptedJob = new JobView();
    private JobView _failedJob = new JobView();

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
        _overpricedJob.setJobIdentifier("0");
        _overpricedJob.setJobPrice(OVERPRICED_PRICE);
        _overpricedJob.setJobState(JobStateView.PROPOSED);

        _underpricedJob.setCompanyName(TRANSPORTER_COMPANY_PREFIX + "1");
        _underpricedJob.setJobDestination(VALID_DESTINATION);
        _underpricedJob.setJobOrigin(VALID_ORIGIN);
        _underpricedJob.setJobIdentifier("1");
        _underpricedJob.setJobPrice(UNDERPRICED_PRICE);
        _underpricedJob.setJobState(JobStateView.PROPOSED);

        _acceptedJob.setCompanyName(TRANSPORTER_COMPANY_PREFIX + "1");
        _acceptedJob.setJobDestination(VALID_DESTINATION);
        _acceptedJob.setJobOrigin(VALID_ORIGIN);
        _acceptedJob.setJobIdentifier("1");
        _acceptedJob.setJobPrice(UNDERPRICED_PRICE);
        _acceptedJob.setJobState(JobStateView.ACCEPTED);

        _failedJob.setCompanyName(TRANSPORTER_COMPANY_PREFIX + "1");
        _failedJob.setJobDestination(VALID_DESTINATION);
        _failedJob.setJobOrigin(VALID_ORIGIN);
        _failedJob.setJobIdentifier("1");
        _failedJob.setJobPrice(UNDERPRICED_PRICE);
        _failedJob.setJobState(JobStateView.REJECTED);
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

                _tpt.jobStatus((String) any);
                result = _failedJob;
            }
        };
        String id = _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        assertEquals("Booking should have failed", TransportStateView.FAILED, _broker.viewTransport(id).getState());
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

                _tpt.jobStatus((String) any);
                result = _failedJob;

            }
        };
        String id = _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        assertEquals("Booking should have failed", TransportStateView.FAILED, _broker.viewTransport(id).getState());
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
//                 result = (new JAXRException());
                result = (new TransporterClientException("message"));

                _tpt.jobStatus((String) any);
//                 result = new JAXRException();
                result = new TransporterClientException("message");

            }
        };
        String id = _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        assertEquals("Booking should have failed", TransportStateView.FAILED, _broker.viewTransport(id).getState());
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

                _tpt.jobStatus((String) any);
                result = (_acceptedJob);

            }
        };
        String id = _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        assertEquals("Booking should not have failed", TransportStateView.BOOKED, _broker.viewTransport(id).getState());
    }

	/*
	viewTransport Test cases
	*/

    @Test(expected = UnknownTransportFault_Exception.class)
	public void viewTransportEmptyList() throws Exception {
		_broker.viewTransport(VALID_ID);
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
        String id = _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		_broker.viewTransport(id);
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
        String id = _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		TransportView t = _broker.viewTransport(id);
        assertEquals(VALID_DESTINATION, t.getDestination());
        assertEquals(VALID_ORIGIN, t.getOrigin());
        assertEquals(TRANSPORTER_COMPANY_PREFIX + "1", t.getTransporterCompany());
        assertTrue(t.getPrice() <= VALID_PRICE);
        assertEquals(TransportStateView.BOOKED, t.getState() );
        assertEquals(id, t.getId());
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
        assertEquals("Should only have one job", 1, tl.size());
        assertEquals(VALID_DESTINATION, tl.get(0).getDestination());
        assertEquals(VALID_ORIGIN, tl.get(0).getOrigin());
        assertEquals(TRANSPORTER_COMPANY_PREFIX + "1", tl.get(0).getTransporterCompany());
        assertTrue(tl.get(0).getPrice() <= VALID_PRICE);
        assertEquals(TransportStateView.BOOKED, tl.get(0).getState());

        _broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        tl = _broker.listTransports();
        assertEquals("Should have tow jobs", 2, tl.size());
        assertEquals(VALID_DESTINATION, tl.get(1).getDestination());
        assertEquals(VALID_ORIGIN, tl.get(1).getOrigin());
        assertEquals(TRANSPORTER_COMPANY_PREFIX + "1", tl.get(1).getTransporterCompany());
        assertTrue(tl.get(1).getPrice() <= VALID_PRICE);
        assertEquals(TransportStateView.BOOKED, tl.get(1).getState());
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
        assertEquals(2, tl.size());
        _broker.clearTransports();
        tl = _broker.listTransports();
        assertEquals(0, tl.size());
    }

}