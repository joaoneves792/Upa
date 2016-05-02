package pt.upa.broker.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import pt.upa.broker.ws.TransportView;

public class ListTransportsIT extends AbstractIT {

	// tests
	// assertEquals(expected, actual);

	// public List<TransportView> listTransports();

	@Test
	public void testListTransports() throws Exception {
		PORT.clearTransports();// To start fresh
		String j1 = PORT.requestTransport(SOUTH_1, CENTER_1, PRICE_SMALLEST_LIMIT);
		String j2 = PORT.requestTransport(NORTH_1, CENTER_1, PRICE_SMALLEST_LIMIT);
		String j3 = PORT.requestTransport(CENTER_1, CENTER_2, PRICE_SMALLEST_LIMIT);
		TransportView jtv1 = PORT.viewTransport(j1);
		TransportView jtv2 = PORT.viewTransport(j2);
		PORT.viewTransport(j3);

		List<TransportView> tList = PORT.listTransports();
		assertEquals(3, tList.size());

		TransportView tv1 = tList.get(0);
		assertTrue(new Boolean((jtv1.getId().equals(tv1.getId())) && jtv1.getOrigin().equals(tv1.getOrigin())
				&& jtv1.getDestination().equals(tv1.getDestination()) && jtv1.getPrice().equals(tv1.getPrice())
				&& jtv1.getState().toString().equals(tv1.getState().toString())));
		TransportView tv2 = tList.get(1);
		assertTrue(new Boolean((jtv2.getId().equals(tv2.getId())) && jtv2.getOrigin().equals(tv2.getOrigin())
				&& jtv2.getDestination().equals(tv2.getDestination()) && jtv2.getPrice().equals(tv2.getPrice())
				&& jtv2.getState().toString().equals(tv2.getState().toString())));
	}

}
