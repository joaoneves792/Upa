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

    private BrokerPort broker;

    private final String BOGUS_URL = "http://this.is.a.bogus.url/service";
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


    private final String TRANSPORTER_COMPANY_PREFIX = "UpaTransporter";

    private ArrayList<String> transporterlist = new ArrayList<>();
    private JobView overpricedJob = new JobView();
    private JobView underpricedJob = new JobView();
    private JobView acceptedJob = new JobView();

    @Mocked
    private TransporterClient client;

    @Mocked
    private TransporterPortType tpt;

    @Mocked
    private UDDINaming uddi;


    @Before
    public void setUp() throws Exception {
        broker = new BrokerPort(BOGUS_URL);
        transporterlist.clear();
        transporterlist.add(TRANSPORTER_COMPANY_PREFIX + "1");
        overpricedJob.setCompanyName(TRANSPORTER_COMPANY_PREFIX + "1");
        overpricedJob.setJobDestination(VALID_DESTINATION);
        overpricedJob.setJobOrigin(VALID_ORIGIN);
        overpricedJob.setJobIdentifier("1");
        overpricedJob.setJobPrice(OVERPRICED_PRICE);
        overpricedJob.setJobState(JobStateView.PROPOSED);

        underpricedJob.setCompanyName(TRANSPORTER_COMPANY_PREFIX + "1");
        underpricedJob.setJobDestination(VALID_DESTINATION);
        underpricedJob.setJobOrigin(VALID_ORIGIN);
        underpricedJob.setJobIdentifier("2");
        underpricedJob.setJobPrice(UNDERPRICED_PRICE);
        underpricedJob.setJobState(JobStateView.PROPOSED);

        acceptedJob.setCompanyName(TRANSPORTER_COMPANY_PREFIX + "1");
        acceptedJob.setJobDestination(VALID_DESTINATION);
        acceptedJob.setJobOrigin(VALID_ORIGIN);
        acceptedJob.setJobIdentifier("2");
        acceptedJob.setJobPrice(UNDERPRICED_PRICE);
        acceptedJob.setJobState(JobStateView.ACCEPTED);
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
        broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, INVALID_PRICE);
    }

    @Test(expected = UnknownLocationFault_Exception.class)
    public void requestTransportInvalidOrigin() throws Exception {
        broker.requestTransport(INVALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnknownLocationFault_Exception.class)
    public void requestTransportInvalidDestination() throws Exception {
        broker.requestTransport(VALID_ORIGIN, INVALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnknownLocationFault_Exception.class)
    public void requestTransportInvalidOriginAndDestination() throws Exception {
        broker.requestTransport(INVALID_ORIGIN, INVALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportUDDIUnavailable() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (new JAXRException());
            }
        };
        broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportUDDIListFailed() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (uddi);

                uddi.list(TRANSPORTER_COMPANY_PREFIX + "*");
                result = (new JAXRException());
            }
        };
        broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportTransporterNoneAvailable() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (uddi);

                uddi.list(TRANSPORTER_COMPANY_PREFIX + "*");
                result = (new ArrayList<String>());
            }
        };
        broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportTransporterUnavailable() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (uddi);

                uddi.list(TRANSPORTER_COMPANY_PREFIX + "*");
                result = (transporterlist);

                new TransporterClient(BOGUS_URL, (String) any);
                result = (client);

                client.getPort();
                result = (tpt);

                tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (new JAXRException());

            }
        };
        broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportNoneAvailableBadLocation() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (uddi);

                uddi.list(TRANSPORTER_COMPANY_PREFIX + "*");
                result = (transporterlist);

                new TransporterClient(BOGUS_URL, (String) any);
                result = (client);

                client.getPort();
                result = (tpt);

                tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (new BadLocationFault_Exception("", new BadLocationFault()));

            }
        };
        broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportNoneAvailableBadPrice() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (uddi);

                uddi.list(TRANSPORTER_COMPANY_PREFIX + "*");
                result = (transporterlist);

                new TransporterClient(BOGUS_URL, (String) any);
                result = (client);

                client.getPort();
                result = (tpt);

                tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (new BadPriceFault_Exception("", new BadPriceFault()));

            }
        };
        broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnavailableTransportPriceFault_Exception.class)
    public void requestTransportPriceTooHigh() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (uddi);

                uddi.list(TRANSPORTER_COMPANY_PREFIX + "*");
                result = (transporterlist);

                new TransporterClient(BOGUS_URL, (String) any);
                result = (client);

                client.getPort();
                result = (tpt);

                tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (overpricedJob);

            }
        };
        broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }

    @Test
    public void requestTransportFailedBookBadJob() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (uddi);

                uddi.list(TRANSPORTER_COMPANY_PREFIX + "*");
                result = (transporterlist);

                new TransporterClient(BOGUS_URL, (String) any);
                result = (client);

                client.getPort();
                result = (tpt);

                tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (underpricedJob);

                tpt.decideJob((String) any, true);
                result = (new BadJobFault_Exception("", new BadJobFault()));

            }
        };
        assertEquals("Booking should have failed", broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE), FAILED_JOB);
    }

    @Test
    public void requestTransportFailedBookNullJob() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (uddi);

                uddi.list(TRANSPORTER_COMPANY_PREFIX + "*");
                result = (transporterlist);

                new TransporterClient(BOGUS_URL, (String) any);
                result = (client);

                client.getPort();
                result = (tpt);

                tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (underpricedJob);

                tpt.decideJob((String) any, true);
                result = (null);

            }
        };
        assertEquals("Booking should have failed", broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE), FAILED_JOB);
    }

    @Test
    public void requestTransportFailedBookTransporterConnectionFailed() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (uddi);

                uddi.list(TRANSPORTER_COMPANY_PREFIX + "*");
                result = (transporterlist);

                new TransporterClient(BOGUS_URL, (String) any);
                result = (client);

                client.getPort();
                result = (tpt);

                tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (underpricedJob);

                tpt.decideJob((String) any, true);
                result = (new JAXRException());

            }
        };
        assertEquals("Booking should have failed", broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE), FAILED_JOB);
    }

    @Test
    public void requestTransportSuccess() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (uddi);

                uddi.list(TRANSPORTER_COMPANY_PREFIX + "*");
                result = (transporterlist);

                new TransporterClient(BOGUS_URL, (String) any);
                result = (client);

                client.getPort();
                result = (tpt);

                tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (underpricedJob);

                tpt.decideJob((String) any, true);
                result = (acceptedJob);

            }
        };
        assertEquals("Booking should have failed", broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE), BOOKED_JOB);
    }


    /*
    listTransport Test cases
    */

    @Test
    public void listTransportsSuccess() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (uddi);

                uddi.list(TRANSPORTER_COMPANY_PREFIX + "*");
                result = (transporterlist);

                new TransporterClient(BOGUS_URL, (String) any);
                result = (client);

                client.getPort();
                result = (tpt);

                tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (underpricedJob);

                tpt.decideJob((String) any, true);
                result = (acceptedJob);

            }
        };
        broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        List<TransportView> tl = broker.listTransports();
        assertEquals("Should only have one job", tl.size(), 1);
        assertEquals(tl.get(0).getDestination(), VALID_DESTINATION);
        assertEquals(tl.get(0).getOrigin(), VALID_ORIGIN);
        assertEquals(tl.get(0).getTransporterCompany(), TRANSPORTER_COMPANY_PREFIX + "1");
        assertTrue(tl.get(0).getPrice() <= VALID_PRICE);
        assertEquals(tl.get(0).getState(), TransportStateView.BOOKED);

        broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        tl = broker.listTransports();
        assertEquals("Should have two jobs", tl.size(), 2);
        assertEquals(tl.get(1).getDestination(), VALID_DESTINATION);
        assertEquals(tl.get(1).getOrigin(), VALID_ORIGIN);
        assertEquals(tl.get(1).getTransporterCompany(), TRANSPORTER_COMPANY_PREFIX + "1");
        assertTrue(tl.get(1).getPrice() <= VALID_PRICE);
        assertEquals(tl.get(1).getState(), TransportStateView.BOOKED);
    }

    @Test
    public void listTransportsEmptyOnStart() {
        assertTrue("List not empty", broker.listTransports().isEmpty());
    }

    /*
    clearTransports test cases
     */

    @Test
    public void clearTransportsSuccess() throws Exception {
        new Expectations() {
            {
                new UDDINaming((String) any);
                result = (uddi);

                uddi.list(TRANSPORTER_COMPANY_PREFIX + "*");
                result = (transporterlist);

                new TransporterClient(BOGUS_URL, (String) any);
                result = (client);

                client.getPort();
                result = (tpt);

                tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result = (underpricedJob);

                tpt.decideJob((String) any, true);
                result = (acceptedJob);

            }
        };
        broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
        List<TransportView> tl = broker.listTransports();
        assertEquals(tl.size(), 2);
        broker.clearTransports();
        tl = broker.listTransports();
        assertEquals(tl.size(), 0);
    }

}