package pt.upa.a45.CA;

import sun.security.pkcs10.PKCS10;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Created by joao on 4/27/16.
 */

/*
    This class is only intended to be used during the initial setup of the keys
    all methods are throwing Exception on purpose
 */
public class KeyGenerator {

    private static final String CA_PRIVATE_KEY = "CAPRIVATEKEY";
    private static final String PASSWORD = "";

    private static final String COMMONNAME = "UpaCA";
    private static final String ORGANIZATIONAL_UNIT = "SD";
    private static final String ORGANIZATION = "IST";
    private static final String CITY = "Lisbon";
    private static final String STATE = "Lisbon";
    private static final String COUNTRY = "Portugal";

    private static final int KEY_SIZE = 2048;
    private static final long VALIDITY = 365;


    public static void main(String args[])throws Exception{
        if(args[0].equals("CA")){
            generateCAKeys();
        }else{
            generateSignedCert(args[0]);
        }
    }

    private static void generateSignedCert(String commonName)throws Exception{

        //Generate the Keys and prepare the certificate request
        CertAndKeyGen keypair = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
        X500Name x500Name = new X500Name(commonName, ORGANIZATIONAL_UNIT, ORGANIZATION, CITY, STATE, COUNTRY);
        keypair.generate(KEY_SIZE);
        PrivateKey privKey = keypair.getPrivateKey();

        PKCS10 CR = keypair.getCertRequest(x500Name);


        KeyStore ks = readKeystoreFile("key.store", PASSWORD.toCharArray());

        KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(PASSWORD.toCharArray());
        KeyStore.PrivateKeyEntry privKeyEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(CA_PRIVATE_KEY, protParam);

        Signature signature = Signature.getInstance("SHA1WithRSA");
        signature.initSign(privKeyEntry.getPrivateKey());

        CR.encodeAndSign(x500Name, signature);


        //Certificate cacert = loadCertificateFromFile("cacert.cer");
        //X509Certificate[] chain = new X509Certificate[2];

        FileOutputStream fos = new FileOutputStream(commonName + ".cer");
        fos.write(CR.getEncoded());
        fos.flush();
        fos.close();
    }

    public static Certificate loadCertificateFromFile(String filename) throws CertificateException, IOException {
        InputStream is = new FileInputStream(filename);
        BufferedInputStream bis = new BufferedInputStream(is);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        Certificate cert = null;

        if (bis.available() > 0) {
            cert = cf.generateCertificate(bis);
        }
        return cert;
    }

    public static KeyStore readKeystoreFile(String keyStoreFilePath, char[] keyStorePassword) throws Exception {
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

    private static void generateCAKeys() throws Exception{
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null);

        CertAndKeyGen keypair = new CertAndKeyGen("RSA", "SHA1WithRSA", null);

        X500Name x500Name = new X500Name(COMMONNAME, ORGANIZATIONAL_UNIT, ORGANIZATION, CITY, STATE, COUNTRY);

        keypair.generate(KEY_SIZE);
        PrivateKey privKey = keypair.getPrivateKey();

        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = keypair.getSelfCertificate(x500Name, new Date(), VALIDITY * 24 * 60 * 60);

        KeyStore.PrivateKeyEntry privKeyEntry = new KeyStore.PrivateKeyEntry(privKey, chain);
        KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(PASSWORD.toCharArray());
        ks.setEntry(CA_PRIVATE_KEY, privKeyEntry, protParam);

        FileOutputStream fos = new FileOutputStream("cacert.cer");
        fos.write(chain[0].getEncoded());
        fos.flush();
        fos.close();

        fos = new FileOutputStream("key.store");
        ks.store(fos, PASSWORD.toCharArray());
    }

}
