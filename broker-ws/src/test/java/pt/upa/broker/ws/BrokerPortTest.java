package pt.upa.broker.ws;

import mockit.Expectations;
import mockit.Mocked;
import org.junit.*;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.cli.TransporterClient;

import javax.xml.registry.JAXRException;

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


    @Mocked
    TransporterClient client;

    @Mocked
    TransporterPortType tpt;

    @Before
    public void setUp() throws Exception {
        broker = new BrokerPort(BOGUS_URL);
    }

    @After
    public void tearDown() {
        //Empty for now
    }

    @Test
    public void emptyListOnStartTest(){
        assertTrue("List not empty", broker.listTransports().isEmpty());
    }

    @Test(expected = InvalidPriceFault_Exception.class)
    public void requestTransportNegativePrice()throws Exception{
        broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, INVALID_PRICE);
    }

    @Test(expected = UnknownLocationFault_Exception.class)
    public void requestTransportInvalidOrigin()throws Exception{
        broker.requestTransport(INVALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnknownLocationFault_Exception.class)
    public void requestTransportInvalidDestination()throws Exception{
        broker.requestTransport(VALID_ORIGIN, INVALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnknownLocationFault_Exception.class)
    public void requestTransportInvalidOriginAndDestination()throws Exception{
        broker.requestTransport(INVALID_ORIGIN, INVALID_DESTINATION, VALID_PRICE);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void requestTransportNoneAvailable()throws Exception{
        new Expectations(){
            {
                new TransporterClient(BOGUS_URL, (String) any);
                result=(client);

                client.getPort();
                result=(tpt);

                tpt.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
                result=(new JAXRException());

            }
        };
        broker.requestTransport(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
    }
}
