package pt.upa.a45.CA.cli.it;

import pt.upa.a45.CA.cli.CAClient;
import pt.upa.a45.CA.cli.CAException;

import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;

public class CAITTest {
    private final String UDDI_URL = "http://localhost:9090";

    private final String TRANSPORTER_COMPANY_PREFIX = "UpaTransporter";
    private final String BROKER = "UpaBroker";
    private final String BROKER_DN = "CN=UpaBroker, OU=SD, O=IST, C=PT";
    private final String CA_DN = "CN=UpaCA, OU=SD, O=IST, C=PT";
    private final String TRANSPORTER1_DN = "CN=UpaTransporter1, OU=SD, O=IST, C=PT";

    private final String NON_EXISTING_ENTITY = "IdontExist";

    private CAClient _ca;

    @org.junit.Before
    public void setUp() throws Exception {
        _ca = new CAClient(UDDI_URL);
    }

    @After
    public void tearDown() {
        //Empty for now
    }

    @Test()
    public void getBrokerCert() throws Exception {
        X509Certificate cert = _ca.getCertificate(BROKER);
        cert.checkValidity();
        assertEquals(BROKER_DN, cert.getSubjectDN().toString());
        assertEquals(CA_DN, cert.getIssuerDN().toString());
    }

    @Test()
    public void getTransporterCert() throws Exception {
        X509Certificate cert = _ca.getCertificate(TRANSPORTER_COMPANY_PREFIX+"1");
        cert.checkValidity();
        assertEquals(TRANSPORTER1_DN, cert.getSubjectDN().toString());
        assertEquals(CA_DN, cert.getIssuerDN().toString());
    }

    @Test(expected = CAException.class)
    public void getUnknownCert()throws Exception{
        _ca.getCertificate(NON_EXISTING_ENTITY);
    }

}