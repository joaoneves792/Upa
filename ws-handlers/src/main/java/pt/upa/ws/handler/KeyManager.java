package pt.upa.ws.handler;

import pt.upa.a45.CA.cli.CAClient;
import pt.upa.a45.CA.cli.CAException;

import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by joao on 5/4/16.
 */
public class KeyManager {

	private static final char[] PASSWORD = "123456".toCharArray();
	private static final String UDDI_URL = "http://localhost:9090"; /*This shouldnt be a constant, but I dont see any other way (First incoming message)*/
	private static final String CACERT_FILENAME = "cacert.pem";
	private static final String CACERT = "cacert";
	private static final String MYKEY = "mykey";

	private static KeyStore _ks;
	private static CAClient _ca;

	private static Hashtable<String, X509Certificate> _certCache = new Hashtable<>();

	private static KeyManager instance = null;



	private Map<String, String> _brokerRecievedNonces = new TreeMap<String, String>();

    

    public void addNounce(String nounce) {
		_brokerRecievedNonces.put(nounce, nounce);
    }
    
    public boolean containsNounce(String nounce) {
		return _brokerRecievedNonces.containsKey(nounce);
    }
    
    

    private KeyManager(String keystore){
        _ks = loadKeystore(keystore, PASSWORD);
        initializeCAClient();
        loadCACertificate();
//         initNounceMaps();
    }

    private KeyManager(){
        initializeCAClient();
        loadCACertificate();
//         initNounceMaps();
    }

    public static KeyManager getInstance(String keystore){
        if(null == instance)
            if(null != keystore)
                instance = new KeyManager(keystore);
            else
                instance = new KeyManager();
        else if(null == _ks && null != keystore)
            _ks = loadKeystore(keystore, PASSWORD);
        return instance;
    }

    private static KeyStore loadKeystore(String name, char[] password){
        System.out.println("[Handler]: Loading keystore for: "+ name);
		
        FileInputStream fis;
        String filename = name + ".jks";
        try {
            fis = new FileInputStream(filename);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(fis, password);
            return keystore;
        } catch (FileNotFoundException e) {
            System.err.println("Keystore file <" + filename + "> not fount.");
            System.exit(-1);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e){
            System.err.println("Failed to load the Keystore" + e.getMessage());
            System.exit(-1);
        }
        return null;
    }
    private static void initializeCAClient(){
        try {
            _ca = new CAClient(UDDI_URL);
        } catch (CAException e) {
            System.err.println("Failed to initializeKeyStore the CA client " + e.getMessage());
            System.exit(-1);
        }

    }

    public PrivateKey getMyPrivateKey() throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException{
        KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(PASSWORD);
        return ((KeyStore.PrivateKeyEntry)(_ks.getEntry(MYKEY, protParam))).getPrivateKey();
    }

    public X509Certificate getCertificate(String entity)throws CAException, CertificateException, SignatureException{
        if(_certCache.containsKey(entity)){
            X509Certificate cert = _certCache.get(entity);
            cert.checkValidity();
            return cert;
        }

        return forceCertificateRefresh(entity);
    }

    public X509Certificate forceCertificateRefresh(String entity)throws CAException, CertificateException, SignatureException{
        X509Certificate cert = _ca.getCertificate(entity);
        verifyCertificate(cert);

        _certCache.put(entity, cert);
        return cert;
    }

    public X509Certificate getCACertificate(){
        return _certCache.get(CACERT);
    }

    private static void loadCACertificate(){
        try{
            X509Certificate cacert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new FileInputStream(CACERT_FILENAME));
            cacert.checkValidity();
            _certCache.put(CACERT, cacert);
        }catch(CertificateException | FileNotFoundException e){
            System.err.println("Failed to load the CA root certificate, error: "+ e.toString() + e.getMessage());
            System.exit(-1);
        }

    }

    public void verifyCertificate(X509Certificate cert)throws CertificateException, SignatureException{
        cert.checkValidity();
        try {
            cert.verify(getCACertificate().getPublicKey());
        }catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e){
            System.err.println("FATAL: Can't verify the signature on a certificate: " + e.getMessage());
            System.exit(-1);
        }
    }
}
