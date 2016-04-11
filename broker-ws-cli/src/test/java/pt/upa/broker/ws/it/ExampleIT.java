package pt.upa.broker.ws.it;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *  Integration Test example
 *  
 *  Invoked by Maven in the "verify" life-cycle phase
 *  Should invoke "live" remote servers 
 */
public class ExampleIT {

    // static members


    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() {

    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members


    // initialization and clean-up for each test

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    // tests

    @Test
    public void pingSuccess() {

        // assertEquals(expected, actual);
        // if the assert fails, the test fails
    }
    
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

	
    

}