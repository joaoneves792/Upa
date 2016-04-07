package pt.upa.transporter.ws;

import org.junit.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import javax.xml.registry.JAXRException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNull;

public class TransporterPortTest {
	private final String TRANSPORTER_COMPANY_PREFIX = "UpaTransporter";
	private final String INVALID_ID = "-1";
	private final String VALID_ORIGIN = "Lisboa";
    private final String VALID_DESTINATION = "Leiria";

    private final int VALID_PRICE = 50;
	
    private TransporterPort _transporter;
    

    @Before
    public void setUp() {
   		_transporter = new TransporterPort(1); //random id
    }

    @After
    public void tearDown() {
    	// empty for now
    }

	/*
	jobStatus test cases
	*/

    @Test
	public void jobStatusEmptyList() throws Exception {
		_transporter.jobStatus(INVALID_ID);
	}

    @Test
	public void jobStatusInvalidId() throws Exception {
        _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		assertNull("Should return null", _transporter.jobStatus(INVALID_ID));
	}
	
	@Test
	public void jobStatusSucess() throws Exception {
		JobView expectedJob = _transporter.requestJob(VALID_ORIGIN, VALID_DESTINATION, VALID_PRICE);
		assertSame("Returned jobView is not correct", _transporter.jobStatus("0"), expectedJob);

	}
	
}