package pt.upa.a45.CA;

import javax.jws.WebService;
import java.io.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * Created by joao on 5/2/16.
 */
@WebService(endpointInterface = "pt.upa.a45.CA.CA")
public class CAImpl implements CA{

    private final String KEYSTORE_FILENAME = "CA.jks";
    private final char[] PASSWORD = "123456".toCharArray();

    private KeyStore _keyStore;

    CAImpl(){
        try{
            _keyStore = loadKeystore(KEYSTORE_FILENAME, PASSWORD);
        }catch(KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e){
            System.out.println("Unable to load the keystore! " + e.getMessage());
            System.exit(-1); //No point to keep going...
        }
    }

    public static KeyStore loadKeystore(String keyStoreFilePath, char[] keyStorePassword)throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException{
        FileInputStream fis;
        try {
            fis = new FileInputStream(keyStoreFilePath);
        } catch (FileNotFoundException e) {
            System.err.println("Keystore file <" + keyStoreFilePath + "> not fount.");
            return null;
        }
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(fis, keyStorePassword);
        return keystore;
    }

    public byte[] getCertificate(String entity)throws CAException{
        X509Certificate cert = null;
        try {
            cert = (X509Certificate)_keyStore.getCertificate(entity);
        }catch (KeyStoreException e){
            throw new CAException("There is no certificate for the requested entity: " + entity);
        }

        if (null != cert) {
            byte[] data = certificateToByteArray(cert);
            if(null != data)
                return data;
            else
                throw new CAException("Failed to convert certificate for sending");
        }else
            throw new CAException("There is no certificate for the requested entity: " + entity);
    }

    private byte[] certificateToByteArray(X509Certificate cert){
        try {
            return cert.getEncoded();
        }catch (CertificateEncodingException e){
            return null;
        }
    }
}
