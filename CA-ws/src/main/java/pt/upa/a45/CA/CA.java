package pt.upa.a45.CA;

import javax.jws.WebService;
import java.security.cert.X509Certificate;

/**
 * Created by joao on 5/2/16.
 */
@WebService
public interface CA {
    X509Certificate getCertificate(String entity)throws CAException;
}
