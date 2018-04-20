package ch.puzzle.activemq.broker;

import javax.net.ssl.X509KeyManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class CustomKeyManager implements X509KeyManager {
    private final char[] PASSWORD = "123456".toCharArray();
    KeyStore ks;

    CustomKeyManager(String keystoreFile) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(keystoreFile), PASSWORD);
    }

    @Override
    public String[] getClientAliases(String s, Principal[] principals) {
        return new String[0];
    }

    @Override
    public String chooseClientAlias(String[] strings, Principal[] principals, Socket socket) {
        return null;
    }

    @Override
    public String[] getServerAliases(String s, Principal[] principals) {
        return new String[0];
    }

    @Override
    public String chooseServerAlias(String s, Principal[] principals, Socket socket) {
        return "server-key";
    }

    @Override
    public X509Certificate[] getCertificateChain(String s) {
        try {
            return new X509Certificate[]{(X509Certificate) ks.getCertificateChain(s)[0]};
        } catch (KeyStoreException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PrivateKey getPrivateKey(String s) {
        try {
            return (PrivateKey) ks.getKey("server-key", PASSWORD);
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            return null;
        }
    }
}
