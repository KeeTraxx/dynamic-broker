package ch.puzzle.activemq.broker;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class CustomTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        // TODO: Proper implementation
        if ( s == null ) {
            throw new CertificateException("checkClientTrusted!");
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        // TODO: Proper implementation
        if ( s == null ) {
            throw new CertificateException("checkServerTrusted!");
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        // TODO: Implementation. Should get trusted issuers from a Keystore
        return new X509Certificate[0];
    }
}
