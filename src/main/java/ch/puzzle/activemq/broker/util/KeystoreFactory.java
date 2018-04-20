package ch.puzzle.activemq.broker.util;

import sun.security.x509.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

public class KeystoreFactory {
    public static final char[] DEFAULT_PASSWORD = "123456".toCharArray();

    public static KeyStore generateKeyStore() {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            // Set Keystore Password
            ks.load(null, DEFAULT_PASSWORD);

            // Generate a Key Pair for encryption and certificate
            KeyPair kp = KeyPairGenerator.getInstance("RSA").generateKeyPair();

            Certificate c = generateCertificate("CN=CustomKeyManagerGeneratedCertificate, L=Bern, C=CH", kp, 36500, "SHA256withRSA");

            // Put certificate in keystore
            ks.setCertificateEntry("server-cert", c);

            // Put private key in keystore
            ks.setKeyEntry("server-key", kp.getPrivate(), DEFAULT_PASSWORD, new Certificate[]{c});

            // Optional... for debugging and analyzing with KeyStore Explorer
            // Saves keystore in a file
            return ks;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Well... you landed in Nirvana now.");
        }
    }

    private static X509Certificate generateCertificate(String dn, KeyPair pair, int days, String algorithm)
            throws GeneralSecurityException, IOException {
        // TODO: Use bouncycastle to generate certificate...
        // Sun stuff is proprietary
        PrivateKey privkey = pair.getPrivate();
        X509CertInfo info = new X509CertInfo();
        Date from = new Date();
        Date to = new Date(from.getTime() + days * 86400000L);
        CertificateValidity interval = new CertificateValidity(from, to);
        BigInteger sn = new BigInteger(64, new SecureRandom());
        X500Name owner = new X500Name(dn);

        info.set("validity", interval);
        info.set("serialNumber", new CertificateSerialNumber(sn));
        info.set("subject", owner);
        info.set("issuer", owner);
        info.set("key", new CertificateX509Key(pair.getPublic()));
        info.set("version", new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(privkey, algorithm);

        // Update the algorith, and resign.
        algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
        cert = new X509CertImpl(info);
        cert.sign(privkey, algorithm);
        return cert;
    }
}
