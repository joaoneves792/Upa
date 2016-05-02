package pt.upa.broker.ws.it;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pt.upa.broker.ws.TransportStateView;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

public class ViewTransportIT extends AbstractIT {

	// public TransportView viewTransport(String id)
	// throws UnknownTransportFault_Exception

	@Test
	public void testTransportStateTransition() throws Exception {
	  List<TransportStateView> tS = new ArrayList<>();

	  tS.add(TransportStateView.HEADING);
	  tS.add(TransportStateView.ONGOING);
	  tS.add(TransportStateView.COMPLETED);

	  String rt = PORT.requestTransport(CENTER_1, SOUTH_1, PRICE_SMALLEST_LIMIT);
	  TransportView vt = PORT.viewTransport(rt);
	  assertEquals(vt.getState(), TransportStateView.BOOKED);

	  for (int t = 0; t <= 3 * DELAY_UPPER || !tS.isEmpty(); t += TENTH_OF_SECOND) {
	    Thread.sleep(TENTH_OF_SECOND);
	    vt = PORT.viewTransport(rt);
	    if (tS.contains(vt.getState()))
	      tS.remove(vt.getState());
	  }
	  assertEquals(0, tS.size());
	}

	@Test(expected = UnknownTransportFault_Exception.class)
	public void testViewInvalidTransport() throws Exception {
		PORT.viewTransport(null);
	}

	@Test(expected = UnknownTransportFault_Exception.class)
	public void testViewNullTransport() throws Exception {
		PORT.viewTransport(EMPTY_STRING);
	}

}
