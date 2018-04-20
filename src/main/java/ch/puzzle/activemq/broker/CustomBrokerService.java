package ch.puzzle.activemq.broker;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.SslContext;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class CustomBrokerService extends BrokerService {

    public CustomBrokerService(String keystoreFile) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        super();
        SslContext sslContext = new SslContext();
        sslContext.addTrustManager(new CustomTrustManager());
        sslContext.addKeyManager(new CustomKeyManager(keystoreFile));
        this.setSslContext(sslContext);
        this.setPersistent(false);
    }
}
